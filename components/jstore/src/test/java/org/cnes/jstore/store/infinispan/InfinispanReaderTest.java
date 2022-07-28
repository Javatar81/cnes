package org.cnes.jstore.store.infinispan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.cnes.jstore.model.Event;
import org.cnes.jstore.model.EventType;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class InfinispanReaderTest {

	private final InfinispanWriter writer;
	private final RemoteCacheManager remoteCacheManager;
	private static DateTimeFormatter TEST_OUTPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");
	
	InfinispanReaderTest(InfinispanWriter writer, RemoteCacheManager remoteCacheManager) {
		this.writer = writer;
		this.remoteCacheManager = remoteCacheManager;
	}
	
	@Test
	void testTop() throws InterruptedException {
		EventType type = new EventType("testTop");
		writer.consumeCreatedStore(type);
		InfinispanReader reader = new InfinispanReader(type, remoteCacheManager);
		for(Event evt : generateEvents(type, 100)) {
			writer.consumeAppendEvents(evt);
		}
		List<Event> top = reader.top(100);
		assertEquals(100, reader.size());
		assertEquals(100, top.size());
		Optional<Event> previous = Optional.empty();
		for(Event evt : top) {
			int i = top.indexOf(evt);
			assertEquals("data" + (100 -1 - i), evt.getData());
			LocalDateTime previousDate = previous.map(Event::getCreated).orElse(evt.getCreated());
			assertFalse(previousDate.isBefore(evt.getCreated()), 
					String.format("Event %d has timestamp %s before %s", i, TEST_OUTPUT_FORMAT.format(evt.getCreated()), TEST_OUTPUT_FORMAT.format(previousDate)));
			previous = Optional.of(evt);
		}
	}
	
	private List<Event> generateEvents(EventType type, int n) {
		List<Event> evts = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			evts.add(new Event(System.currentTimeMillis() - 1000 * (n - i), type, "data" + i, Optional.empty()));
		}
		return evts;
	}

}
