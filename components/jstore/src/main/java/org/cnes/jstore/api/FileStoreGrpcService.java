package org.cnes.jstore.api;

import org.cnes.jstore.FileStoreFactory;
import org.cnes.jstore.model.EventType;

import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;

@GrpcService
public class FileStoreGrpcService implements FileStoreGrpc {
	private final FileStoreFactory storeFactory;
	private final MeterRegistry registry;
	
	public FileStoreGrpcService(MeterRegistry registry, FileStoreFactory storeFactory) {
		this.registry = registry;
		this.storeFactory = storeFactory;
	}

	@Override
	public Uni<Empty> append(EventBodyGrpc request) {
		storeFactory.getFileStore(new EventType("test")).append("");
		return Uni.createFrom().item(request).map(r -> {
			storeFactory.getFileStore(new EventType("test")).append("");
			return Empty.newBuilder().build();
		});
	}

}
