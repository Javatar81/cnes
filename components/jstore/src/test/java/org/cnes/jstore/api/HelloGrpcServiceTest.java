package org.cnes.jstore.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class HelloGrpcServiceTest {

    @GrpcClient
    FileStoreGrpc helloGrpc;

    @Test
    public void testHello() {
        Empty reply = helloGrpc
                .append(EventBodyGrpc.newBuilder().setPayload("Neo").build()).await().atMost(Duration.ofSeconds(5));
        assertNotNull(reply);
    }

}
