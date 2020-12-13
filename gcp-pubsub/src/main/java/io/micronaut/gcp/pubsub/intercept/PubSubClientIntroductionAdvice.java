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
package io.micronaut.gcp.pubsub.intercept;

import com.google.cloud.pubsub.v1.PublisherInterface;
import com.google.pubsub.v1.ProjectTopicName;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.pubsub.annotation.OrderingKey;
import io.micronaut.gcp.pubsub.annotation.PubSubClient;
import io.micronaut.gcp.pubsub.annotation.Topic;
import io.micronaut.gcp.pubsub.configuration.PubSubConfigurationProperties;
import io.micronaut.gcp.pubsub.exception.PubSubClientException;
import io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDesRegistry;
import io.micronaut.gcp.pubsub.support.PubSubPublisherState;
import io.micronaut.gcp.pubsub.support.PubSubTopicUtils;
import io.micronaut.gcp.pubsub.support.PublisherFactory;
import io.micronaut.gcp.pubsub.support.PublisherFactoryConfig;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.messaging.annotation.Body;
import io.micronaut.messaging.annotation.Header;
import io.micronaut.scheduling.TaskExecutors;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Implementation of {@link io.micronaut.gcp.pubsub.annotation.PubSubClient} advice annotation.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@Singleton
public class PubSubClientIntroductionAdvice extends AbstractPubSubClientInterceptor {

    private final Logger logger = LoggerFactory.getLogger(PubSubClientIntroductionAdvice.class);
    private final PubSubConfigurationProperties pubSubConfigurationProperties;

    public PubSubClientIntroductionAdvice(PublisherFactory publisherFactory,
                                          PubSubMessageSerDesRegistry serDesRegistry,
                                          @Named(TaskExecutors.IO) ExecutorService executorService,
                                          ConversionService<?> conversionService,
                                          GoogleCloudConfiguration googleCloudConfiguration,
                                          PubSubConfigurationProperties pubSubConfigurationProperties) {
        super(publisherFactory, serDesRegistry, Schedulers.from(executorService), conversionService, googleCloudConfiguration);

        this.pubSubConfigurationProperties = pubSubConfigurationProperties;
    }

    @Override
    PubSubPublisherState createState(ExecutableMethod method) {
        AnnotationValue<PubSubClient> client = method.findAnnotation(PubSubClient.class).orElseThrow(() -> new IllegalStateException("No @PubSubClient annotation present"));
        String projectId = client.stringValue().orElse(googleCloudConfiguration.getProjectId());
        AnnotationValue<Topic> topicAnnotation = method.findAnnotation(Topic.class).get();
        Optional<Argument> orderingArgument = Arrays.stream(method.getArguments()).filter(argument -> argument.getAnnotationMetadata().hasAnnotation(OrderingKey.class)).findFirst();
        String topic = topicAnnotation.stringValue().get();
        String endpoint = topicAnnotation.get("endpoint", String.class).orElse("");
        String configurationName = topicAnnotation.get("configuration", String.class).orElse("");
        String contentType = topicAnnotation.get("contentType", String.class).orElse("");
        ProjectTopicName projectTopicName = PubSubTopicUtils.toProjectTopicName(topic, projectId);
        Map<String, String> staticMessageAttributes = new HashMap<>();
        List<AnnotationValue<Header>> headerAnnotations = method.getAnnotationValuesByType(Header.class);
        headerAnnotations.forEach((header) -> {
            String name = header.stringValue("name").orElse(null);
            String value = header.stringValue().orElse(null);
            if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(value)) {
                staticMessageAttributes.put(name, value);
            }
        });
        Argument<?> bodyArgument = findBodyArgument(method)
                .orElseThrow(() -> new PubSubClientException("No valid message body argument found for method: " + method));

        PubSubPublisherState.TopicState topicState = new PubSubPublisherState.TopicState(contentType, projectTopicName, configurationName, endpoint, orderingArgument.isPresent());
        logger.debug("Created a new publisher[{}] for topic: {}", method.getName(), topic);
        PublisherInterface publisher = publisherFactory.createPublisher(new PublisherFactoryConfig(topicState, pubSubConfigurationProperties.getPublishingExecutor()));
        return new PubSubPublisherState(topicState, staticMessageAttributes, bodyArgument, publisher, orderingArgument);
    }

    @Override
    void doClose() throws Exception {
        for (PubSubPublisherState publisherState : publisherStateCache.values()) {
            publisherState.close();
        }
    }

    private Optional<Argument<?>> findBodyArgument(ExecutableMethod<?, ?> method) {
        return Optional.ofNullable(Arrays.stream(method.getArguments())
                .filter(argument -> argument.getAnnotationMetadata().hasAnnotation(Body.class))
                .findFirst()
                .orElseGet(
                        () -> Arrays.stream(method.getArguments())
                                .findFirst()
                                .orElse(null)
                )
        );
    }
}
