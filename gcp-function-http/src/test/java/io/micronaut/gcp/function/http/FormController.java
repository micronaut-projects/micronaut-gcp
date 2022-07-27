package io.micronaut.gcp.function.http;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

import java.util.Collections;
import java.util.Map;

@Controller("/form")
public class FormController {
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post
    String save(@Body MessageCreate messageCreate) {
        return "{\"message\":\"Hello " + messageCreate.getMessage() + "\"}";
    }

    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/without-body-annotation")
    String withoutBodyAnnotation(MessageCreate messageCreate) {
        return "{\"message\":\"Hello " + messageCreate.getMessage() + "\"}";
    }

    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post("/nested-attribute")
    String save(@Body("message") String value) {
        return "{\"message\":\"Hello " + value + "\"}";
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Post("/json-nested-attribute")
    String jsonNestedAttribute(@Body("message") String value) {
        return "{\"message\":\"Hello " + value + "\"}";
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Post("/json-without-body-annotation")
    String jsonWithoutBody(MessageCreate messageCreate) {
        return "{\"message\":\"Hello " + messageCreate.getMessage() + "\"}";
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Post("/json-nested-attribute-with-map-return")
    Map<String, String> jsonNestedAttributeWithMapReturn(@Body("message") String value) {
        return Collections.singletonMap("message", "Hello " + value);
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Post("/json-with-body-annotation-and-with-object-return")
    MyResponse jsonNestedAttributeWithObjectReturn(@Body MessageCreate messageCreate) {
        return new MyResponse("Hello " + messageCreate.getMessage());
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Post("/json-with-body")
    String jsonWithBody(@Body MessageCreate messageCreate) {
        return "{\"message\":\"Hello " + messageCreate.getMessage() + "\"}";
    }
}
