package org.cnes.gatling;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.reachRps;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

import java.time.Duration;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

public class StoreResourceSimulation extends Simulation {

	HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080/");
	ScenarioBuilder multiUsers = scenario("MultiUser")
			.exec(http("stores endpoint").get("/store"));
	
	{
		setUp(multiUsers.injectOpen(constantUsersPerSec(100).during(Duration.ofMinutes(3))))
		  .throttle(
		    reachRps(200).in(10)).protocols(httpProtocol);
	}

}
