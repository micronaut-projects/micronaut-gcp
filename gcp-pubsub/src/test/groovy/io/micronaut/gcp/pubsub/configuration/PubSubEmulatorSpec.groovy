package io.micronaut.gcp.pubsub.configuration

import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.api.gax.rpc.TransportChannelProvider
import io.micronaut.context.ApplicationContext
import io.micronaut.gcp.Modules
import io.micronaut.inject.qualifiers.Qualifiers
import spock.lang.Specification

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
