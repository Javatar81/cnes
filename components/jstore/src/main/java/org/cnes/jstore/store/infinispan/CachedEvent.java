package org.cnes.jstore.store.infinispan;

import java.util.Objects;

import org.cnes.jstore.model.Event;
import org.cnes.jstore.model.EventType;
import org.cnes.jstore.model.Identifier;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

public class CachedEvent {

	private final String id;
	private final Long created;
	private final String payload;
	
	@ProtoFactory
    public CachedEvent(String id, Long created, String payload) {
        this.id = Objects.requireNonNull(id);
        this.created = Objects.requireNonNull(created);
        this.payload = Objects.requireNonNull(payload);
    }
	
    @ProtoField(number = 1)
    public String getId() {
        return id;
    }

    @ProtoField(number = 2)
    public Long getCreated() {
        return created;
    }

    @ProtoField(number = 3)
    public String getPayload() {
        return payload;
    }
    
    public Event toEvent(EventType type) {
    	return new Event(getCreated(), type, getPayload(), new Identifier(getId()), null);
    }

}
