/*
 * Copyright 2017-2026 original authors
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

import com.google.api.gax.rpc.FixedTransportChannelProvider;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.Qualifier;
import io.micronaut.context.WatchableBeanContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.env.DevelopmentActive;
import io.micronaut.context.event.BeanPreDestroyEvent;
import io.micronaut.context.event.BeanPreDestroyEventListener;
import io.micronaut.context.reload.ClassChange;
import io.micronaut.context.reload.ClassChangeEvent;
import io.micronaut.context.reload.ReloadStrategy;
import io.micronaut.context.watch.BeanDefinitionChange;
import io.micronaut.context.watch.BeanDefinitionWatcher;
import io.micronaut.context.watch.ClassChangeWatcher;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.order.Ordered;
import io.micronaut.gcp.pubsub.annotation.PubSubClient;
import io.micronaut.gcp.pubsub.annotation.PubSubListener;
import io.micronaut.gcp.pubsub.annotation.PushSubscription;
import io.micronaut.gcp.pubsub.annotation.Subscription;
import io.micronaut.gcp.pubsub.bind.PubSubArgumentBinder;
import io.micronaut.gcp.pubsub.bind.PubSubBinderRegistry;
import io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDes;
import io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDesRegistry;
import io.micronaut.gcp.pubsub.support.PubSubConfigurationFactory;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanDefinitionReference;
import io.micronaut.inject.BeanType;
import io.micronaut.inject.ExecutableMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Keeps the Pub/Sub subscribers and publishers in step with the code in development mode. It exists only in
 * development mode, so nothing of it is on the path of a message received or published.
 *
 * <ul>
 *     <li>A definition of a {@link PubSubListener} bean registered or removed, or a listener class changed in place,
 *     restarts the subscribers: the changed listener beans are recreated, then the pull and push consumer processors,
 *     which stop and remove the subscribers they added as they are destroyed.</li>
 *     <li>A {@link PubSubMessageSerDes} or {@link PubSubArgumentBinder} definition, or a {@link PubSubClient},
 *     registered or removed, a class of one of them, of a registry, or of a factory that produces one, changed in
 *     place, or a class change applied in place that retires a classloader, recreates the serializer registry, the
 *     binder registry and the client advice, whose publisher state is cached by method. The beans that received them,
 *     the clients and the consumer processors among them, are destroyed with them, as the dependency graph records, and
 *     the subscribers are restarted.</li>
 *     <li>A class change applied in place that touches none of these recreates nothing.</li>
 * </ul>
 *
 * <p>The context creates a recreated consumer processor, or one destroyed as a dependent of a recreated bean, again
 * at once, and gives it the subscription methods it gave it at startup, so that it subscribes again, on the
 * transport channel, credentials and executors that were there, on top of the new beans.</p>
 *
 * <p>A consumer processor removes its subscribers only in development mode: as it is destroyed, in place or with a
 * context, this reloader removes them, waiting until each pull subscriber has terminated. A message a stopped
 * subscriber had received is processed by the code that received it, or nacked when {@code gcp.pubsub.nack-on-shutdown}
 * is set, and then redelivered by Pub/Sub to the subscriber that replaces it; one it had not acknowledged when it
 * terminated is redelivered once its acknowledgement deadline expires. Outside development mode the subscribers
 * are stopped as before, with the subscriber factory.</p>
 *
 * <p>Across a restart, the transport channel provider, with the channel of the emulator, the credentials, the
 * executor services the publishers run on and the publishers are retained, until a change under {@code gcp}, of the
 * emulator or of the executors releases them. The subscribers are not: each generation subscribes again on the
 * retained channel and executors, after the stopped one removed its subscribers. As the retained channel of the
 * emulator is released, this reloader shuts it down.</p>
 *
 * <p>A change that restarts the application is ignored: the new context subscribes again. A processor has no way
 * to remove the subscribers of one bean, so a change of one listener restarts all of them. Each bean is recreated
 * through {@link WatchableBeanContext#recreate(Object)}; a context that does not track bean dependencies recreates
 * nothing, and the subscribers stay as they are until a restart.</p>
 *
 * <p>The watches run after those of other modules. The reloader holds the context only, never a Pub/Sub bean: a bean
 * that received one is a dependent of it, which recreating it would destroy along with its watches.</p>
 *
 * @author graemerocher
 * @since 6.3.0
 */
@Internal
@Context
@DevelopmentActive
final class DevelopmentPubSubReloader implements BeanPreDestroyEventListener<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(DevelopmentPubSubReloader.class);

    /**
     * The beans that cache what they were built from: the serializer and binder registries, which hold the beans
     * registered when they were created, and the client advice, which caches the publisher state by method.
     */
    private static final List<Class<?>> REGISTRIES = List.of(PubSubMessageSerDesRegistry.class, PubSubBinderRegistry.class, PubSubClientIntroductionAdvice.class);

    /**
     * The types of a class whose change makes the registries stale.
     */
    private static final List<Class<?>> REGISTERED_TYPES = List.of(PubSubMessageSerDes.class, PubSubMessageSerDesRegistry.class, PubSubArgumentBinder.class, PubSubBinderRegistry.class);

    /**
     * The stereotypes of a listener: {@link PubSubListener} on the class, a subscription on a method.
     */
    private static final List<Class<? extends Annotation>> LISTENER_STEREOTYPES = List.of(PubSubListener.class, Subscription.class, PushSubscription.class);

    /**
     * The stereotype of a client.
     */
    private static final List<Class<? extends Annotation>> CLIENTS = List.of(PubSubClient.class);

    /**
     * The listener beans: a listener stereotype on the class, or on an executable method.
     */
    private static final Qualifier<Object> LISTENERS = new ListenerQualifier();

    /**
     * The beans the registries are built from: the serializers, the binders, their registries and the clients.
     */
    private static final Qualifier<Object> REGISTERED = new RegisteredQualifier();

    private final BeanContext beanContext;

    /**
     * @param beanContext The context, watched when it can be
     */
    DevelopmentPubSubReloader(BeanContext beanContext) {
        this.beanContext = beanContext;
        if (beanContext instanceof WatchableBeanContext watchable) {
            watchable.definitions().qualifier(LISTENERS).watch(new ListenerDefinitionsWatcher());
            // one watch, so that a bean that is several of these, such as a serializer that is a binder too, restarts once
            watchable.definitions().qualifier(REGISTERED).watch(new RegistryDefinitionsWatcher());
            watchable.classChanges().watch(new ClassWatcher());
        }
    }

    /**
     * Removes the subscribers of a consumer processor as it is destroyed, recreated in place or stopped with its
     * context, before the processor that replaces it subscribes again, and shuts down the channel of the emulator as
     * its provider is destroyed, released or stopped with its context.
     *
     * @param event The event
     * @return The bean
     */
    @Override
    public Object onPreDestroy(BeanPreDestroyEvent<Object> event) {
        Object bean = event.getBean();
        if (bean instanceof AbstractPubSubConsumerMethodProcessor<?> processor) {
            processor.removeSubscribers();
        } else if (bean instanceof FixedTransportChannelProvider channelProvider && isEmulatorChannelProvider(event.getBeanDefinition())) {
            try {
                channelProvider.getTransportChannel().shutdown();
            } catch (Exception e) {
                LOG.warn("Failed to shut down the channel of the Pub/Sub emulator", e);
            }
        }
        return bean;
    }

    private static boolean isEmulatorChannelProvider(BeanDefinition<?> definition) {
        return definition.getDeclaringType().filter(type -> type == PubSubConfigurationFactory.class).isPresent();
    }

    private void onClassChange(ClassChangeEvent change) {
        if (change.strategy() == ReloadStrategy.RESTART) {
            // the new context subscribes again, and the processors of the one it replaces remove theirs as it stops
            return;
        }
        if (!change.retiredLoaders().isEmpty()) {
            // every class of the application is stale: the listeners and the registries hold instances of them
            List<String> stale = new ArrayList<>();
            for (BeanRegistration<?> registration : beanContext.getActiveBeanRegistrations(LISTENERS)) {
                if (change.isStaleDefinition(registration.getBeanDefinition()) || change.isStaleInstance(registration.bean())) {
                    stale.add(registration.getBeanDefinition().getBeanType().getName());
                }
            }
            restartSubscribers(true, stale, "a reload retired a classloader");
            return;
        }
        boolean registries = false;
        List<String> listeners = new ArrayList<>();
        for (ClassChange classChange : change.changes()) {
            String className = classChange.className();
            Class<?> type = load(className, change.newLoader());
            if (isListener(type) || wasCompiledWith(className, LISTENER_STEREOTYPES)) {
                listeners.add(className);
            }
            if (isRegisteredOrClient(type) || wasCompiledWith(className, CLIENTS) || wasRegistered(className)) {
                registries = true;
            }
        }
        // a change that touches none of these needs nothing: a module that recreated a bean a consumer processor
        // received destroyed the processor with it, and the context created it again and subscribed again
        if (registries || !listeners.isEmpty()) {
            restartSubscribers(registries, listeners, registries ? "a serializer, a binder or a Pub/Sub client changed" : listeners + " changed");
        }
    }

    private static Class<?> load(String className, ClassLoader loader) {
        try {
            return Class.forName(className, false, loader);
        } catch (ClassNotFoundException | LinkageError e) {
            // removed, or not loadable on its own: nothing of the new generation is built from it
            return null;
        }
    }

    private static boolean isListener(Class<?> type) {
        if (type == null) {
            return false;
        }
        if (hasAnnotation(type.getAnnotations(), LISTENER_STEREOTYPES)) {
            return true;
        }
        try {
            // the declared methods through the hierarchy: a subscription method need not be public
            for (Class<?> declaring = type; declaring != null && declaring != Object.class; declaring = declaring.getSuperclass()) {
                for (Method method : declaring.getDeclaredMethods()) {
                    if (hasAnnotation(method.getAnnotations(), LISTENER_STEREOTYPES)) {
                        return true;
                    }
                }
            }
        } catch (LinkageError e) {
            // a method signature that does not resolve: the class cannot be a working listener
        }
        return false;
    }

    private static boolean isRegisteredOrClient(Class<?> type) {
        if (type == null) {
            return false;
        }
        for (Class<?> registeredType : REGISTERED_TYPES) {
            if (registeredType.isAssignableFrom(type)) {
                return true;
            }
        }
        return hasAnnotation(type.getAnnotations(), CLIENTS);
    }

    /**
     * Whether one of the annotations is present, or is the stereotype of one that is.
     */
    private static boolean hasAnnotation(Annotation[] annotations, List<Class<? extends Annotation>> wanted) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            for (Class<? extends Annotation> candidate : wanted) {
                if (annotationType == candidate || annotationType.isAnnotationPresent(candidate)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Whether the class a change replaces carried one of the stereotypes as the context was compiled: a bean
     * definition of it, or of its proxy, has it on the class or an executable method. The references are matched by
     * name, so that only the definitions of that class are loaded, and nothing of a previous class is kept.
     *
     * @param className The changed class
     * @param stereotypes The stereotypes
     * @return Whether one was there
     */
    private boolean wasCompiledWith(String className, List<Class<? extends Annotation>> stereotypes) {
        for (BeanDefinitionReference<?> reference : definitionsOf(className)) {
            try {
                if (hasStereotype(reference.getAnnotationMetadata(), stereotypes)) {
                    return true;
                }
                BeanDefinition<?> definition = reference.load();
                if (definition == null) {
                    continue;
                }
                for (ExecutableMethod<?, ?> method : definition.getExecutableMethods()) {
                    if (hasStereotype(method.getAnnotationMetadata(), stereotypes)) {
                        return true;
                    }
                }
            } catch (RuntimeException | LinkageError e) {
                // a definition of that name that no longer loads: what it was is unknown, so it counts
                return true;
            }
        }
        return false;
    }

    private static boolean hasStereotype(AnnotationMetadata metadata, List<Class<? extends Annotation>> stereotypes) {
        for (Class<? extends Annotation> stereotype : stereotypes) {
            if (metadata.hasStereotype(stereotype)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether the class a change replaces was a serializer, a binder, a registry or a client bean as the context was
     * compiled, or a factory that produced one: a product of a factory is defined by {@code $Factory$...$Definition},
     * whose bean type is the product and whose declaring type is the factory.
     */
    private boolean wasRegistered(String className) {
        for (BeanDefinitionReference<?> reference : definitionsOf(className)) {
            try {
                if (isRegisteredBean(reference)) {
                    return true;
                }
            } catch (RuntimeException | LinkageError e) {
                return true;
            }
        }
        int lastDot = className.lastIndexOf('.');
        String products = className.substring(0, lastDot + 1) + '$' + className.substring(lastDot + 1) + '$';
        for (BeanDefinitionReference<?> reference : beanContext.getBeanDefinitionReferences()) {
            String name = reference.getBeanDefinitionName();
            if (!name.startsWith(products) || !name.endsWith("$Definition")) {
                continue;
            }
            try {
                if (!isRegisteredBean(reference)) {
                    continue;
                }
                // the name alone matches a nested class too: the declaring type tells the product of this factory
                BeanDefinition<?> definition = reference.load();
                if (definition == null || definition.getDeclaringType().map(Class::getName).filter(className::equals).isPresent()) {
                    return true;
                }
            } catch (RuntimeException | LinkageError e) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether a bean is one the registries are built from: a serializer, a binder, a registry of them, or a client.
     */
    private static boolean isRegisteredBean(BeanType<?> candidate) {
        Class<?> beanType = candidate.getBeanType();
        for (Class<?> registeredType : REGISTERED_TYPES) {
            if (registeredType.isAssignableFrom(beanType)) {
                return true;
            }
        }
        return hasStereotype(candidate.getAnnotationMetadata(), CLIENTS);
    }

    private List<BeanDefinitionReference<?>> definitionsOf(String className) {
        int lastDot = className.lastIndexOf('.');
        // $Name$Definition, the definitions of its proxies and, for an introduced interface, $Name$Intercepted$Definition
        String prefix = className.substring(0, lastDot + 1) + '$' + className.substring(lastDot + 1);
        String definition = prefix + "$Definition";
        List<BeanDefinitionReference<?>> references = new ArrayList<>();
        for (BeanDefinitionReference<?> reference : beanContext.getBeanDefinitionReferences()) {
            String name = reference.getBeanDefinitionName();
            if (name.equals(definition) || name.startsWith(definition + '$') || name.startsWith(prefix + "$Intercepted$Definition")) {
                references.add(reference);
            }
        }
        return references;
    }

    /**
     * Recreates what changed and the consumer processors, which remove their subscribers as they are destroyed. The
     * context creates the processors again at once and gives them the subscription methods, as at startup, so that
     * they subscribe again on top of the new beans. Nothing is created that was not created already, but for the
     * listener beans the processors resolve.
     *
     * @param registries Whether to recreate the serializer and binder registries and the client advice, and the beans
     * that received them
     * @param listeners The listener classes that changed, whose beans are recreated
     * @param reason Why, for the log
     */
    private void restartSubscribers(boolean registries, List<String> listeners, String reason) {
        if (!(beanContext instanceof WatchableBeanContext context)) {
            return;
        }
        // taken first: recreating one destroys the beans that received it, as the graph records them. The listener
        // beans go first, as a processor subscribes again as soon as it is recreated, on the listener beans it finds
        // then; the processors go last, recreated already, and so skipped, when a registry they received was
        List<Object> beans = new ArrayList<>();
        if (!listeners.isEmpty()) {
            Set<String> names = new HashSet<>(listeners);
            for (BeanRegistration<?> registration : beanContext.getActiveBeanRegistrations(LISTENERS)) {
                if (names.contains(registration.getBeanDefinition().getBeanType().getName())) {
                    add(beans, registration.bean());
                }
            }
        }
        if (registries) {
            for (Class<?> type : REGISTRIES) {
                for (BeanRegistration<?> registration : beanContext.getActiveBeanRegistrations(type)) {
                    add(beans, registration.bean());
                }
            }
        }
        for (BeanRegistration<AbstractPubSubConsumerMethodProcessor> registration : beanContext.getActiveBeanRegistrations(AbstractPubSubConsumerMethodProcessor.class)) {
            add(beans, registration.bean());
        }
        if (beans.isEmpty()) {
            return;
        }
        LOG.debug("Restarting the Pub/Sub subscribers: {}", reason);
        for (Object bean : beans) {
            // false for a bean destroyed with one recreated before it, and for all of them in a context that does
            // not track bean dependencies: they are kept, and the subscribers stay until a restart
            context.recreate(bean);
        }
    }

    private static void add(List<Object> beans, Object bean) {
        for (Object taken : beans) {
            if (taken == bean) {
                return;
            }
        }
        beans.add(bean);
    }

    private static List<String> names(BeanDefinitionChange<?> change) {
        List<String> changed = new ArrayList<>();
        for (BeanDefinition<?> definition : change.removed()) {
            changed.add(definition.getBeanType().getName());
        }
        for (BeanDefinition<?> definition : change.added()) {
            changed.add(definition.getBeanType().getName());
        }
        return changed;
    }

    private static boolean ignored(BeanDefinitionChange<?> change) {
        return change.initial() || (change.added().isEmpty() && change.removed().isEmpty());
    }

    /**
     * Restarts the subscribers when a listener definition is registered or removed. The first batch is what the
     * consumer processors were given at startup.
     */
    private final class ListenerDefinitionsWatcher implements BeanDefinitionWatcher<Object>, Ordered {
        @Override
        public void onChange(BeanDefinitionChange<Object> change) {
            if (!ignored(change)) {
                restartSubscribers(false, names(change), "listener definitions changed");
            }
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }

    /**
     * Selects the listener beans, whether a stereotype is on the class or only on a method. The class metadata of a
     * bean is read first, which needs nothing loaded. The methods are read only for a bean that requires method
     * processing, as a bean whose methods a consumer processor receives does; that skips the executable methods of
     * every other bean, and the class that holds them. A reference, which has no methods to read, is kept when it
     * requires method processing.
     */
    private static final class ListenerQualifier implements Qualifier<Object> {
        @Override
        public <B extends BeanType<Object>> Stream<B> reduce(Class<Object> beanType, Stream<B> candidates) {
            return candidates.filter(ListenerQualifier::isListenerBean);
        }

        @Override
        public boolean doesQualify(Class<Object> beanType, BeanType<Object> candidate) {
            return isListenerBean(candidate);
        }

        private static boolean isListenerBean(BeanType<?> candidate) {
            if (hasStereotype(candidate.getAnnotationMetadata(), LISTENER_STEREOTYPES)) {
                return true;
            }
            if (!candidate.requiresMethodProcessing()) {
                return false;
            }
            if (!(candidate instanceof BeanDefinition<?> definition)) {
                return true;
            }
            for (ExecutableMethod<?, ?> method : definition.getExecutableMethods()) {
                if (hasStereotype(method.getAnnotationMetadata(), LISTENER_STEREOTYPES)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "Pub/Sub listeners";
        }
    }

    /**
     * Selects the beans the registries are built from: a serializer, a binder, a registry of them, or a client.
     */
    private static final class RegisteredQualifier implements Qualifier<Object> {
        @Override
        public <B extends BeanType<Object>> Stream<B> reduce(Class<Object> beanType, Stream<B> candidates) {
            return candidates.filter(DevelopmentPubSubReloader::isRegisteredBean);
        }

        @Override
        public boolean doesQualify(Class<Object> beanType, BeanType<Object> candidate) {
            return isRegisteredBean(candidate);
        }

        @Override
        public String toString() {
            return "Pub/Sub serializers, binders and clients";
        }
    }

    /**
     * Recreates the registries when a serializer, binder, registry or client definition is registered or removed.
     * The first batch is what they were built from.
     */
    private final class RegistryDefinitionsWatcher implements BeanDefinitionWatcher<Object>, Ordered {
        @Override
        public void onChange(BeanDefinitionChange<Object> change) {
            if (!ignored(change)) {
                restartSubscribers(true, List.of(), names(change) + " registered or removed");
            }
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }

    /**
     * Follows a class change applied in place.
     */
    private final class ClassWatcher implements ClassChangeWatcher, Ordered {
        @Override
        public void onChange(ClassChangeEvent change) {
            onClassChange(change);
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }

}
