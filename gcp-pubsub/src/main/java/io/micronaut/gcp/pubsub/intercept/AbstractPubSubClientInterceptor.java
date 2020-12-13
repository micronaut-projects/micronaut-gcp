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

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.PublisherInterface;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.pubsub.annotation.Topic;
import io.micronaut.gcp.pubsub.exception.PubSubClientException;
import io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDes;
import io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDesRegistry;
import io.micronaut.gcp.pubsub.support.PubSubPublisherState;
import io.micronaut.gcp.pubsub.support.PublisherFactory;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.messaging.annotation.Header;
import io.reactivex.Scheduler;
import io.reactivex.Single;

import javax.annotation.PreDestroy;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains common code for intercepting PubSubClient calls. Concrete implementations must return a PublisherState with proper configuration
 * and corresponding {@link com.google.cloud.pubsub.v1.PublisherInterface}. This class deals with Conversions, Binders, and Content Type handling.
 *
 * @since 3.5.0
 * @author Vinicius Carvalho
 */
public abstract class AbstractPubSubClientInterceptor implements MethodInterceptor<Object, Object>, AutoCloseable {

    protected final ConcurrentHashMap<ExecutableMethod, PubSubPublisherState> publisherStateCache = new ConcurrentHashMap<>();
    protected final PublisherFactory publisherFactory;
    protected final PubSubMessageSerDesRegistry serDesRegistry;
    protected final Scheduler scheduler;
    protected final ConversionService<?> conversionService;
    protected final GoogleCloudConfiguration googleCloudConfiguration;

    public AbstractPubSubClientInterceptor(PublisherFactory publisherFactory,
                                           PubSubMessageSerDesRegistry serDesRegistry,
                                           Scheduler scheduler,
                                           ConversionService<?> conversionService,
                                           GoogleCloudConfiguration googleCloudConfiguration) {
        this.publisherFactory = publisherFactory;
        this.serDesRegistry = serDesRegistry;
        this.scheduler = scheduler;
        this.conversionService = conversionService;
        this.googleCloudConfiguration = googleCloudConfiguration;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {

        if (context.hasAnnotation(Topic.class)) {
            PubSubPublisherState publisherState = publisherStateCache.computeIfAbsent(context.getExecutableMethod(), method -> createState(method));
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

            PublisherInterface publisher = publisherState.getPublisher();
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
        }
        else {
            return context.proceed();
        }
    }

    @Override
    public void close() throws Exception {
        doClose();
    }

    protected Map.Entry<String, String> getNameAndValue(Argument argument, AnnotationValue<?> annotationValue, Map<String, Object> parameterValues) {
        String argumentName = argument.getName();
        String name = annotationValue.stringValue("name").orElse(annotationValue.getValue(String.class).orElse(argumentName));
        String value = String.valueOf(parameterValues.get(argumentName));

        return new AbstractMap.SimpleEntry<>(name, value);
    }

    abstract PubSubPublisherState createState(ExecutableMethod method);

    @PreDestroy
    abstract void doClose() throws Exception;
}
