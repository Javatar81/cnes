package org.cnes.jstore.store.infinispan;

import java.time.ZoneOffset;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.cnes.jstore.model.Event;
import org.cnes.jstore.model.EventType;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.slf4j.LoggerFactory;

import io.quarkus.vertx.ConsumeEvent;

@ApplicationScoped
public class InfinispanWriter {
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(InfinispanWriter.class);
	private final RemoteCacheManager remoteCacheManager;
	private static final String xml = ""
			+ "<infinispan>\n"
			//+ "  <cache-container>"
			//+ "    <serialization marshaller=\"org.infinispan.commons.marshall.JavaSerializationMarshaller\">\n"
			//+ "      <allow-list>\n"
			//+ "        <class>org.cnes.jstore.store.infinispan.CachedEvent</class>\n"
			//+ "      </allow-list>\n"
			//+ "    </serialization>"
			//+ "  </cache-container>"
			+ "<distributed-cache owners=\"2\"\n"
			+ "                   segments=\"256\"\n"
			+ "                   capacity-factor=\"1.0\"\n"
			+ "                   l1-lifespan=\"5000\"\n"
			+ "                   mode=\"SYNC\"\n"
			+ "                   statistics=\"true\">\n"
			+ "  <encoding media-type=\"application/x-protostream\"/>\n"
			+ "  <locking isolation=\"REPEATABLE_READ\"/>\n"
			+ "  <transaction mode=\"FULL_XA\"\n"
			+ "               locking=\"OPTIMISTIC\"/>\n"
			+ "  <expiration lifespan=\"5000\"\n"
			+ "              max-idle=\"1000\" />\n"
			+ "  <memory max-count=\"1000000\"\n"
			+ "          when-full=\"REMOVE\"/>\n"
			//+ "  <indexing enabled=\"true\"\n"
			//+ "            storage=\"local-heap\">\n"
			//+ "    <index-reader refresh-interval=\"1000\"/>\n"      
			//+ "  </indexing>\n"
			+ "  <partition-handling when-split=\"ALLOW_READ_WRITES\"\n"
			+ "                      merge-policy=\"PREFERRED_NON_NULL\"/>\n"
			+ "  <persistence passivation=\"false\">\n"
			+ "    <!-- Persistent storage configuration. -->\n"
			+ "  </persistence>\n"
			+ "</distributed-cache>"
			+ ""
			+ "</infinispan>";
	private final XMLStringConfiguration xmlConfig = new XMLStringConfiguration(xml);

	@Inject
	public InfinispanWriter(RemoteCacheManager remoteCacheManager) {
		super();
		this.remoteCacheManager = remoteCacheManager;
		if (this.remoteCacheManager == null) {
			LOGGER.warn("No cache manager configured, won't write to cache");
		}
	}

	@ConsumeEvent("createdStore")
	public void consume(EventType type) {
		if (this.remoteCacheManager != null) {
			LOGGER.debug("Creating cache for store {}", type);
			remoteCacheManager.administration().getOrCreateCache(type.toString(), xmlConfig);
			LOGGER.debug("Cache created");
		}
	}

	@ConsumeEvent("appendedEvent")
	public void consumeAppendEvents(Event event) {
		if (this.remoteCacheManager != null) {
			RemoteCache<Object, Object> cache = remoteCacheManager.administration().getOrCreateCache(event.getType().toString(), xmlConfig);
			CachedEvent cachedEvent = new CachedEvent(event.getId().toString(), event.getCreated().toEpochSecond(ZoneOffset.UTC), event.getData());
			cache.put(event.getId().toString(), cachedEvent);
			LOGGER.debug("Written {} to cache", event);
		}
	}

}
