package org.cnes.jstore.store;

import java.util.List;
import java.util.Optional;

import org.cnes.jstore.model.Event;
import org.cnes.jstore.model.EventType;

public interface FileStore {

	List<Event> top(int n);

	Optional<Event> peek();

	Event append(String data);

	void verify(int n) throws VerificationException;

	long size();

	EventType getType();

	void delete();

}
