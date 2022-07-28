package org.cnes.jstore.store.infinispan;

import java.util.Collections;
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
import org.slf4j.LoggerFactory;

public class InfinispanReader implements FileStoreReader{
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(InfinispanReader.class);
	private final EventType type;
	private final RemoteCacheManager cacheManager;
	private RemoteCache<String, CachedEvent> cache;
	
	public InfinispanReader(EventType type, RemoteCacheManager remoteCacheManager) {
		super();
		this.type = type;
		this.cacheManager = Objects.requireNonNull(remoteCacheManager);
		LOGGER.debug("Infinispan reader for {} created", type);
	}
	
	private RemoteCache<String, CachedEvent> getCache() {
		if (cache == null) {
			cache = Objects.requireNonNull(cacheManager).getCache(type.toString());
		}
		return cache;
	}
	
	private boolean cacheExists() {
		return getCache() != null;
	}

	@Override
	public List<Event> top(int n) {
		if (cacheExists()) {
			QueryFactory qf = org.infinispan.client.hotrod.Search.getQueryFactory(getCache());
			Query<CachedEvent> q = qf.create("from jstore.CachedEvent ORDER BY created DESC");
			q = q.maxResults(n);
			QueryResult<CachedEvent> queryResult = q.execute();
			return queryResult.list().stream()
					.map(e -> e.toEvent(type))
					.collect(Collectors.toList());
		} else {
			LOGGER.warn("Infinispan cache does not exist for {}", type);
			return Collections.emptyList();
		}
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
