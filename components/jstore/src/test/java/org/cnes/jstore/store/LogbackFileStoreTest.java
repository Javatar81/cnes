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
import org.cnes.jstore.store.logback.LogbackFileStoreWriter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.mutiny.core.eventbus.EventBus;

class LogbackFileStoreTest {
	
	private static final Path LOG_DIR = Paths.get("src/test/resources/tmp");
	private static final Logger LOGGER = LoggerFactory.getLogger(LogbackFileStoreWriter.class);
	private static ConfigurationProperties config;
	private static final String LOG_ARCHIVE_PATTERN ="yyyy-MM-dd";
	private static DateTimeFormatter TEST_OUTPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");
	private static EventBus bus = new EventBus(null) {
		@Override
		public EventBus publish(String message, Object event) {
			// Do nothing in test
			return this;
			
		}
	};
	private static ObjectMapper mapper = new ObjectMapper();
	
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
		LocalFileStoreReader reader = new LocalFileStoreReader(mapper, type1, config);
		LogbackFileStoreWriter store = new LogbackFileStoreWriter(type1, mapper, config, bus);
		store.append("testdata");
		assertLogFileExistsFor(reader);
		reader.verify(1);
	}
	
	@Test
    void appendTwoEventsWithSameStore() throws VerificationException {
		EventType type2 = new EventType("Type2");
		LocalFileStoreReader reader = new LocalFileStoreReader(mapper, type2, config);
		LogbackFileStoreWriter store = new LogbackFileStoreWriter(type2, mapper, config, bus);
		store.append("testdata1");
		store.append("testdata2");
		assertLogFileExistsFor(reader);
		reader.verify(2);
	}
	
	@Test
    void appendTwoEventsWithSameAsyncStore() throws VerificationException, InterruptedException {
	    ConfigurationProperties asyncConfig = ConfigurationProperties.builder()
				.withStoreArchivePattern("%d{"+ LOG_ARCHIVE_PATTERN + "}")
				.withStoreDir(LOG_DIR.toString())
				.withLogAsync(true)
				.build();
		EventType type = new EventType("twoEventsWithSameAsyncStore");
		LocalFileStoreReader reader = new LocalFileStoreReader(mapper, type, config);
		LogbackFileStoreWriter store = new LogbackFileStoreWriter(type, mapper, asyncConfig, bus);
		store.append("testdata1");
		store.append("testdata2");
		Thread.sleep(1000);
		assertLogFileExistsFor(reader);
		reader.verify(2);
	}
	
	@Test
    void appendMultipleEventsWithTwoStores() throws VerificationException {
		final int ENTRIES = 100;
		EventType type2 = new EventType("MultipleEventsWithTwoStores");
		LogbackFileStoreWriter store1 = new LogbackFileStoreWriter(type2, mapper, config, bus);
		LogbackFileStoreWriter store2 = new LogbackFileStoreWriter(type2, mapper, config, bus);
		LocalFileStoreReader reader = new LocalFileStoreReader(mapper, type2, config);
		
		List<String> testData = new ArrayList<>();
		for (int i = 0; i < ENTRIES; i++) {
			testData.add("testdata" + i);
		}
		for (Iterator<String> iterator = testData.iterator(); iterator.hasNext();) {
			String dataForStore1 = iterator.next();
			store1.append(dataForStore1);
			assertEquals(reader.peek().get().getData(), dataForStore1);
			String dataForStore2 = iterator.next();
			store2.append(dataForStore2);
			assertEquals(reader.peek().get().getData(), dataForStore2);
		}
		assertLogFileExistsFor(reader);
		List<Event> topEvents = reader.top(ENTRIES);
		assertEquals(ENTRIES, topEvents.size());
		List<String> topEventData = topEvents.stream().map(Event::getData).collect(Collectors.toList());
		Collections.reverse(topEventData);
		assertEquals(testData, topEventData);
		reader.verify(ENTRIES);
	}
	
	@Test
    void append1000Events() {
		EventType type = new EventType("1000Events");
		LogbackFileStoreWriter store = new LogbackFileStoreWriter(type, mapper, config, bus);
		FileStoreReader reader = new LocalFileStoreReader(mapper, type, config);
		for (int i = 0; i < 1000; i++) {
			store.append("dummy" + i);
		}
		List<Event> top = reader.top(1000);
		assertEquals(1000, top.size());
		Optional<Event> previous = Optional.empty();
		for (Event evt: top) {
			int i = top.indexOf(evt);
			assertEquals("dummy" + (1000 -1 - i), evt.getData());
			LocalDateTime previousDate = previous.map(Event::getCreated).orElse(evt.getCreated());
			assertFalse(previousDate.isBefore(evt.getCreated()), 
					String.format("Event %d has timestamp %s before %s", i, TEST_OUTPUT_FORMAT.format(evt.getCreated()), TEST_OUTPUT_FORMAT.format(previousDate)));
			previous = Optional.of(evt);
			
		}
	}
    
	
	
	@Test
	void peekFileNotExists() {
		EventType type1 = new EventType("peekNoFile");
		FileStoreReader reader = new LocalFileStoreReader(mapper, type1, config);
		new LogbackFileStoreWriter(type1, mapper, config, bus);
		assertFalse(reader.peek().isPresent(), "File should not exist");
	}
	
	@Test
	void peekEmpty() throws IOException {
		EventType type1 = new EventType("peekEmpty");
		new LogbackFileStoreWriter(type1, mapper, config, bus);
		LocalFileStoreReader reader = new LocalFileStoreReader(mapper, type1, config);
		try {
			Files.createFile(reader.meta.fileStorePath());
			assertFalse(reader.peek().isPresent(), "File should be empty");
		} finally {
			if (Files.exists(reader.meta.fileStorePath())) {
				Files.delete(reader.meta.fileStorePath());
			}
		}
	}
	
	@Test
	void peekSingleEvent() {
		EventType type1 = new EventType("peekSingle");
		LogbackFileStoreWriter store = new LogbackFileStoreWriter(type1, mapper, config, bus);
		LocalFileStoreReader reader = new LocalFileStoreReader(mapper, type1, config);
		Event event = store.append("dummy");
		assertLogFileExistsFor(reader);
		Optional<Event> peek = reader.peek();
		assertEquals("dummy", peek.map(Event::getData).orElse(""));
		assertEquals(event.getCreated(), peek.map(Event::getCreated).orElse(null));
	}
	
	@Test
	void peekTwoEvents() {
		EventType type1 = new EventType("peekTwo");
		FileStoreWriter store = new LogbackFileStoreWriter(type1, mapper, config, bus);
		LocalFileStoreReader reader = new LocalFileStoreReader(mapper, type1, config);
		Event event1 = store.append("dummy1");
		Optional<Event> peek1 = reader.peek();
		assertEquals("dummy1", peek1.map(Event::getData).orElse(""));
		assertEquals(event1.getCreated(), peek1.map(Event::getCreated).orElse(null));
		Event event2 = store.append("dummy2");
		Optional<Event> peek2 = reader.peek();
		assertEquals("dummy2", peek2.map(Event::getData).orElse(""));
		assertEquals(event2.getCreated(), peek2.map(Event::getCreated).orElse(null));
		//assertEquals(peek1.map(Event::getId), peek2.flatMap(Event::getPredecessor));
		
	}
	
	@Test
	void peekThreeEvents() {
		EventType type1 = new EventType("peekThree");
		LogbackFileStoreWriter store = new LogbackFileStoreWriter(type1, mapper, config, bus);
		LocalFileStoreReader reader = new LocalFileStoreReader(mapper, type1, config);
		store.append("dummy1");
		Optional<Event> peek1 = reader.peek();
		assertEquals("dummy1", peek1.map(Event::getData).orElse(""));
		store.append("dummy2");
		Optional<Event> peek2 = reader.peek();
		assertEquals("dummy2", peek2.map(Event::getData).orElse(""));
		//assertEquals(peek1.map(Event::getId), peek2.flatMap(Event::getPredecessor));
		store.append("dummy3");
		Optional<Event> peek3 = reader.peek();
		assertEquals("dummy3", peek3.map(Event::getData).orElse(""));
		//assertEquals(peek2.map(Event::getId), peek3.flatMap(Event::getPredecessor));
		assertLogFileExistsFor(reader);
	}
	
	@Test
	void topThreeEventsOfThree() {
		EventType type1 = new EventType("topThreeOfThree");
		FileStoreWriter store = new LogbackFileStoreWriter(type1, mapper, config, bus);
		LocalFileStoreReader reader = new LocalFileStoreReader(mapper, type1, config);
		store.append("dummy1");
		store.append("dummy2");
		store.append("dummy3");
		List<Event> top3 = reader.top(3);
		assertEquals(3, top3.size());
		assertEquals("dummy3",top3.get(0).getData());
		assertEquals("dummy2",top3.get(1).getData());
		assertEquals("dummy1",top3.get(2).getData());
	}
	
	@Test
	void topRolling1File() throws StreamWriteException, DatabindException, IOException, InterruptedException {
		EventType type = new EventType("topRolling1File");
		LogbackFileStoreWriter store = new LogbackFileStoreWriter(type, mapper, config, bus);
		LocalFileStoreReader reader = new LocalFileStoreReader(mapper, type, config);
		Event event4 = store.append("dummy4");
		Path archiveFile = createArchive(reader, LocalDateTime.now());
		Event event1 = new Event(type, "dummy1", Optional.empty());
		Event event2 = new Event(type, "dummy2", Optional.empty());
		Event event3 = new Event(type, "dummy3", Optional.empty());
		String archiveContent = mapper.writeValueAsString(event1) + "\n" + mapper.writeValueAsString(event2) + "\n" + mapper.writeValueAsString(event3);
		Files.write(archiveFile, archiveContent.getBytes());
		assertEquals(event4.getData(), reader.top(1).get(0).getData());
		List<Event> events = reader.top(3);
		for (int i = 4; i > 1; i--) {
			assertEquals("dummy" + i, events.get(4 - i).getData());
		}
		events = reader.top(4);
		for (int i = 4; i > 0; i--) {
			assertEquals("dummy" + i, events.get(4 - i).getData());
		}
	}
	
	@Test
	void topRolling2File() throws StreamWriteException, DatabindException, IOException, InterruptedException {
		EventType type = new EventType("topRolling2File");
		LogbackFileStoreWriter store = new LogbackFileStoreWriter(type, mapper, config, bus);
		LocalFileStoreReader reader = new LocalFileStoreReader(mapper, type, config);
		Event event7 = store.append("dummy7");
		Path archiveFileNow = createArchive(reader, LocalDateTime.now());
		Event event4 = new Event(type, "dummy4", Optional.empty());
		Event event5 = new Event(type, "dummy5", Optional.empty());
		Event event6 = new Event(type, "dummy6", Optional.empty());
		String archiveContent = mapper.writeValueAsString(event4) + "\n" + mapper.writeValueAsString(event5) + "\n" + mapper.writeValueAsString(event6);
		Files.write(archiveFileNow, archiveContent.getBytes());
		Path archiveFileYesterday = createArchive(reader, LocalDateTime.now().minusDays(1));
		Event event1 = new Event(type, "dummy1", Optional.empty());
		Event event2 = new Event(type, "dummy2", Optional.empty());
		Event event3 = new Event(type, "dummy3", Optional.empty());
		archiveContent = mapper.writeValueAsString(event1) + "\n" + mapper.writeValueAsString(event2) + "\n" + mapper.writeValueAsString(event3);
		Files.write(archiveFileYesterday, archiveContent.getBytes());
		assertEquals(event7.getData(), reader.top(1).get(0).getData());
		List<Event> events = reader.top(3);
		assertEquals(3, events.size());
		for (int i = 7; i > 4; i--) {
			assertEquals("dummy" + i, events.get(7 - i).getData());
		}
		events = reader.top(4);
		assertEquals(4, events.size());
		for (int i = 7; i > 3; i--) {
			assertEquals("dummy" + i, events.get(7 - i).getData());
		}
		events = reader.top(5);
		assertEquals(5, events.size());
		for (int i = 7; i > 2; i--) {
			assertEquals("dummy" + i, events.get(7 - i).getData());
		}
	}
	
	private Path createArchive(LocalFileStoreReader reader, LocalDateTime dateTime) throws IOException {
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(LOG_ARCHIVE_PATTERN);
		if (!Files.exists(reader.meta.getArchiveFolder())) {
			Files.createDirectory(reader.meta.getArchiveFolder());
		}
		Path archiveFile = reader.meta.getArchiveFolder().resolve(reader.getType() + timeFormatter.format(dateTime) + ".log");
		if (!Files.exists(archiveFile)) {			
			Files.createFile(archiveFile);
		}
		return archiveFile;
	}
	
	
	
	private void assertLogFileExistsFor(LocalFileStoreReader store) {
		Path path = store.meta.fileStorePath();
		assertTrue(Files.exists(path), "Expected logfile does not exist at " + path);
	}
}
