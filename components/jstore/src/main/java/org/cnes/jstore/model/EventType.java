package org.cnes.jstore.model;

import java.util.Objects;

public class EventType {
	
	private final String name;
	
	public EventType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventType other = (EventType) obj;
		return Objects.equals(name, other.name);
	}

	@Override
	public String toString() {
		return name;
	}
	
}
