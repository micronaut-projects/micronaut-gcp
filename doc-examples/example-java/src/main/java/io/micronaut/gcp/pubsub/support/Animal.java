package io.micronaut.gcp.pubsub.support;

public class Animal {
    private String name;

    public Animal(String name) {
        this.name = name;
    }

    public Animal() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
