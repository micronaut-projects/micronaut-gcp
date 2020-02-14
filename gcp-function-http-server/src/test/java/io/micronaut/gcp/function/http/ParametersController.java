package io.micronaut.gcp.function.http;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/parameters")
public class ParametersController {

    @Get("/uri/{name}")
    String uriParam(String name) {
        return "Hello " + name;
    }


}
