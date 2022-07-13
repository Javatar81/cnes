package org.cnes.jstore.api;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class FileStoreResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/store/mystore")
          .then()
             .statusCode(200);
    }

}