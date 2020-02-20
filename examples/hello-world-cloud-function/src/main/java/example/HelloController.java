package example;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

@Controller("/hello")
public class HelloController {

    @Get("/{name}")
    @Produces("text/plain")
    String hello(String name) {
        return "Hello " + name;
    }
}
