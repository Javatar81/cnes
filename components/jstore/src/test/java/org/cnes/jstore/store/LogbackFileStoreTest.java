package org.cnes.jstore.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.cnes.jstore.model.Event;
import org.cnes.jstore.model.EventType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

class LogbackFileStoreTest {
	
	private static Path LOG_DIR = Paths.get("src/test/resources/tmp");
	private static final Logger LOGGER = LoggerFactory.getLogger(LogbackFileStore.class);
	
	@BeforeAll
	static void setUp() throws IOException {
		if (!Files.exists(LOG_DIR)) {
			Files.createDirectory(LOG_DIR);
		}
	}
	
	@AfterAll
	static void tearDown() throws IOException {
		try (Stream<Path> walk = Files.walk(LOG_DIR)) {
		    walk.sorted(Comparator.reverseOrder())
		        .map(Path::toFile)
		        .peek(f -> LOGGER.debug(f.toString()))
		        .forEach(File::delete);
		}
	}
	 
	@Test
	void appendSingleEvent() {
		EventType type1 = new EventType("Type1");
		LogbackFileStore store = new LogbackFileStore(type1, new ObjectMapper(), LOG_DIR);
		store.append("testdata");
		assertLogFileExistsFor(store);
	}
	
	@Test
    void appendTwoEventsWithSameStore() {
		EventType type2 = new EventType("Type2");
		LogbackFileStore store = new LogbackFileStore(type2, new ObjectMapper(), LOG_DIR);
		store.append("testdata1");
		store.append("testdata2");
		assertLogFileExistsFor(store);
	}
	
	@Test
    void appendMultipleEventsWithTwoStores() {
		final int ENTRIES = 100;
		EventType type2 = new EventType("Type2");
		LogbackFileStore store1 = new LogbackFileStore(type2, new ObjectMapper(), LOG_DIR);
		LogbackFileStore store2 = new LogbackFileStore(type2, new ObjectMapper(), LOG_DIR);
		List<String> testData = new ArrayList<>();
		for (int i = 0; i < ENTRIES; i++) {
			testData.add("testdata" + i);
		}
		for (Iterator<String> iterator = testData.iterator(); iterator.hasNext();) {
			String dataForStore1 = iterator.next();
			store1.append(dataForStore1);
			assertEquals(store1.peek().get().getData(), dataForStore1);
			assertEquals(store2.peek().get().getData(), dataForStore1);
			String dataForStore2 = iterator.next();
			store2.append(dataForStore2);
			assertEquals(store1.peek().get().getData(), dataForStore2);
			assertEquals(store2.peek().get().getData(), dataForStore2);
		}
		assertLogFileExistsFor(store1);
		assertLogFileExistsFor(store2);
		List<Event> topEvents = store1.top(ENTRIES);
		assertEquals(ENTRIES, topEvents.size());
		List<String> topEventData = topEvents.stream().map(Event::getData).collect(Collectors.toList());
		Collections.reverse(topEventData);
		assertEquals(testData, topEventData);
	}
	
	
	
	@Test
	void peekFileNotExists() {
		EventType type1 = new EventType("peekNoFile");
		FileStore store = new LogbackFileStore(type1, new ObjectMapper(), LOG_DIR);
		assertFalse(store.peek().isPresent(), "File should not exist");
	}
	
	@Test
	void peekEmpty() throws IOException {
		EventType type1 = new EventType("peekEmpty");
		LogbackFileStore store = new LogbackFileStore(type1, new ObjectMapper(),LOG_DIR);
		try {
			assertLogFileExistsFor(store);
			Files.delete(store.fileStorePath());
			Files.createFile(store.fileStorePath());
			assertFalse(store.peek().isPresent(), "File should be empty");
		} finally {
			Files.delete(store.fileStorePath());
		}
	}
	
	@Test
	void peekSingleEvent() {
		EventType type1 = new EventType("peekSingle");
		LogbackFileStore store = new LogbackFileStore(type1, new ObjectMapper(), LOG_DIR);
		store.append("dummy");
		assertLogFileExistsFor(store);
		assertEquals("dummy", store.peek().map(Event::getData).orElse(""));
	}
	
	@Test
	void peekTwoEvents() {
		EventType type1 = new EventType("peekTwo");
		LogbackFileStore store = new LogbackFileStore(type1, new ObjectMapper(), LOG_DIR);
		assertLogFileExistsFor(store);
		store.append("dummy1");
		Optional<Event> peek1 = store.peek();
		assertEquals("dummy1", peek1.map(Event::getData).orElse(""));
		store.append("dummy2");
		Optional<Event> peek2 = store.peek();
		assertEquals("dummy2", peek2.map(Event::getData).orElse(""));
		assertEquals(peek1.map(Event::getId), peek2.flatMap(Event::getPredecessor));
		
	}
	
	@Test
	void peekThreeEvents() {
		EventType type1 = new EventType("peekTwo");
		LogbackFileStore store = new LogbackFileStore(type1, new ObjectMapper(), LOG_DIR);
		assertLogFileExistsFor(store);
		store.append("dummy1");
		Optional<Event> peek1 = store.peek();
		assertEquals("dummy1", peek1.map(Event::getData).orElse(""));
		store.append("dummy2");
		Optional<Event> peek2 = store.peek();
		assertEquals("dummy2", peek2.map(Event::getData).orElse(""));
		assertEquals(peek1.map(Event::getId), peek2.flatMap(Event::getPredecessor));
		store.append("dummy3");
		Optional<Event> peek3 = store.peek();
		assertEquals("dummy3", peek3.map(Event::getData).orElse(""));
		assertEquals(peek2.map(Event::getId), peek3.flatMap(Event::getPredecessor));
		assertLogFileExistsFor(store);
	}
	
	@Test
	void topThreeEventsOfThree() {
		EventType type1 = new EventType("topThreeOfThree");
		FileStore store = new LogbackFileStore(type1, new ObjectMapper(), LOG_DIR);
		store.append("dummy1");
		store.append("dummy2");
		store.append("dummy3");
		List<Event> top3 = store.top(3);
		assertEquals(3, top3.size());
		assertEquals("dummy3",top3.get(0).getData());
		assertEquals("dummy2",top3.get(1).getData());
		assertEquals("dummy1",top3.get(2).getData());
	}
	
	
	
	private void assertLogFileExistsFor(LogbackFileStore store) {
		Path path = store.fileStorePath();
		assertTrue(Files.exists(path), "Expected logfile does not exist at " + path);
	}
}
