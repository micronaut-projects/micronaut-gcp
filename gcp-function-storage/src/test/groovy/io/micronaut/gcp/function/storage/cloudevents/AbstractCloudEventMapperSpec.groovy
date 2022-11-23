package io.micronaut.gcp.function.storage.cloudevents

import io.cloudevents.core.v1.CloudEventBuilder
import io.micronaut.gcp.function.storage.cloudevents.test.Foobar

import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import java.time.OffsetDateTime

/**
 * @author Guillermo Calvo
 * @since XXXX
 */
@MicronautTest
class AbstractCloudEventMapperSpec extends Specification {

    @Inject
    ObjectMapper objectMapper

    void "map cloud event to foobar"() {
        given:
        final event = CloudEventBuilder.v1()
                .withId('123')
                .withSource(URI.create('//storage.googleapis.com/projects/_/buckets/my-bucket'))
                .withType('google.cloud.storage.object.v1.finalized')
                .withData('application/json', '{ "foo": "FOO", "bar": 123 }'.bytes)
                .withSubject('objects/subject')
                .withTime(OffsetDateTime.now())
                .build()
        final mapper = new AbstractCloudEventMapper<Foobar>(Foobar.class, objectMapper) {}

        when:
        final result = mapper.map(event)

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
        final mapper = new AbstractCloudEventMapper<Foobar>(Foobar.class, objectMapper) {}

        when:
        final result = mapper.map(event)

        then:
        !result.present
    }
}
