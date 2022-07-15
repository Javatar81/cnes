package org.cnes.jstore.api;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.cnes.jstore.FileStoreFactory;

@Path("/store")
public class StoresResource {
	
	private final FileStoreFactory storeFactory;
	
	public StoresResource(FileStoreFactory storeFactory) {
		super();
		this.storeFactory = storeFactory;
	}
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<StoreListEntry> peek(@PathParam("eventType") String eventType) throws IOException {
    	return storeFactory.getAllStores().collect(Collectors.toList());	
    }

}
