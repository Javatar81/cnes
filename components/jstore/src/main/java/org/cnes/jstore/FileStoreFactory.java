package org.cnes.jstore;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.cnes.jstore.model.EventType;
import org.cnes.jstore.store.FileStore;
import org.cnes.jstore.store.LogbackFileStore;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class FileStoreFactory {
	@ConfigProperty(name = "org.cnes.jstore.store-path", defaultValue = "src/test/resources/")
	String storePath;
	private Map<EventType, FileStore> fileStores = new HashMap<>();
	private ObjectMapper mapper;
	
	
	public FileStoreFactory(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	public FileStore getFileStore(EventType type) {
		FileStore store;
		if (!fileStores.containsKey(type)) {
			store = new LogbackFileStore(type, mapper, Paths.get(storePath));
			fileStores.put(type, store);
		} else {
			store = fileStores.get(type);
		}
		return store;
	}

}
