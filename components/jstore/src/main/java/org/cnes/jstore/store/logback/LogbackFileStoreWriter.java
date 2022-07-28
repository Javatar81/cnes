package org.cnes.jstore.store.logback;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.cnes.jstore.ConfigurationProperties;
import org.cnes.jstore.model.Event;
import org.cnes.jstore.model.EventType;
import org.cnes.jstore.store.FileStoreMeta;
import org.cnes.jstore.store.FileStoreWriter;
import org.cnes.jstore.store.WritingException;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.TriggeringPolicy;
import io.vertx.mutiny.core.eventbus.EventBus;

public class LogbackFileStoreWriter implements FileStoreWriter{
	private static final String PATTERN = "%msg%n";
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LogbackFileStoreWriter.class);
	private final EventType type;
	private final ObjectWriter jsonWriter;
	private final FileStoreMeta meta;
	private LoggerContext context;
	private Logger logWriter;
	private EventBus bus;
	private final ConfigurationProperties config;
	
	public LogbackFileStoreWriter(EventType type, ObjectMapper mapper, ConfigurationProperties config, EventBus bus) {
		LOGGER.debug("Creating log store instance for type '{}'", type);
		this.jsonWriter = mapper.writerFor(Event.class);
		this.type = type;
		this.config = config;
		this.meta = new FileStoreMeta(Paths.get(config.getStoreDir()), type);
		this.bus = bus;
		bus.publish("createdStore", this.getType());
	}

	private void initializeIfNecessary() {
		if (logWriter == null) {
			LOGGER.info("Config is '{}'", config);
			if (!Files.exists(meta.dirStorePath())) {
				LOGGER.warn("Cannot resolve store dir. Creating it...");
				try {
					Files.createDirectory(meta.dirStorePath());
				} catch (IOException e) {
					throw new WritingException(e);
				}
			}
			this.context = new LoggerContext();
			PatternLayoutEncoder encoder = new PatternLayoutEncoder();
			encoder.setPattern(PATTERN);
			encoder.setContext(context);
	        encoder.start();
			this.logWriter = createLogger(type, encoder);
	        LOGGER.debug("Started encoder with pattern '{}'", PATTERN);
		}
	}
	
	Logger createLogger(EventType type, PatternLayoutEncoder encoder) {
		LOGGER.debug("Creating new logger instance for type '{}'", type);
		RollingFileAppender<ILoggingEvent> fileAppender = createFileAppender(encoder);
		Logger newLogger = context.getLogger(LogbackFileStoreWriter.class);
		newLogger.setLevel(Level.WARN);
		newLogger.setAdditive(false); 
		if (config.isLogAsync()) {
			createAsyncAppender(fileAppender, newLogger);
		} else {			
			newLogger.addAppender(fileAppender);
		}
		return newLogger;
	}

	private RollingFileAppender<ILoggingEvent> createFileAppender(PatternLayoutEncoder encoder) {
		RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
		String path = meta.fileStorePath().toString();
		fileAppender.setFile(path);
		LOGGER.debug("Setting file appender to '{}'", path);
		fileAppender.setEncoder(encoder);
		fileAppender.setImmediateFlush(true);
		fileAppender.setContext(context);
		fileAppender.setAppend(true);
		fileAppender.setTriggeringPolicy(createTriggeringPolicy(fileAppender));
		fileAppender.start();
		return fileAppender;
	}

	private void createAsyncAppender(RollingFileAppender<ILoggingEvent> fileAppender, Logger newLogger) {
		AsyncAppender asyncAppender = new AsyncAppender();
		asyncAppender.setContext(context);
		asyncAppender.addAppender(fileAppender);
		asyncAppender.setMaxFlushTime(1000);
		asyncAppender.start();
		newLogger.addAppender(asyncAppender);
	}
	
	private TriggeringPolicy<ILoggingEvent> createTriggeringPolicy(FileAppender<ILoggingEvent> parent) {
		TimeBasedRollingPolicy<ILoggingEvent> triggeringPolicy = new TimeBasedRollingPolicy<>();
		triggeringPolicy.setParent(parent);
		triggeringPolicy.setContext(context);
		triggeringPolicy.setFileNamePattern(meta.getArchiveFolder() + "/" + type + config.getStoreArchivePattern() + ".log");
		triggeringPolicy.start();
		return triggeringPolicy;
	}
	
	@Override
	public Event append(String data) {
		initializeIfNecessary();
		Event event = new Event(type, data, Optional.empty());
		try {
			String valueAsString = jsonWriter.writeValueAsString(event);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Writing '{}'", valueAsString);
			}
			logWriter.warn(valueAsString);
			bus.publish("appendedEvent", event);
			return event;
		} catch (JsonProcessingException e) {
			throw new WritingException(e);
		}
	}
	
	@Override
	public EventType getType() {
		return type;
	}

	@Override
	public void delete() {
		try {
			Files.delete(meta.fileStorePath());
		} catch (IOException e) {
			throw new WritingException(e);
		}
	}
	
	
	
	
}
