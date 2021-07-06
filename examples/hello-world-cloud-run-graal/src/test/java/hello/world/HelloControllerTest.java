package hello.world;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MicronautTest;

@MicronautTest
public class HelloControllerTest {

    @Inject
    @Client("/")
    RxHttpClient client;

    @Test
    public void testHello() throws Exception {
        assertEquals( "Hello John", 
                        client.retrieve(HttpRequest.GET("/hello/John"), Greeting.class).blockingFirst().getMessage());
    }
}
