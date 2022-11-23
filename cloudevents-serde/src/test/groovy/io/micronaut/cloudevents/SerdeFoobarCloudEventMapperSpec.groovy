package io.micronaut.cloudevents

import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import io.micronaut.core.annotation.Introspected
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.serde.ObjectMapper

import jakarta.inject.Inject
import spock.lang.Specification

import java.time.OffsetDateTime
@MicronautTest
class SerdeFoobarCloudEventMapperSpec extends Specification {

    @Inject
    ObjectMapper objectMapper

    void "map cloud event to foobar"() {
        given:
        CloudEvent event = CloudEventBuilder.v1()
                .withId('123')
                .withSource(URI.create('//storage.googleapis.com/projects/_/buckets/my-bucket'))
                .withType('google.cloud.storage.object.v1.finalized')
                .withData('application/json', '{ "foo": "FOO", "bar": 123 }'.bytes)
                .withSubject('objects/subject')
                .withTime(OffsetDateTime.now())
                .build()
        CloudEventMapper mapper = new SerdeCloudEventMapper(objectMapper)

        when:
        Optional<Foobar> result = mapper.map(event, Foobar)

        then:
        result.present
        with(result.get()) {
            foo == 'FOO'
            bar == 123
        }
    }

    void "handle deserialization error"() {
        given:
        final event = CloudEventBuilder.v1()
                .withId('123')
                .withSource(URI.create('//storage.googleapis.com/projects/_/buckets/my-bucket'))
                .withType('google.cloud.storage.object.v1.finalized')
                .withData('application/json', '{ "foo": "FOO", "bar": "OOPS" }'.bytes)
                .withSubject('objects/subject')
                .withTime(OffsetDateTime.now())
                .build()
        CloudEventMapper mapper = new SerdeCloudEventMapper(objectMapper)

        when:
        Optional<Foobar> result = mapper.map(event, Foobar)

        then:
        !result.present
    }

    @Introspected
    static class Foobar {
        String foo
        Integer bar
    }
}
