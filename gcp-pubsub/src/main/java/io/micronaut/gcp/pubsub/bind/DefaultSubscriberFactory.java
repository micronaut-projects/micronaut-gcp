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
import com.google.pubsub.v1.ProjectSubscriptionName;
import io.micronaut.context.annotation.Factory;
import io.micronaut.gcp.pubsub.exception.PubSubListenerException;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@Factory
public class DefaultSubscriberFactory implements SubscriberFactory {

    private final ConcurrentHashMap<String, Subscriber> subscribers = new ConcurrentHashMap<>();

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
}
