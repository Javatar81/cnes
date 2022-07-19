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
import org.cnes.jstore.store.FileStore;
import org.cnes.jstore.store.LogbackFileStore;
import org.cnes.jstore.store.ReadingException;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class FileStoreFactory {
	
	private Map<EventType, FileStore> fileStores = new HashMap<>();
	private ObjectMapper mapper;
	private ConfigurationProperties config;
	
	public FileStoreFactory(ObjectMapper mapper, ConfigurationProperties config) {
		this.mapper = mapper;
		this.config = config;
		try (Stream<Path> fileWalk = Files.walk(Paths.get(config.getStoreDir()), 1)){
			fileWalk
				.map(Path::getFileName)
				.filter(f -> f.toString().endsWith(".log"))
				.map(f -> f.getName(0).toString().split("\\.log")[0])
				.forEach(n -> fileStores.put(new EventType(n), new LogbackFileStore(new EventType(n), mapper, config)));
		} catch (IOException e) {
			throw new ReadingException(e);
		}
	}
	
	public FileStore getFileStore(EventType type) {
		FileStore store;
		if (!fileStores.containsKey(type)) {
			store = new LogbackFileStore(type, mapper, config);
			fileStores.put(type, store);
		} else {
			store = fileStores.get(type);
		}
		return store;
	}
	
	public void removeFileStore(EventType type) {
		fileStores.get(type).delete();
		fileStores.remove(type);
	}
	
	
	public Stream<StoreListEntry> getAllStores() throws IOException {
		return fileStores.values().stream()
			.map(s -> new StoreListEntry(s.getType().toString(), s.size()));
	}

}
