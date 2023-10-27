package io.micronaut.gcp.function.http;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.reactivex.rxjava3.core.Flowable;

@Controller("/reactive")
public class ReactiveController {

    @Post(value = "/jsonArray", processes = "application/json")
    Flowable<Person> jsonArray(@Body Flowable<Person> flowable) {
        return flowable;
    }
}
