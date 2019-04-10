package hello.world;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/hello")
public class HelloController {

    @Get("/{name}")
    public Greeting hello(String name) {
        Greeting greeting = new Greeting();
        greeting.setMessage("Hello " + name);
        return greeting;
    }
}