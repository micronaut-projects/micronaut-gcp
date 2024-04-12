/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.gcp.tracing.zipkin;

import brave.propagation.B3Propagation;
import brave.propagation.Propagation;
import brave.propagation.stackdriver.StackdriverTracePropagation;
import com.google.auth.oauth2.GoogleCredentials;
import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.auth.MoreCallCredentials;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.util.StringUtils;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.Modules;
import io.micronaut.gcp.UserAgentHeaderProvider;
import io.micronaut.gcp.condition.RequiresGoogleProjectId;
import io.micronaut.tracing.brave.AsyncReporterConfiguration;
import io.micronaut.tracing.brave.BraveTracerConfiguration;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.BytesEncoder;
import zipkin2.reporter.Sender;
import zipkin2.reporter.stackdriver.StackdriverEncoder;
import zipkin2.reporter.stackdriver.StackdriverSender;

import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.net.URI;
import java.util.Arrays;

/**
 * Configures the {@link StackdriverSender} for Micronaut if present on the classpath.
 *
 * @author graemerocher
 * @author Ray Tsang
 * @since 1.0
 */
@Factory
@Requires(classes = StackdriverSender.class)
@Requires(property = StackdriverSenderFactory.PROPERTY_ENABLED, value = StringUtils.TRUE, defaultValue = StringUtils.TRUE)
public class StackdriverSenderFactory {

    /**
     * The trace scope to use.
     */
    public static final URI TRACE_SCOPE = URI.create("https://www.googleapis.com/auth/trace.append");

    /**
     * The trace target to use.
     */
    public static final String TRACE_TARGET = "cloudtrace.googleapis.com";

    /**
     * The property used to enable tracing.
     */
    public static final String PROPERTY_ENABLED = "gcp.tracing.enabled";

    /**
     * @return A GRPC channel to use to send traces.
     */
    @Singleton
    @Bean(preDestroy = "shutdownNow")
    @Named("stackdriverTraceSenderChannel")
    protected @NonNull ManagedChannel stackdriverTraceSenderChannel() {
        UserAgentHeaderProvider userAgentHeaderProvider = new UserAgentHeaderProvider(Modules.TRACING);

        return ManagedChannelBuilder.forTarget(TRACE_TARGET)
                .userAgent(userAgentHeaderProvider.getUserAgent())
                .build();
    }

    /**
     * The {@link StackdriverSender} bean.
     * @param cloudConfiguration The google cloud configuration
     * @param credentials The credentials
     * @param channel The channel to use
     * @return The sender
     */
    @RequiresGoogleProjectId
    @Requires(classes = StackdriverSender.class)
    @Singleton
    protected @NonNull Sender stackdriverSender(
            @NonNull GoogleCloudConfiguration cloudConfiguration,
            @NonNull GoogleCredentials credentials,
            @NonNull @Named("stackdriverTraceSenderChannel") ManagedChannel channel) {

        GoogleCredentials traceCredentials = credentials.createScoped(Arrays.asList(TRACE_SCOPE.toString()));

        return StackdriverSender.newBuilder(channel)
                .projectId(cloudConfiguration.getProjectId())
                .callOptions(CallOptions.DEFAULT
                        .withCallCredentials(MoreCallCredentials.from(traceCredentials)))
                .build();
    }

    /**
     * A {@link BeanCreatedEventListener} that modifies the brave trace configuration for Stackdriver compatibility.
     * @return The {@link BeanCreatedEventListener}
     */
    @Singleton
    @Requires(classes = StackdriverSender.class)
    protected @NonNull BeanCreatedEventListener<BraveTracerConfiguration> braveTracerConfigurationBeanCreatedEventListener() {
        return (configuration) -> {
            BraveTracerConfiguration configurationBean = configuration.getBean();

            configurationBean.getTracingBuilder()
                    .propagationFactory(StackdriverTracePropagation.newFactory(B3Propagation.FACTORY))
                    .traceId128Bit(true)
                    .supportsJoin(false);

            return configurationBean;
        };
    }

    /**
     * The {@link Propagation.Factory} as a bean.
     * @return The bean.
     */
    @Singleton
    @Requires(beans = StackdriverSender.class)
    protected Propagation.Factory stackdriverPropagation() {
        return StackdriverTracePropagation.newFactory(B3Propagation.FACTORY);
    }

    /**
     * A custom {@link AsyncReporter} that uses {@link StackdriverEncoder#V2}.
     *
     * @param configuration The configuration
     * @return The bean.
     */
    @Singleton
    @Requires(classes = StackdriverSender.class)
    @Requires(beans = AsyncReporterConfiguration.class)
    public AsyncReporter<Span> stackdriverReporter(@NonNull AsyncReporterConfiguration configuration) {
        return configuration.getBuilder()
                .build((BytesEncoder<Span>) StackdriverEncoder.V2);
    }
}
