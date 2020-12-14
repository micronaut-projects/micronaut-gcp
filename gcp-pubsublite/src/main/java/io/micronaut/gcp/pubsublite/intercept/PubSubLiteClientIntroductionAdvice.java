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
package io.micronaut.gcp.pubsublite.intercept;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsublite.TopicPath;
import com.google.cloud.pubsublite.cloudpubsub.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.core.util.StringUtils;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.pubsub.annotation.OrderingKey;
import io.micronaut.gcp.pubsub.exception.PubSubClientException;
import io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDes;
import io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDesRegistry;
import io.micronaut.gcp.pubsublite.annotation.LiteTopic;
import io.micronaut.gcp.pubsublite.annotation.PubSubLiteClient;
import io.micronaut.gcp.pubsublite.configuration.PubSubLiteConfigurationProperties;
import io.micronaut.gcp.pubsublite.support.LitePublisherFactory;
import io.micronaut.gcp.pubsublite.support.LitePublisherFactoryConfig;
import io.micronaut.gcp.pubsublite.support.PubSubLitePublisherState;
import io.micronaut.gcp.pubsublite.support.PubSubLiteTopicUtils;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.messaging.annotation.Body;
import io.micronaut.messaging.annotation.Header;
import io.reactivex.Single;

import javax.inject.Singleton;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link PubSubLiteClient} advice annotation.
 *
 * Based on {@link io.micronaut.gcp.pubsub.intercept.PubSubClientIntroductionAdvice}
 *
 * @author Jacob Mims
 * @since 2.2.0
 */
@Singleton
public class PubSubLiteClientIntroductionAdvice implements MethodInterceptor<Object, Object>  {

    private final ConcurrentHashMap<ExecutableMethod, PubSubLitePublisherState> publisherStateCache = new ConcurrentHashMap<>();
    private final LitePublisherFactory publisherFactory;
    private final PubSubMessageSerDesRegistry serDesRegistry;
    private final ConversionService<?> conversionService;
    private final GoogleCloudConfiguration googleCloudConfiguration;
    private final PubSubLiteConfigurationProperties pubSubConfigurationProperties;

    public PubSubLiteClientIntroductionAdvice(LitePublisherFactory publisherFactory,
                                              PubSubMessageSerDesRegistry serDesRegistry,
                                              ConversionService<?> conversionService,
                                              GoogleCloudConfiguration googleCloudConfiguration,
                                              PubSubLiteConfigurationProperties pubSubConfigurationProperties) {
        this.publisherFactory = publisherFactory;
        this.serDesRegistry = serDesRegistry;
        this.conversionService = conversionService;
        this.googleCloudConfiguration = googleCloudConfiguration;
        this.pubSubConfigurationProperties = pubSubConfigurationProperties;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {

        if (context.hasAnnotation(LiteTopic.class)) {
            PubSubLitePublisherState publisherState = publisherStateCache.computeIfAbsent(context.getExecutableMethod(), method -> {
                AnnotationValue<PubSubLiteClient> client = method.findAnnotation(PubSubLiteClient.class).orElseThrow(() -> new IllegalStateException("No @PubSubLiteClient annotation present"));
                AnnotationValue<LiteTopic> topicAnnotation = method.findAnnotation(LiteTopic.class).get();

                TopicPath topicPath;
                if (!topicAnnotation.stringValue().isPresent()) {
                    String projectId = googleCloudConfiguration.getProjectId();
                    long projectNumber = client.longValue("projectNumber").orElse(0);
                    String location = topicAnnotation.getRequiredValue("location", String.class);
                    String topicName = topicAnnotation.getRequiredValue("name", String.class);
                    topicPath = projectNumber > 0 ?
                            PubSubLiteTopicUtils.toPubsubLiteTopic(topicName, projectNumber, location) :
                            PubSubLiteTopicUtils.toPubsubLiteTopic(topicName, projectId, location);
                } else {
                    topicPath = TopicPath.parse(topicAnnotation.stringValue().get());
                }

                Optional<Argument> orderingArgument = Arrays.stream(method.getArguments()).filter(argument -> argument.getAnnotationMetadata().hasAnnotation(OrderingKey.class)).findFirst();
                String configurationName = topicAnnotation.get("configuration", String.class).orElse("");
                String contentType = topicAnnotation.get("contentType", String.class).orElse("");

                Map<String, String> staticMessageAttributes = new HashMap<>();
                List<AnnotationValue<Header>> headerAnnotations = context.getAnnotationValuesByType(Header.class);
                headerAnnotations.forEach((header) -> {
                    String name = header.stringValue("name").orElse(null);
                    String value = header.stringValue().orElse(null);
                    if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(value)) {
                        staticMessageAttributes.put(name, value);
                    }
                });
                Argument<?> bodyArgument = findBodyArgument(method)
                        .orElseThrow(() -> new PubSubClientException("No valid message body argument found for method: " + context.getExecutableMethod()));

                PubSubLitePublisherState.TopicState topicState = new PubSubLitePublisherState.TopicState(contentType, topicPath, configurationName);
                Publisher publisher = publisherFactory.createLitePublisher(new LitePublisherFactoryConfig(topicState));
                return new PubSubLitePublisherState(topicState, staticMessageAttributes, bodyArgument, publisher, orderingArgument);
            });

            Map<String, String> messageAttributes = new HashMap<>(publisherState.getStaticMessageAttributes());
            String contentType = publisherState.getTopicState().getContentType();
            Argument<?> bodyArgument = publisherState.getBodyArgument();
            Map<String, Object> parameterValues = context.getParameterValueMap();
            ReturnType<Object> returnType = context.getReturnType();
            Class<?> javaReturnType = returnType.getType();

            Argument[] arguments = context.getArguments();
            for (Argument arg : arguments) {
                AnnotationValue<Header> headerAnn = arg.getAnnotation(Header.class);
                if (headerAnn != null) {
                    Map.Entry<String, String> entry = getNameAndValue(arg, headerAnn, parameterValues);
                    messageAttributes.put(entry.getKey(), entry.getValue());
                }
            }

            Publisher publisher = publisherState.getPublisher();

            Object body = parameterValues.get(bodyArgument.getName());
            PubsubMessage pubsubMessage = null;
            if (body.getClass() == PubsubMessage.class) {
                pubsubMessage = (PubsubMessage) body;
            } else {
                //if target type is byte[] we bypass serdes completely
                byte[] serialized = null;
                if (body.getClass() == byte[].class) {
                    serialized = (byte[]) body;
                } else {
                    PubSubMessageSerDes serDes = serDesRegistry.find(contentType)
                            .orElseThrow(() -> new PubSubClientException("Could not locate a valid SerDes implementation for type: " + contentType));
                    serialized = serDes.serialize(body);
                }
                messageAttributes.put("Content-Type", contentType);
                PubsubMessage.Builder messageBuilder = PubsubMessage.newBuilder();
                messageBuilder.setData(ByteString.copyFrom(serialized))
                        .putAllAttributes(messageAttributes);
                if (publisherState.getOrderingArgument().isPresent()) {
                    String orderingKey = conversionService.convert(parameterValues.get(publisherState.getOrderingArgument().get().getName()), String.class)
                            .orElseThrow(() -> new PubSubClientException("Could not convert argument annotated with @OrderingKey to String type"));
                    messageBuilder.setOrderingKey(orderingKey);

                }
                pubsubMessage = messageBuilder.build();

            }
            ApiFuture<String> future = publisher.publish(pubsubMessage);
            Single<String> reactiveResult = Single.fromFuture(future);
            boolean isReactive = Publishers.isConvertibleToPublisher(javaReturnType);
            if (javaReturnType == void.class || javaReturnType == Void.class) {
                String result = reactiveResult.blockingGet();
                return null;
            } else {
                if (isReactive) {
                    return Publishers.convertPublisher(reactiveResult, javaReturnType);
                } else {
                    String result = reactiveResult.blockingGet();
                    return conversionService.convert(result, javaReturnType)
                            .orElseThrow(() -> new PubSubClientException("Could not convert publisher result to method return type: " + javaReturnType));
                }
            }
        } else {
            return context.proceed();
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

    private Map.Entry<String, String> getNameAndValue(Argument argument, AnnotationValue<?> annotationValue, Map<String, Object> parameterValues) {
        String argumentName = argument.getName();
        String name = annotationValue.stringValue("name").orElse(annotationValue.getValue(String.class).orElse(argumentName));
        String value = String.valueOf(parameterValues.get(argumentName));

        return new AbstractMap.SimpleEntry<>(name, value);
    }
}
