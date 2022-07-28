package org.cnes.jstore.store;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.cnes.jstore.model.EventType;

public class FileStoreMeta {
	private final Path storeDir;
	private final EventType type;
	
	public FileStoreMeta(Path storeDir, EventType type) {
		super();
		this.storeDir = storeDir;
		this.type = type;
	}
	
	public Path dirStorePath() {
		return storeDir;
	}

	public Path fileStorePath() {
		return storeDir.resolve(Paths.get(getLogFileName()));
	}

	private String getLogFileName() {
		return type + ".log";
	}

	public Path getArchiveFolder() {
		return storeDir.resolve("archived");
	}
	
	public boolean isArchivedFiles() {
		return Files.exists(getArchiveFolder());
	}
}
