package io.micronaut.gcp.tracing.zipkin

import com.google.devtools.cloudtrace.v2.BatchWriteSpansRequest
import com.google.devtools.cloudtrace.v2.TraceServiceGrpc
import com.google.protobuf.Empty
import io.grpc.ManagedChannel
import io.grpc.Server
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.stub.StreamObserver
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.AccessToken
import jakarta.inject.Named
import jakarta.inject.Singleton
import spock.lang.Specification
import zipkin2.Span
import zipkin2.reporter.AsyncReporter
import zipkin2.reporter.Sender
import zipkin2.reporter.stackdriver.StackdriverSender
import zipkin2.reporter.stackdriver.zipkin.StackdriverEncoder
import io.grpc.CallOptions

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Integration test that verifies AsyncReporter sends a span via the StackdriverSender.
 * It boots a Micronaut context, replaces the ManagedChannel with an in-process gRPC server
 * that implements Cloud Trace v2 BatchWriteSpans, and asserts that a span was received.
 */
class StackdriverAsyncReporterIntegrationSpec extends Specification {

    static final String SERVER_NAME = "stackdriver-trace-test"

    Server server
    List<com.google.devtools.cloudtrace.v2.Span> received = Collections.synchronizedList(new ArrayList<>())
    CountDownLatch latch

    def setup() {
        def service = new TraceServiceGrpc.TraceServiceImplBase() {
            @Override
            void batchWriteSpans(BatchWriteSpansRequest request, StreamObserver<Empty> responseObserver) {
                received.addAll(request.getSpansList())
                if (latch != null) {
                    latch.countDown()
                }
                responseObserver.onNext(Empty.getDefaultInstance())
                responseObserver.onCompleted()
            }
        }
        server = InProcessServerBuilder.forName(SERVER_NAME)
                .addService(service)
                .build()
                .start()
    }

    def cleanup() {
        if (server != null) {
            server.shutdownNow()
        }
    }

    void "AsyncReporter sends a span to Stackdriver via in-process gRPC"() {
        given: "A Micronaut application context with project id, test credentials, and tracing enabled"
        ApplicationContext ctx = ApplicationContext.run([
                "gcp.project-id"                                    : "test-project",
                "tracing.zipkin.enabled"                            : true
        ])
        latch = new CountDownLatch(1)

        and: "An AsyncReporter configured with a StackdriverSender targeting the in-process channel"
        ManagedChannel channel = InProcessChannelBuilder.forName(SERVER_NAME).build()
        Sender sender = StackdriverSender.newBuilder(channel)
                .projectId("test-project")
                .callOptions(CallOptions.DEFAULT)
                .build()
        AsyncReporter<Span> reporter = AsyncReporter.builder(sender).build(StackdriverEncoder.V2)

        and: "A minimal Zipkin span"
        Span span = Span.newBuilder()
                .traceId("463ac35c9f6413ad48485a3953bb6124") // 128-bit trace id
                .id("a2fb4a1d1a96d312")
                .name("test-span")
                .timestamp(System.currentTimeMillis() * 1000) // microseconds
                .duration(2000) // microseconds
                .build()

        when: "Reporting the span and flushing"
        reporter.report(span)
        reporter.flush()

        then: "The in-process Cloud Trace server receives the BatchWriteSpans request containing exactly one span"
        assert latch.await(5, TimeUnit.SECONDS)
        assert received.size() == 1
        assert received.get(0).getName().contains("projects/test-project/")

        cleanup:
        reporter.close()
        ctx.close()
    }

    /**
     * Test factory that replaces GoogleCredentials with a static access token to avoid network calls.
     */
    @Factory
    static class TestCredentialsFactory {
        @Singleton
        @Replaces(GoogleCredentials)
        GoogleCredentials googleCredentials() {
            return GoogleCredentials.create(new AccessToken("test-token", new Date(System.currentTimeMillis() + 3600_000)));
        }
    }

    /**
     * Test factory that replaces the Stackdriver ManagedChannel bean with an in-process channel
     * pointing to the test gRPC server.
     */
    @Factory
    static class TestChannelFactory {
        @Singleton
        @Bean(preDestroy = "shutdownNow")
        @Named("stackdriverTraceSenderChannel")
        @Replaces(bean = ManagedChannel, named = "stackdriverTraceSenderChannel")
        ManagedChannel stackdriverTraceSenderChannel() {
            return InProcessChannelBuilder.forName(SERVER_NAME)
                    .build()
        }
    }
}
