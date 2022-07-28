package org.cnes.jstore.store;

import java.util.List;
import java.util.Optional;

import org.cnes.jstore.model.Event;
import org.cnes.jstore.model.EventType;

public interface FileStoreReader {
	
	List<Event> top(int n);

	default Optional<Event> peek() {
		List<Event> top = top(1);
		if (!top.isEmpty()) {
			return Optional.of(top.get(0));
		} else {
			return Optional.empty();
		}
	}

	default void verify(int n) throws VerificationException {
		for (Event event : top(n)) {
			event.verify();
		}
	}

	long size();

	EventType getType();

}
