package org.cnes.jstore.api;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.cnes.jstore.model.Event;
import org.cnes.jstore.model.EventType;
import org.cnes.jstore.store.FileStore;
import org.cnes.jstore.store.FileStoreFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/store/{eventType}")
public class FileStoreResource {
	
	@Inject
	FileStoreFactory storeFactory;
	@Inject
	ObjectMapper mapper;
	
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Optional<Event> peek(@PathParam("eventType") String eventType) {
    	EventType type = new EventType(eventType);
    	FileStore fileStore = storeFactory.getFileStore(type);
    	return fileStore.peek();
    }
    
    @GET
    @Path("/{number}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Event> top(@PathParam("eventType") String eventType, @PathParam("number") int number) {
    	EventType type = new EventType(eventType);
    	FileStore fileStore = storeFactory.getFileStore(type);
    	return fileStore.top(number);
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void post(@PathParam("eventType") String eventType, EventBody body) {
    	EventType type = new EventType(eventType);
    	FileStore fileStore = storeFactory.getFileStore(type);
    	fileStore.append(body.getPayload().toString());
    }
}