package org.cnes.jstore.store;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.cnes.jstore.ConfigurationProperties;
import org.cnes.jstore.model.Event;
import org.cnes.jstore.model.EventType;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class LocalFileStoreReader implements FileStoreReader{
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LocalFileStoreReader.class);
	private final ObjectReader jsonReader;
	private final EventType type;
	final FileStoreMeta meta;
	
	
	public LocalFileStoreReader(ObjectMapper mapper, EventType type, ConfigurationProperties config) {
		this.jsonReader = mapper.readerFor(Event.class);
		this.type = type;
		this.meta = new FileStoreMeta(Paths.get(config.getStoreDir()), type);
	}
	
	@Override
	public EventType getType() {
		return type;
	}

	@Override
	public Optional<Event> peek() {
		Path path = meta.fileStorePath();
		if (Files.exists(path)) {
			try(Stream<String> lines = Files.lines(path)) {
				return top(lines, meta.fileStorePath(), 1).findFirst();
			} catch (IOException e) {
				throw new ReadingException(e);
			}
		} else {
			return Optional.empty();
		}
	}
	
	@Override
	public void verify(int n) throws VerificationException {
		for (Event event : top(n)) {
			event.verify();
		}
	}
	
	@Override
	public long size() {
		long size = 0;
		try(Stream<String> lines = Files.lines(meta.fileStorePath())) {
			size += lines.count();
			for(File archive : getArchivedFiles()) {
				try(Stream<String> archiveLines = Files.lines(Paths.get(archive.getAbsolutePath()))) {
					size += archiveLines.count();
				}
			}
			return size;
		} catch (IOException e) {
			throw new ReadingException(e);
		}
	}
	
	@Override
	public List<Event> top(int n) {
		Path path = meta.fileStorePath();
		try(Stream<String> lines = Files.lines(path)) {
			List<Event> topAsc = top(lines, meta.fileStorePath(), n).collect(Collectors.toList());
			LOGGER.debug("Found {} events in file", topAsc.size());
			if (meta.isArchivedFiles() && topAsc.size() < n) {
				List<File> archivedFiles = getArchivedFiles();
				LOGGER.debug("Reading {} archived files", archivedFiles.size());
				List<Event> archivedAsc = readArchived(n - topAsc.size(), 0, archivedFiles);
				LOGGER.trace("Found {} events in archived files", archivedAsc.size());
				archivedAsc.addAll(topAsc);
				topAsc = archivedAsc;
			}
			Collections.reverse(topAsc);
			return topAsc;
		} catch (IOException e) {
			throw new ReadingException(e);
		}
	}
	
	private List<Event> readArchived(int n, int archiveNum, List<File> archives) throws IOException {
		File archive = archives.get(archiveNum);
		LOGGER.debug("Reading next {} entries from {}", n, archive.getAbsolutePath());
		try(Stream<String> archivedLines = Files.lines(Paths.get(archive.getAbsolutePath()))) {
			List<Event> collectedFromArchive = top(archivedLines, Paths.get(archive.getAbsolutePath()), n).collect(Collectors.toList());
			if (n - collectedFromArchive.size() > 0 && archives.size() > archiveNum + 1) {
				List<Event> collectedFromNextArchive = readArchived(n - collectedFromArchive.size(), archiveNum + 1, archives);
				collectedFromNextArchive.addAll(collectedFromArchive);
				collectedFromArchive = collectedFromNextArchive;
			}
			return collectedFromArchive;
		}
	}
	
	private Stream<Event> top(Stream<String> lines, Path path, int n) throws IOException {
		try(Stream<String> linesForCount = Files.lines(path)) {
			long lineCount = linesForCount.count();
			LOGGER.debug("Found {} lines", lineCount);
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
	
	private List<File> getArchivedFiles() {
    	Pattern pattern = Pattern.compile(type + ".*" + "\\.log");
    	try (Stream<Path> walk = Files.walk(meta.getArchiveFolder())) {
		    return walk.sorted(Comparator.comparing(Path::getFileName).reversed())
		        .map(Path::toFile)
		        .filter(f -> f.getName().endsWith(".log"))
		        .filter(f -> pattern.matcher(f.getName()).matches())
		        .collect(Collectors.toList());
		} catch (IOException e) {
			throw new ReadingException(e);
		}
    }

	

}
