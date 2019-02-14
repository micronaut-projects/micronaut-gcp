package example;

import io.micronaut.tracing.annotation.NewSpan;

import javax.inject.Singleton;

@Singleton
public class AnotherService {
    @NewSpan("another-span")
    String go() {
        return "done!";
    }
}
