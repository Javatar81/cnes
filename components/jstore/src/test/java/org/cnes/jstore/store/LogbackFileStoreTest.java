package org.cnes.jstore.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.cnes.jstore.ConfigurationProperties;
import org.cnes.jstore.model.Event;
import org.cnes.jstore.model.EventType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

class LogbackFileStoreTest {
	
	private static final Path LOG_DIR = Paths.get("src/test/resources/tmp");
	private static final Logger LOGGER = LoggerFactory.getLogger(LogbackFileStore.class);
	private static ConfigurationProperties config;
	private static final String LOG_ARCHIVE_PATTERN ="yyyy-MM-dd";
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@BeforeAll
	static void setUp() throws IOException {
		if (!Files.exists(LOG_DIR)) {
			Files.createDirectory(LOG_DIR);
		}
		config = ConfigurationProperties.builder()
				.withStoreArchivePattern("%d{"+ LOG_ARCHIVE_PATTERN + "}")
				.withStoreDir(LOG_DIR.toString())
				.withLogAsync(false)
				.build();
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
	void appendSingleEvent() throws VerificationException {
		EventType type1 = new EventType("Type1");
		LogbackFileStore store = new LogbackFileStore(type1, new ObjectMapper(), config);
		store.append("testdata");
		assertLogFileExistsFor(store);
		store.verify(1);
	}
	
	@Test
    void appendTwoEventsWithSameStore() throws VerificationException {
		EventType type2 = new EventType("Type2");
		LogbackFileStore store = new LogbackFileStore(type2, new ObjectMapper(), config);
		store.append("testdata1");
		store.append("testdata2");
		assertLogFileExistsFor(store);
		store.verify(2);
	}
	
	@Test
    void appendTwoEventsWithSameAsyncStore() throws VerificationException {
	    ConfigurationProperties asyncConfig = ConfigurationProperties.builder()
				.withStoreArchivePattern("%d{"+ LOG_ARCHIVE_PATTERN + "}")
				.withStoreDir(LOG_DIR.toString())
				.withLogAsync(true)
				.build();
		EventType type = new EventType("twoEventsWithSameAsyncStore");
		LogbackFileStore store = new LogbackFileStore(type, new ObjectMapper(), asyncConfig);
		store.append("testdata1");
		store.append("testdata2");
		assertLogFileExistsFor(store);
		store.verify(2);
	}
	
	@Test
    void appendMultipleEventsWithTwoStores() throws VerificationException {
		final int ENTRIES = 100;
		EventType type2 = new EventType("MultipleEventsWithTwoStores");
		LogbackFileStore store1 = new LogbackFileStore(type2, new ObjectMapper(), config);
		LogbackFileStore store2 = new LogbackFileStore(type2, new ObjectMapper(), config);
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
		store1.verify(ENTRIES);
	}
	
	@Test
    void append1000Events() {
		EventType type = new EventType("1000Events");
		LogbackFileStore store = new LogbackFileStore(type, new ObjectMapper(), config);
		for (int i = 0; i < 1000; i++) {
			store.append("dummy" + i);
		}
		List<Event> top = store.top(1000);
		assertEquals(1000, top.size());
		Optional<Event> previous = Optional.empty();
		for (Event evt: top) {
			int i = top.indexOf(evt);
			assertEquals("dummy" + (1000 -1 - i), evt.getData());
			LocalDateTime previousDate = previous.map(Event::getCreated).orElse(evt.getCreated());
			assertFalse(previousDate.isAfter(evt.getCreated()), 
					String.format("Event %d hast timestamp before previous", i));
			
		}
	}
    
	
	
	@Test
	void peekFileNotExists() {
		EventType type1 = new EventType("peekNoFile");
		FileStore store = new LogbackFileStore(type1, new ObjectMapper(), config);
		assertFalse(store.peek().isPresent(), "File should not exist");
	}
	
	@Test
	void peekEmpty() throws IOException {
		EventType type1 = new EventType("peekEmpty");
		LogbackFileStore store = new LogbackFileStore(type1, new ObjectMapper(), config);
		try {
			Files.createFile(store.fileStorePath());
			assertFalse(store.peek().isPresent(), "File should be empty");
		} finally {
			if (Files.exists(store.fileStorePath())) {
				Files.delete(store.fileStorePath());
			}
		}
	}
	
	@Test
	void peekSingleEvent() {
		EventType type1 = new EventType("peekSingle");
		LogbackFileStore store = new LogbackFileStore(type1, new ObjectMapper(), config);
		Event event = store.append("dummy");
		assertLogFileExistsFor(store);
		Optional<Event> peek = store.peek();
		assertEquals("dummy", peek.map(Event::getData).orElse(""));
		assertEquals(event.getCreated(), peek.map(Event::getCreated).orElse(null));
	}
	
	@Test
	void peekTwoEvents() {
		EventType type1 = new EventType("peekTwo");
		FileStore store = new LogbackFileStore(type1, new ObjectMapper(), config);
		Event event1 = store.append("dummy1");
		Optional<Event> peek1 = store.peek();
		assertEquals("dummy1", peek1.map(Event::getData).orElse(""));
		assertEquals(event1.getCreated(), peek1.map(Event::getCreated).orElse(null));
		Event event2 = store.append("dummy2");
		Optional<Event> peek2 = store.peek();
		assertEquals("dummy2", peek2.map(Event::getData).orElse(""));
		assertEquals(event2.getCreated(), peek2.map(Event::getCreated).orElse(null));
		//assertEquals(peek1.map(Event::getId), peek2.flatMap(Event::getPredecessor));
		
	}
	
	@Test
	void peekThreeEvents() {
		EventType type1 = new EventType("peekThree");
		LogbackFileStore store = new LogbackFileStore(type1, new ObjectMapper(), config);
		store.append("dummy1");
		Optional<Event> peek1 = store.peek();
		assertEquals("dummy1", peek1.map(Event::getData).orElse(""));
		store.append("dummy2");
		Optional<Event> peek2 = store.peek();
		assertEquals("dummy2", peek2.map(Event::getData).orElse(""));
		//assertEquals(peek1.map(Event::getId), peek2.flatMap(Event::getPredecessor));
		store.append("dummy3");
		Optional<Event> peek3 = store.peek();
		assertEquals("dummy3", peek3.map(Event::getData).orElse(""));
		//assertEquals(peek2.map(Event::getId), peek3.flatMap(Event::getPredecessor));
		assertLogFileExistsFor(store);
	}
	
	@Test
	void topThreeEventsOfThree() {
		EventType type1 = new EventType("topThreeOfThree");
		FileStore store = new LogbackFileStore(type1, new ObjectMapper(), config);
		store.append("dummy1");
		store.append("dummy2");
		store.append("dummy3");
		List<Event> top3 = store.top(3);
		assertEquals(3, top3.size());
		assertEquals("dummy3",top3.get(0).getData());
		assertEquals("dummy2",top3.get(1).getData());
		assertEquals("dummy1",top3.get(2).getData());
	}
	
	@Test
	void topRolling1File() throws StreamWriteException, DatabindException, IOException, InterruptedException {
		EventType type = new EventType("topRolling1File");
		LogbackFileStore store = new LogbackFileStore(type, mapper, config);
		Event event4 = store.append("dummy4");
		Path archiveFile = createArchive(store, LocalDateTime.now());
		Event event1 = new Event(type, "dummy1", Optional.empty());
		Event event2 = new Event(type, "dummy2", Optional.empty());
		Event event3 = new Event(type, "dummy3", Optional.empty());
		String archiveContent = mapper.writeValueAsString(event1) + "\n" + mapper.writeValueAsString(event2) + "\n" + mapper.writeValueAsString(event3);
		Files.write(archiveFile, archiveContent.getBytes());
		assertEquals(event4.getData(), store.top(1).get(0).getData());
		List<Event> events = store.top(3);
		for (int i = 4; i > 1; i--) {
			assertEquals("dummy" + i, events.get(4 - i).getData());
		}
		events = store.top(4);
		for (int i = 4; i > 0; i--) {
			assertEquals("dummy" + i, events.get(4 - i).getData());
		}
	}
	
	@Test
	void topRolling2File() throws StreamWriteException, DatabindException, IOException, InterruptedException {
		EventType type = new EventType("topRolling2File");
		LogbackFileStore store = new LogbackFileStore(type, mapper, config);
		Event event7 = store.append("dummy7");
		Path archiveFileNow = createArchive(store, LocalDateTime.now());
		Event event4 = new Event(type, "dummy4", Optional.empty());
		Event event5 = new Event(type, "dummy5", Optional.empty());
		Event event6 = new Event(type, "dummy6", Optional.empty());
		String archiveContent = mapper.writeValueAsString(event4) + "\n" + mapper.writeValueAsString(event5) + "\n" + mapper.writeValueAsString(event6);
		Files.write(archiveFileNow, archiveContent.getBytes());
		Path archiveFileYesterday = createArchive(store, LocalDateTime.now().minusDays(1));
		Event event1 = new Event(type, "dummy1", Optional.empty());
		Event event2 = new Event(type, "dummy2", Optional.empty());
		Event event3 = new Event(type, "dummy3", Optional.empty());
		archiveContent = mapper.writeValueAsString(event1) + "\n" + mapper.writeValueAsString(event2) + "\n" + mapper.writeValueAsString(event3);
		Files.write(archiveFileYesterday, archiveContent.getBytes());
		assertEquals(event7.getData(), store.top(1).get(0).getData());
		List<Event> events = store.top(3);
		assertEquals(3, events.size());
		for (int i = 7; i > 4; i--) {
			assertEquals("dummy" + i, events.get(7 - i).getData());
		}
		events = store.top(4);
		assertEquals(4, events.size());
		for (int i = 7; i > 3; i--) {
			assertEquals("dummy" + i, events.get(7 - i).getData());
		}
		events = store.top(5);
		assertEquals(5, events.size());
		for (int i = 7; i > 2; i--) {
			assertEquals("dummy" + i, events.get(7 - i).getData());
		}
	}
	
	private Path createArchive(LogbackFileStore store, LocalDateTime dateTime) throws IOException {
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(LOG_ARCHIVE_PATTERN);
		if (!Files.exists(store.getArchiveFolder())) {
			Files.createDirectory(store.getArchiveFolder());
		}
		Path archiveFile = store.getArchiveFolder().resolve(store.getType() + timeFormatter.format(dateTime) + ".log");
		if (!Files.exists(archiveFile)) {			
			Files.createFile(archiveFile);
		}
		return archiveFile;
	}
	
	
	
	private void assertLogFileExistsFor(LogbackFileStore store) {
		Path path = store.fileStorePath();
		assertTrue(Files.exists(path), "Expected logfile does not exist at " + path);
	}
}
