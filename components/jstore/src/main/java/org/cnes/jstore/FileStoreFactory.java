package org.cnes.jstore;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.cnes.jstore.model.EventType;
import org.cnes.jstore.store.FileStore;
import org.cnes.jstore.store.LogbackFileStore;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class FileStoreFactory {
	
	private Map<EventType, FileStore> fileStores = new HashMap<>();
	private ObjectMapper mapper;
	private ConfigurationProperties config;
	
	public FileStoreFactory(ObjectMapper mapper, ConfigurationProperties config) {
		this.mapper = mapper;
		this.config = config;
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

}
