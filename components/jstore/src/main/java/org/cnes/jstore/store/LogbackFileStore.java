package org.cnes.jstore.store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.cnes.jstore.model.Event;
import org.cnes.jstore.model.EventType;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;

public class LogbackFileStore implements FileStore{
	private static final String PATTERN = "%msg%n";
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LogbackFileStore.class);
	private final Logger logger;
	private final EventType type;
	private final PatternLayoutEncoder encoder;
	private final LoggerContext context;
	private final ObjectWriter jsonWriter;
	private final ObjectReader jsonReader;
	private final Path storeDir;
	private Optional<Event> top = Optional.empty();
	
	public LogbackFileStore(EventType type, ObjectMapper mapper, Path storeDir) {
		LOGGER.debug("Creating log store instance for type '{}'", type);
		this.jsonReader = mapper.readerFor(Event.class);
		this.jsonWriter = mapper.writerFor(Event.class);
		this.type = type;
		this.storeDir = storeDir;
		LOGGER.debug("Store dir is '{}'", storeDir.toString());
		if (!Files.exists(storeDir)) {
			LOGGER.warn("Cannot resolve store dir. Creating it...");
			try {
				Files.createDirectory(storeDir);
			} catch (IOException e) {
				throw new WritingException(e);
			}
		}
		this.context = new LoggerContext();
		this.encoder = new PatternLayoutEncoder();
		encoder.setPattern(PATTERN);
		encoder.setContext(context);
        encoder.start();
		this.logger = createLogger(type);
        LOGGER.debug("Started encoder with pattern '{}'", PATTERN);
	}
	
	Logger createLogger(EventType type) {
		LOGGER.debug("Creating new logger instance for type '{}'", type);
		FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
		String path = fileStorePath().toString();
		fileAppender.setFile(path);
		LOGGER.debug("Setting file appender to '{}'", path);
		fileAppender.setEncoder(encoder);
		fileAppender.setContext(context);
		fileAppender.start();
		Logger newLogger = context.getLogger(LogbackFileStore.class);
		newLogger.addAppender(fileAppender);
		newLogger.setLevel(Level.DEBUG);
		newLogger.setAdditive(false); 
		return newLogger;
	}
	
	@Override
	public Event append(String data) {
		Event event = new Event(type, data, top);
		try {
			String valueAsString = jsonWriter.writeValueAsString(event);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Writing '{}'", valueAsString);
			}
			logger.debug(valueAsString);
			this.top = peek();
			return event;
		} catch (JsonProcessingException e) {
			throw new WritingException(e);
		}
	}
	
	@Override
	public Optional<Event> peek() {
		Path path = fileStorePath();
		try(Stream<String> lines = Files.lines(path)) {
			return top(lines, 1).findFirst();
		} catch (IOException e) {
			throw new ReadingException(e);
		}
	}
	
	@Override
	public List<Event> top(int n) {
		Path path = fileStorePath();
		try(Stream<String> lines = Files.lines(path)) {
			List<Event> topAsc = top(lines, n).collect(Collectors.toList());
			Collections.reverse(topAsc);
			return topAsc;
		} catch (IOException e) {
			throw new ReadingException(e);
		}
	}
	
	@Override
	public void verify(int n) throws VerificationException {
		for (Event event : top(n)) {
			event.verify();
		}
	}
	
	private Stream<Event> top(Stream<String> lines, int n) throws IOException {
		Path path = fileStorePath();
		try(Stream<String> linesForCount = Files.lines(path)) {
			long lineCount = linesForCount.count();
			if (lineCount > 0) {
				long skipLines = n;
				if (n > lineCount) {
					skipLines = lineCount;
				}
				return lines.skip(lineCount - skipLines).map(t -> {
					try {
						return jsonReader.readValue(t);
					} catch (JsonProcessingException e) {
						throw new ReadingException(e);
					}
				});
			} else {
				return Stream.empty();
			}
		} 
	}
	
	Path fileStorePath() {
		return storeDir.resolve(Paths.get(type + ".log"));
	}
}
