package org.cnes.jstore.model;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;

import org.cnes.jstore.store.VerificationException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Event implements Serializable{
	
	private static final long serialVersionUID = 1L;
	@JsonProperty("created")
	private final long created;
	@JsonProperty("data")
	private final String data;
	@JsonProperty("id")
	private final Identifier id;
	@JsonProperty("predecessor")
	private final Identifier predecessor;
	@JsonIgnore
	private final EventType type;
	
	@JsonCreator
    Event(@JsonProperty("created") long created, @JsonProperty("data") String data, @JsonProperty("id") Identifier id, @JsonProperty("predecessor") Identifier predecessor) {
		this(created, null, data, id, predecessor);
	}
	
	
	public Event(long created, EventType type, String data, Identifier id, Identifier predecessor) {
		super();
		this.created = created;
		this.data = data;
		this.id = id;
		this.predecessor = predecessor;
		this.type = type;
	}
	
	
	
	public Event(EventType type, String data) {
		this(type, data, Optional.empty());
	}

	public Event(EventType type, String data, Optional<Event> predecessor) {
		this(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli(), type, data, predecessor);
	}
	
	public Event(long created, EventType type, String data, Optional<Event> predecessor) {
		this.created = created;
		this.type = type;
		this.data = data;
		this.predecessor = predecessor.map(Event::getId).orElse(null);
		this.id = generateId();
	}


	public Identifier generateId() {
		return Identifier.create(this.created, this.data, Optional.ofNullable(this.predecessor));
	}
	
	public String getData() {
		return data;
	}

	@JsonIgnore
	public LocalDateTime getCreated() {
		return Instant.ofEpochMilli(this.created).atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	@JsonIgnore
	public Optional<Identifier> getPredecessor() {
		return Optional.ofNullable(predecessor);
	}

	public Identifier getId() {
		return id;
	}

	public EventType getType() {
		return type;
	}
	
	public void verify() throws VerificationException {
		Identifier recalculatedId = Identifier.create(this.created, this.getData(), this.getPredecessor());
		if(!recalculatedId.equals(this.getId())) {
			throw new VerificationException(recalculatedId, this.getId(), toString() + " is not valid");
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(created, data, id, predecessor, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Event other = (Event) obj;
		return Objects.equals(created, other.created) && Objects.equals(data, other.data)
				&& Objects.equals(id, other.id) && Objects.equals(predecessor, other.predecessor)
				&& Objects.equals(type, other.type);
	}

	@Override
	public String toString() {
		return String.format("Event [created=%s, data=%s, id=%s, predecessor=%s, type=%s]", created, data, id,
				predecessor, type);
	}
	
	
	
}
