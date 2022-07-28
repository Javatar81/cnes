package org.cnes.jstore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;

import org.cnes.jstore.api.StoreListEntry;
import org.cnes.jstore.model.EventType;
import org.cnes.jstore.store.FileStoreReader;
import org.cnes.jstore.store.FileStoreWriter;
import org.cnes.jstore.store.LocalFileStoreReader;
import org.cnes.jstore.store.ReadingException;
import org.cnes.jstore.store.logback.LogbackFileStoreWriter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.mutiny.core.eventbus.EventBus;

@ApplicationScoped
public class FileStoreFactory {
	
	private Map<EventType, FileStoreWriter> fileStores = new HashMap<>();
	private Map<EventType, FileStoreReader> readers = new HashMap<>();
	private ObjectMapper mapper;
	private ConfigurationProperties config;
	private EventBus bus;
	
	public FileStoreFactory(ObjectMapper mapper, ConfigurationProperties config, EventBus bus) {
		this.mapper = mapper;
		this.config = config;
		try (Stream<Path> fileWalk = Files.walk(Paths.get(config.getStoreDir()), 1)){
			fileWalk
				.map(Path::getFileName)
				.filter(f -> f.toString().endsWith(".log"))
				.map(f -> f.getName(0).toString().split("\\.log")[0])
				
				.forEach(n -> {
					fileStores.put(new EventType(n), new LogbackFileStoreWriter(new EventType(n), mapper, config, bus));
					readers.put(new EventType(n), new LocalFileStoreReader(mapper, new EventType(n), config));
				});
				
		} catch (IOException e) {
			throw new ReadingException(e);
		}
	}
	
	/**
	 * Lazy inits the file store writer and reader.
	 * @param type
	 * @return
	 */
	public FileStoreWriter getFileStore(EventType type) {
		FileStoreWriter store;
		if (!fileStores.containsKey(type)) {
			store = new LogbackFileStoreWriter(type, mapper, config, bus);
			fileStores.put(type, store);
			readers.put(type, getFileReader(type));
		} else {
			store = fileStores.get(type);
		}
		return store;
	}
	
	public FileStoreReader getFileReader(EventType type) {
		FileStoreReader reader;
		if (!readers.containsKey(type)) {
			reader = new LocalFileStoreReader(mapper, type, config);
			readers.put(type, reader);
		} else {
			reader = readers.get(type);
		}
		return reader;
	}
	
	public void removeFileStore(EventType type) {
		fileStores.get(type).delete();
		fileStores.remove(type);
	}
	
	
	public Stream<StoreListEntry> getAllStores() throws IOException {
		return fileStores.values().stream()
			.map(s -> new StoreListEntry(s.getType().toString(), getFileReader(s.getType()).size()));
	}

}
