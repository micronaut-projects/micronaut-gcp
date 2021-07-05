package io.micronaut.gcp.pubsub.configuration

import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.api.gax.rpc.TransportChannelProvider
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.Modules
import io.micronaut.inject.qualifiers.Qualifiers
import spock.lang.Specification

import jakarta.inject.Singleton

class PubSubEmulatorSpec extends Specification{

    void "test environment without pubsub emulator"() {
        given:
        ApplicationContext ctx = ApplicationContext.run([
            'spec.name': 'PubSubEmulatorSpec'
        ])

        when:
        CredentialsProvider provider = ctx.getBean(CredentialsProvider, Qualifiers.byName(Modules.PUBSUB))
        TransportChannelProvider transportChannelProvider = ctx.getBean(TransportChannelProvider, Qualifiers.byName(Modules.PUBSUB))

        then:
        provider instanceof FixedCredentialsProvider
        transportChannelProvider instanceof InstantiatingGrpcChannelProvider
    }

    void "test environment with pubsub emulator"() {
        given:
        ApplicationContext ctx = ApplicationContext.run([
            'spec.name': 'PubSubEmulatorSpec',
            "pubsub.emulator.host" : "localhost:8085"
        ])

        when:
        CredentialsProvider provider = ctx.getBean(CredentialsProvider, Qualifiers.byName(Modules.PUBSUB))
        TransportChannelProvider transportChannelProvider = ctx.getBean(TransportChannelProvider, Qualifiers.byName(Modules.PUBSUB))

        then:
        provider instanceof NoCredentialsProvider
        transportChannelProvider instanceof FixedTransportChannelProvider
    }
}

@Factory
@Requires(property = 'spec.name', value = 'PubSubEmulatorSpec')
class EmptyCredentialsFactory {

    @Singleton
    @Replaces(GoogleCredentials)
    GoogleCredentials mockCredentials() {
        return GoogleCredentials.create(new AccessToken("", new Date()))
    }
}