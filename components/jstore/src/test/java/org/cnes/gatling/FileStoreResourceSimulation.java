package org.cnes.gatling;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.reachRps;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.cnes.jstore.test.data.DataFactory;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

public class FileStoreResourceSimulation extends Simulation {

	private DataFactory factory = new DataFactory();
	
	HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080/");
	
	Iterator<Map<String, Object>> feeder =
		    Stream.generate((Supplier<Map<String, Object>>) ()
		      -> Collections.singletonMap("payload", factory.generateMediumPayload())
		    ).iterator();
	ScenarioBuilder multiUsers = scenario("MultiUser")
			.feed(feeder)
			.exec(http("post event")
					.post("/store/" + getName())
					.body(StringBody("{ \"payload\": #{payload} }"))
					.check(status().is(204))
			);
	
	{
		setUp(multiUsers.injectOpen(constantUsersPerSec(100).during(Duration.ofMinutes(3))))
		  .throttle(
		    reachRps(200).in(10)).protocols(httpProtocol);
	}


	private static String getName() {
		return "perftest" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
	}
}
