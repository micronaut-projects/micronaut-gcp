package io.micronaut.gcp.function.http;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

@Controller("/simple")
public class SimpleController {

    @Produces("text/plain")
    @Get("/text")
    String text() {
        return "good";
    }

    @Produces("application/json")
    @Get("/simplePojo")
    Person simplePojo() {
        return new Person("good");
    }
}
