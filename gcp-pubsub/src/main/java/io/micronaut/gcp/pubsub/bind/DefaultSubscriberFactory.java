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
package io.micronaut.gcp.pubsub.bind;

import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriberInterface;
import com.google.pubsub.v1.ProjectSubscriptionName;
import io.micronaut.gcp.pubsub.exception.PubSubListenerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@Singleton
public class DefaultSubscriberFactory implements SubscriberFactory, AutoCloseable {

    private final ConcurrentHashMap<String, Subscriber> subscribers = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(DefaultSubscriberFactory.class);

    @Override
    public Subscriber createSubscriber(ProjectSubscriptionName projectSubscriptionName, MessageReceiver receiver) {
        Subscriber subscriber = subscribers.compute(projectSubscriptionName.toString(), (k, v) -> {
            if (v == null) {

                return Subscriber.newBuilder(projectSubscriptionName, receiver).build();
            }
            throw new PubSubListenerException(String.format("Subscription %s is already registered for another" +
                    " method", projectSubscriptionName));
        });
        subscriber.startAsync();
        return subscriber;
    }

    @PreDestroy
    @Override
    public void close() throws Exception {
        while (!subscribers.entrySet().isEmpty()) {
            Iterator<Map.Entry<String, Subscriber>> it = subscribers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Subscriber> entry = it.next();
                SubscriberInterface subscriber = entry.getValue();
                try {
                    subscriber.stopAsync().awaitTerminated();
                } catch (Exception e) {
                    logger.error("Failed stopping subscriber for " + entry.getKey(), e);
                } finally {
                    it.remove();
                }

            }
        }
    }
}
