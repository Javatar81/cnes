package org.cnes.jstore.api;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.cnes.jstore.FileStoreFactory;
import org.cnes.jstore.model.Event;
import org.cnes.jstore.model.EventType;
import org.cnes.jstore.store.FileStoreReader;
import org.cnes.jstore.store.FileStoreWriter;

import io.micrometer.core.instrument.MeterRegistry;
import io.smallrye.common.annotation.NonBlocking;

@Path("/store/{eventType}")
public class FileStoreResource {
	
	
	private final FileStoreFactory storeFactory;
	private final MeterRegistry registry;
	
	public FileStoreResource(MeterRegistry registry, FileStoreFactory storeFactory) {
		this.registry = registry;
		this.storeFactory = storeFactory;
	}
	
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Optional<Event> peek(@PathParam("eventType") String eventType) {
    	EventType type = new EventType(eventType);
    	FileStoreReader reader = storeFactory.getFileReader(type);
    	return reader.peek();
    }
    
    @GET
    @Path("/{number}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Event> top(@PathParam("eventType") String eventType, @PathParam("number") int number) {
    	EventType type = new EventType(eventType);
    	FileStoreReader reader = storeFactory.getFileReader(type);
    	return reader.top(number);
    }
    
    @POST
    @NonBlocking
    @Consumes(MediaType.APPLICATION_JSON)
    public void post(@PathParam("eventType") String eventType, EventBody body) {
    	registry.counter("org.cnes.events.stored." + eventType).increment();
    	EventType type = new EventType(eventType);
    	FileStoreWriter fileStore = storeFactory.getFileStore(type);
    	fileStore.append(body.getPayload().toString());
    }
    
    @DELETE
    public void delete(@PathParam("eventType") String eventType) {
    	EventType type = new EventType(eventType);
    	storeFactory.removeFileStore(type);
    }
}