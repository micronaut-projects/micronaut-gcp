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
import io.micronaut.context.annotation.Primary;
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

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

@Factory
@Requires(classes = StackdriverSender.class)
@Requires(property = "gcp.trace.enabled", value = StringUtils.TRUE, defaultValue = StringUtils.TRUE)
public class StackdriverSenderFactory {
    public static final URI TRACE_SCOPE = URI.create("https://www.googleapis.com/auth/trace.append");
    public static final String TRACE_TARGET = "cloudtrace.googleapis.com";

    @Singleton
    @Bean(preDestroy = "shutdownNow")
    @Named("stackdriverTraceSenderChannel")
    protected ManagedChannel stackdriverTraceSenderChannel() {
        UserAgentHeaderProvider userAgentHeaderProvider = new UserAgentHeaderProvider("trace");

        return ManagedChannelBuilder.forTarget(TRACE_TARGET)
                .userAgent(userAgentHeaderProvider.getUserAgent())
                .build();
    }

    @RequiresGoogleProjectId
    @Requires(classes = StackdriverSender.class)
    @Singleton
    protected Sender stackdriverSender(
            GoogleCloudConfiguration cloudConfiguration,
            GoogleCredentials credentials,
            @Named("stackdriverTraceSenderChannel") ManagedChannel channel) throws IOException {

        GoogleCredentials traceCredentials = credentials.createScoped(Arrays.asList(TRACE_SCOPE.toString()));

        return StackdriverSender.newBuilder(channel)
                .projectId(cloudConfiguration.getProjectId())
                .callOptions(CallOptions.DEFAULT
                        .withCallCredentials(MoreCallCredentials.from(traceCredentials)))
                .build();
    }

    @Singleton
    @Requires(classes = StackdriverSender.class)
    protected BeanCreatedEventListener<BraveTracerConfiguration> braveTracerConfigurationBeanCreatedEventListener() {
        return (configuration) -> {
            BraveTracerConfiguration configurationBean = configuration.getBean();

            configurationBean.getTracingBuilder()
                    .traceId128Bit(true)
                    .supportsJoin(false);

            return configurationBean;
        };
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
