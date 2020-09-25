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
import io.micronaut.gcp.Modules
import io.micronaut.inject.qualifiers.Qualifiers
import spock.lang.Specification
import spock.mock.MockingApi

import javax.inject.Singleton

class PubSubEmulatorSpec extends Specification{

    void "test environment without pubsub emulator"() {
        ApplicationContext ctx = ApplicationContext.run()
        CredentialsProvider provider = ctx.getBean(CredentialsProvider, Qualifiers.byName(Modules.PUBSUB))
        TransportChannelProvider transportChannelProvider = ctx.getBean(TransportChannelProvider, Qualifiers.byName(Modules.PUBSUB))
        expect:
            provider instanceof FixedCredentialsProvider
            transportChannelProvider instanceof InstantiatingGrpcChannelProvider
    }
    void "test environment with pubsub emulator"() {
        ApplicationContext ctx = ApplicationContext.run([
                "pubsub.emulator.host" : "localhost:8085"])
        CredentialsProvider provider = ctx.getBean(CredentialsProvider, Qualifiers.byName(Modules.PUBSUB))
        TransportChannelProvider transportChannelProvider = ctx.getBean(TransportChannelProvider, Qualifiers.byName(Modules.PUBSUB))

        expect:
            provider instanceof NoCredentialsProvider
            transportChannelProvider instanceof FixedTransportChannelProvider
    }
}

@Factory
class EmptyCredentialsFactory {

    @Singleton
    @Replaces(GoogleCredentials)
    GoogleCredentials mockCredentials() {
        return GoogleCredentials.create(new AccessToken("", new Date()))
    }
}