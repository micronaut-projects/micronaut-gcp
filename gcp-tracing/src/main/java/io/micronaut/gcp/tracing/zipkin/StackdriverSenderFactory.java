/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.gcp.tracing.zipkin;

import brave.propagation.Propagation;
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
import io.micronaut.gcp.UserAgentHeaderProvider;
import io.micronaut.gcp.condition.RequiresGoogleProjectId;
import io.micronaut.tracing.brave.AsyncReporterConfiguration;
import io.micronaut.tracing.brave.BraveTracerConfiguration;
import zipkin2.Span;
import zipkin2.propagation.stackdriver.StackdriverTracePropagation;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.stackdriver.StackdriverEncoder;
import zipkin2.reporter.stackdriver.StackdriverSender;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;
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
@Requires(property = "gcp.trace.enabled", value = StringUtils.TRUE, defaultValue = StringUtils.TRUE)
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
     * @return A GRPC channel to use to send traces.
     */
    @Singleton
    @Bean(preDestroy = "shutdownNow")
    @Named("stackdriverTraceSenderChannel")
    protected @Nonnull ManagedChannel stackdriverTraceSenderChannel() {
        UserAgentHeaderProvider userAgentHeaderProvider = new UserAgentHeaderProvider("trace");

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
    protected @Nonnull Sender stackdriverSender(
            @Nonnull GoogleCloudConfiguration cloudConfiguration,
            @Nonnull GoogleCredentials credentials,
            @Nonnull @Named("stackdriverTraceSenderChannel") ManagedChannel channel) {

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
    protected @Nonnull BeanCreatedEventListener<BraveTracerConfiguration> braveTracerConfigurationBeanCreatedEventListener() {
        return (configuration) -> {
            BraveTracerConfiguration configurationBean = configuration.getBean();

            configurationBean.getTracingBuilder()
                    .traceId128Bit(true)
                    .supportsJoin(false);

            return configurationBean;
        };
    }

    /**
     * The {@link StackdriverTracePropagation#FACTORY} as a bean.
     * @return The bean.
     */
    @Singleton
    @Requires(beans = StackdriverSender.class)
    protected Propagation.Factory stackdriverPropagation() {
        return StackdriverTracePropagation.FACTORY;
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
    public AsyncReporter<Span> stackdriverReporter(@Nonnull AsyncReporterConfiguration configuration) {
        return configuration.getBuilder()
                .build(StackdriverEncoder.V2);
    }
}
