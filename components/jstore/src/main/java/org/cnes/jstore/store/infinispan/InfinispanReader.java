package org.cnes.jstore.store.infinispan;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.cnes.jstore.model.Event;
import org.cnes.jstore.model.EventType;
import org.cnes.jstore.store.FileStoreReader;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.infinispan.query.dsl.QueryResult;

public class InfinispanReader implements FileStoreReader{

	private final EventType type;
	private final RemoteCache<String, CachedEvent> cache;
	
	public InfinispanReader(EventType type, RemoteCacheManager remoteCacheManager) {
		super();
		this.type = type;
		Objects.requireNonNull(remoteCacheManager);
		this.cache = remoteCacheManager.getCache(type.toString());
	}

	@Override
	public List<Event> top(int n) {
		QueryFactory qf = org.infinispan.client.hotrod.Search.getQueryFactory(cache);
		Query<CachedEvent> q = qf.create("from jstore.CachedEvent ORDER BY created DESC");
		q = q.maxResults(n);
		QueryResult<CachedEvent> queryResult = q.execute();
		return queryResult.list().stream()
				.map(e -> e.toEvent(type))
				.collect(Collectors.toList());
	}

	@Override
	public long size() {
		return cache.size();
	}

	@Override
	public EventType getType() {
		return type;
	}

}
