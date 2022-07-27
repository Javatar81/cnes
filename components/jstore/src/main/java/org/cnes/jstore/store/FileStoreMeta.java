package org.cnes.jstore.store;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.cnes.jstore.model.EventType;

class FileStoreMeta {
	private final Path storeDir;
	private final EventType type;
	
	public FileStoreMeta(Path storeDir, EventType type) {
		super();
		this.storeDir = storeDir;
		this.type = type;
	}
	
	Path dirStorePath() {
		return storeDir;
	}

	Path fileStorePath() {
		return storeDir.resolve(Paths.get(getLogFileName()));
	}

	private String getLogFileName() {
		return type + ".log";
	}

	Path getArchiveFolder() {
		return storeDir.resolve("archived");
	}
	
	boolean isArchivedFiles() {
		return Files.exists(getArchiveFolder());
	}
}
