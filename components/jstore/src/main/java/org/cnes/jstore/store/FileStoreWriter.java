package org.cnes.jstore.store;

import org.cnes.jstore.model.Event;
import org.cnes.jstore.model.EventType;

public interface FileStoreWriter {

	Event append(String data);

	void delete();
	
	EventType getType();

}
