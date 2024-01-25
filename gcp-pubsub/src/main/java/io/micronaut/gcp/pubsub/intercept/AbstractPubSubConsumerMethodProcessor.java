/*
 * Copyright 2017-2024 original authors
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

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.context.BeanContext;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.*;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.bind.BoundExecutable;
import io.micronaut.core.bind.DefaultExecutableBinder;
import io.micronaut.core.bind.exceptions.UnsatisfiedArgumentException;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.StringUtils;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.pubsub.annotation.PubSubListener;
import io.micronaut.gcp.pubsub.bind.DefaultPubSubAcknowledgement;
import io.micronaut.gcp.pubsub.bind.PubSubBinderRegistry;
import io.micronaut.gcp.pubsub.bind.PubSubConsumerState;
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverException;
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverExceptionHandler;
import io.micronaut.gcp.pubsub.support.PubSubSubscriptionUtils;
import io.micronaut.http.MediaType;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.messaging.Acknowledgement;
import io.micronaut.messaging.exceptions.MessageListenerException;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Qualifier;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base implementation of {@link ExecutableMethodProcessor} that handles creation of a
 * {@link com.google.cloud.pubsub.v1.MessageReceiver} that subscribes to a PubSub subscription
 * and invoke methods annotated with a subscription annotation to be supplied by concrete subclasses.
 * <p>
 * There can be only one subscriber for any given subscription (in order to avoid issues with message
 * acknowledgement control). Having more than one method using the same subscription raises a {@link io.micronaut.gcp.pubsub.exception.PubSubListenerException}.
 *
 * @param <A> The subscription annotation
 *
 * @author Jeremy Grelle
 * @since 5.4.0
 */
@Internal
abstract class AbstractPubSubConsumerMethodProcessor<A extends Annotation> implements ExecutableMethodProcessor<A> {

    protected final BeanContext beanContext;
    protected final ConversionService conversionService;
    protected final GoogleCloudConfiguration googleCloudConfiguration;
    protected final PubSubBinderRegistry binderRegistry;
    protected final PubSubMessageReceiverExceptionHandler exceptionHandler;
    private final AtomicBoolean shutDownMode = new AtomicBoolean(false);
    private final Class<A> annotationType;
    private final Logger logger = LoggerFactory.getLogger(AbstractPubSubConsumerMethodProcessor.class);

    protected AbstractPubSubConsumerMethodProcessor(Class<A> annotationType, BeanContext beanContext, ConversionService conversionService, GoogleCloudConfiguration googleCloudConfiguration, PubSubBinderRegistry binderRegistry, PubSubMessageReceiverExceptionHandler exceptionHandler) {
        this.annotationType = annotationType;
        this.beanContext = beanContext;
        this.conversionService = conversionService;
        this.googleCloudConfiguration = googleCloudConfiguration;
        this.binderRegistry = binderRegistry;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        if (beanDefinition.hasDeclaredAnnotation(PubSubListener.class)) {
            AnnotationValue<A> subscriptionAnnotation = method.getAnnotation(annotationType);
            io.micronaut.context.Qualifier<Object> qualifier = beanDefinition
                    .getAnnotationTypeByStereotype(Qualifier.class)
                    .map(type -> Qualifiers.byAnnotation(beanDefinition, type))
                    .orElse(null);
            boolean isBlocking = beanDefinition.hasDeclaredAnnotation(Blocking.class) || method.hasDeclaredAnnotation(Blocking.class);
            boolean hasAckArg = Arrays.stream(method.getArguments())
                    .anyMatch(arg -> Acknowledgement.class.isAssignableFrom(arg.getType()));

            Class<Object> beanType = (Class<Object>) beanDefinition.getBeanType();
            Object bean = beanContext.findBean(beanType, qualifier)
                    .orElseThrow(() -> new MessageListenerException("Could not find the bean to execute the method " + method));

            if (subscriptionAnnotation != null) {
                DefaultExecutableBinder<PubSubConsumerState> binder = new DefaultExecutableBinder<>();
                String subscriptionName = subscriptionAnnotation.getRequiredValue(String.class);
                ProjectSubscriptionName projectSubscriptionName = PubSubSubscriptionUtils.toProjectSubscriptionName(subscriptionName, googleCloudConfiguration.getProjectId());
                String defaultContentType = subscriptionAnnotation.stringValue("contentType").orElse(MediaType.APPLICATION_JSON);
                String configuration = subscriptionAnnotation.stringValue("configuration").orElse("");
                MessageReceiver receiver = buildMessageReceiver(method, defaultContentType, projectSubscriptionName, hasAckArg, binder, bean, isBlocking);
                addSubscriber(projectSubscriptionName, receiver, configuration);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private MessageReceiver buildMessageReceiver(ExecutableMethod<?, ?> method,
                                                 String defaultContentType,
                                                 ProjectSubscriptionName projectSubscriptionName,
                                                 boolean hasAckArg,
                                                 DefaultExecutableBinder<PubSubConsumerState> binder,
                                                 Object bean,
                                                 boolean isBlocking) {
        return (PubsubMessage message, AckReplyConsumer ackReplyConsumer) -> {

            if (!doBeforeSubscriber(message, ackReplyConsumer)) {
                return;
            }

            String messageContentType = message.getAttributesMap().getOrDefault("Content-Type", "");
            String contentType = Optional.of(messageContentType)
                .filter(StringUtils::isNotEmpty)
                .orElse(defaultContentType);
            DefaultPubSubAcknowledgement pubSubAcknowledgement = new DefaultPubSubAcknowledgement(ackReplyConsumer);

            PubSubConsumerState consumerState = new PubSubConsumerState(message, ackReplyConsumer,
                projectSubscriptionName, contentType);
            boolean autoAcknowledge = !hasAckArg;
            try {
                BoundExecutable<Object, Object> executable = (BoundExecutable<Object, Object>) binder.bind(method, binderRegistry, consumerState);
                Flux<Object> resultPublisher = executeSubscriberMethod(executable, bean, isBlocking);
                resultPublisher.subscribe(data -> {
                    }, //no-op
                    ex -> handleException(new PubSubMessageReceiverException("Error handling message", ex, bean, consumerState, autoAcknowledge)),
                    autoAcknowledge ? pubSubAcknowledgement::ack : () -> this.verifyManualAcknowledgment(executable, method.getName()));
            } catch (UnsatisfiedArgumentException e) {
                handleException(new PubSubMessageReceiverException("Error binding message to the method", e, bean, consumerState, autoAcknowledge));
            } catch (Exception e) {
                handleException(new PubSubMessageReceiverException("Error handling message", e, bean, consumerState, autoAcknowledge));
            }
        };
    }

    /**
     * Enter shutdown mode.
     */
    @PreDestroy
    public final void shutDown() {
        shutDownMode.set(true);
    }

    /**
     * A hook to determine whether processing of the current message should proceed.
     *
     * @param message the pub sub message being processed
     * @param ackReplyConsumer the ack reply consumer for the message being processed
     * @return {@code true} to continue processing, or {@code false} to short circuit processing and nack the current message.
     */
    protected boolean doBeforeSubscriber(PubsubMessage message, AckReplyConsumer ackReplyConsumer) {
        return true;
    }

    /**
     * A hook for building an implementation specific subscriber, i.e. for Pull or Push messaging.
     *
     * @param projectSubscriptionName the subscription name
     * @param receiver the configured message receiver
     * @param configuration the optional name of the configured receiver
     */
    protected abstract void addSubscriber(@NonNull ProjectSubscriptionName projectSubscriptionName, @NonNull MessageReceiver receiver, @Nullable String configuration);

    /**
     *
     * @return whether shutdown has been initiated
     */
    protected boolean isShutDownInitiated() {
        return shutDownMode.get();
    }

    /**
     * Default handling logic for {@link PubSubMessageReceiverException}.
     *
     * @param ex the exception thrown during message processing
     */
    protected void handleException(PubSubMessageReceiverException ex) {
        if (ex.getListener() instanceof PubSubMessageReceiverExceptionHandler bean) {
            bean.handle(ex);
        } else {
            exceptionHandler.handle(ex);
        }
    }

    /**
     * Default execution logic for bound subscription methods.
     *
     * @param executable the bound executable subscription method
     * @param bean the bean PubSub listener bean
     * @param isBlocking whether the subscription method is blocking
     * @return a {@link Flux} that will complete after subscriber execution
     */
    @SuppressWarnings({"unchecked"})
    protected Flux<Object> executeSubscriberMethod(BoundExecutable<Object, Object> executable, Object bean, boolean isBlocking) {
        Object result = Objects.requireNonNull(executable).invoke(bean);
        if (!Publishers.isConvertibleToPublisher(result)) {
            return Flux.empty();
        }
        return Flux.from(Publishers.convertPublisher(conversionService, result, Publisher.class));
    }

    private void verifyManualAcknowledgment(BoundExecutable<Object, Object> executable, String methodName) {
        Optional<Object> boundAck = Arrays
            .stream(executable.getBoundArguments())
            .filter(o -> (o instanceof DefaultPubSubAcknowledgement))
            .findFirst();
        if (boundAck.isPresent()) {
            DefaultPubSubAcknowledgement manualAck = (DefaultPubSubAcknowledgement) boundAck.get();
            if (!manualAck.isClientAck()) {
                logger.warn("Method {} was executed and no message acknowledge detected. Did you forget to invoke ack()/nack()?", methodName);
            }
        }
    }
}
