package io.micronaut.gcp.function.http;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;

import java.io.BufferedWriter;
import java.io.IOException;

@Controller("/parameters")
public class ParametersController {

    @Get("/uri/{name}")
    String uriParam(String name) {
        return "Hello " + name;
    }

    @Get("/query")
    String queryValue(@QueryValue("q") String name) {
        return "Hello " + name;
    }

    @Get("/header")
    String headerValue(@Header(HttpHeaders.CONTENT_TYPE) String contentType) {
        return "Hello " + contentType;
    }

    @Get("/reqAndRes")
    void requestAndResponse(HttpRequest request, HttpResponse response) throws IOException {
        response.appendHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
        response.setStatusCode(HttpStatus.ACCEPTED.getCode());
        try (final BufferedWriter writer = response.getWriter()) {
            writer.append("Good");
            writer.flush();
        }
    }

    @Post("/stringBody")
    @Consumes("text/plain")
    String stringBody(@Body String body) {
        return "Hello " + body;
    }

    @Post("/bytesBody")
    @Consumes("text/plain")
    String bytesBody(@Body byte[] body) {
        return "Hello " + new String(body);
    }

    @Post(value = "/jsonBody", processes = "application/json")
    SimplePojo jsonBody(@Body SimplePojo body) {
        return body;
    }

    @Post(value = "/jsonBodySpread", processes = "application/json")
    SimplePojo jsonBody(String name) {
        return new SimplePojo(name);
    }
}
