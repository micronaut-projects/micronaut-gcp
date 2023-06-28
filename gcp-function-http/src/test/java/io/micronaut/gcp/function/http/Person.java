package io.micronaut.gcp.function.http;

import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class Person {
    private String name;
    private int age = 18;

    public Person(String name) {
        this.name = name;
    }

    @Creator
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
