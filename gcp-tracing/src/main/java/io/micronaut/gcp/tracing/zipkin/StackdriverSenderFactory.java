package io.micronaut.gcp.tracing.zipkin;

import brave.propagation.Propagation;
import com.google.auth.oauth2.GoogleCredentials;
import io.grpc.CallOptions;
import io.grpc.auth.MoreCallCredentials;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.condition.RequiresGoogleProjectId;
import zipkin2.propagation.stackdriver.StackdriverTracePropagation;
import zipkin2.reporter.Sender;
import zipkin2.reporter.stackdriver.StackdriverSender;

import javax.inject.Singleton;

@Factory
@Requires(classes = StackdriverSender.class)
@Requires(env = Environment.GOOGLE_COMPUTE)
public class StackdriverSenderFactory {


    @Requires(classes = StackdriverSender.class)
    @Requires(env = Environment.GOOGLE_COMPUTE)
    @Requires(beans = GoogleCredentials.class)
    @RequiresGoogleProjectId
    @Singleton
    protected StackdriverSender stackdriverSender(
            GoogleCloudConfiguration cloudConfiguration,
            GoogleCredentials googleCredentials) {
        final StackdriverSender.Builder builder = StackdriverSender.newBuilder();
        return builder.projectId(cloudConfiguration.getProjectId())
               .callOptions(CallOptions.DEFAULT.withCallCredentials(MoreCallCredentials.from(googleCredentials)))
               .build();
    }

    @Singleton
    @Requires(beans = StackdriverSender.class)
    protected Propagation.Factory stackdriverPropagation() {
        return StackdriverTracePropagation.FACTORY;
    }
}
