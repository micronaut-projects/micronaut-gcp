package io.micronaut.gcp.tracing.zipkin;

import brave.propagation.Propagation;
import com.google.api.gax.core.GoogleCredentialsProvider;
import io.grpc.CallOptions;
import io.grpc.auth.MoreCallCredentials;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.condition.RequiresGoogleProjectId;
import io.micronaut.tracing.brave.AsyncReporterConfiguration;
import zipkin2.Span;
import zipkin2.propagation.stackdriver.StackdriverTracePropagation;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.stackdriver.StackdriverEncoder;
import zipkin2.reporter.stackdriver.StackdriverSender;

import javax.inject.Singleton;
import java.io.IOException;

@Factory
@Requires(classes = StackdriverSender.class)
@Requires(env = Environment.GOOGLE_COMPUTE)
public class StackdriverSenderFactory {


    @Requires(classes = StackdriverSender.class)
    @RequiresGoogleProjectId
    @Requires(beans = GoogleCredentialsProvider.class)
    @Singleton
    protected Sender stackdriverSender(
            GoogleCloudConfiguration cloudConfiguration,
            GoogleCredentialsProvider credentialsProvider) throws IOException {

        return StackdriverSender.newBuilder()
                .projectId(cloudConfiguration.getProjectId())
                .callOptions(CallOptions.DEFAULT
                        .withCallCredentials(MoreCallCredentials.from(credentialsProvider.getCredentials())))
                .build();
    }

    @Singleton
    @Requires(beans = StackdriverSender.class)
    protected Propagation.Factory stackdriverPropagation() {
        return StackdriverTracePropagation.FACTORY;
    }

    @Singleton
    @Requires(classes = StackdriverSender.class)
    @Requires(beans = AsyncReporterConfiguration.class)
    public AsyncReporter<Span> stackdriverReporter(AsyncReporterConfiguration configuration) {
        return configuration.getBuilder()
                .build(StackdriverEncoder.V2);
    }
}
