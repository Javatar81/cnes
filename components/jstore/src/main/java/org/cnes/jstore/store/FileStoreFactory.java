package org.cnes.jstore.store;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.cnes.jstore.model.EventType;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class FileStoreFactory {
	private Map<EventType, FileStore> fileStores = new HashMap<>();
	private ObjectMapper mapper;
	
	public FileStoreFactory(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	public FileStore getFileStore(EventType type) {
		FileStore store;
		if (!fileStores.containsKey(type)) {
			store = new LogbackFileStore(type, mapper, Paths.get("src/test/resources/"));
			fileStores.put(type, store);
		} else {
			store = fileStores.get(type);
		}
		return store;
	}

}
