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
package io.micronaut.gcp.pubsublite.support;

import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsublite.SubscriptionPath;

/**
 * Holds necessary configuration to create {@link com.google.cloud.pubsublite.cloudpubsub.Subscriber} via {@link LiteSubscriberFactory}.
 *
 * Based on {@link io.micronaut.gcp.pubsub.bind.SubscriberFactoryConfig}
 *
 * @author Jacob Mims
 * @since 2.2.0
 */
public class LiteSubscriberFactoryConfig {
    private final SubscriptionPath subscriptionPath;
    private final MessageReceiver receiver;
    private final String subscriberConfiguration;

    public LiteSubscriberFactoryConfig(SubscriptionPath subscriptionPath, MessageReceiver receiver, String subscriberConfiguration) {
        this.subscriptionPath = subscriptionPath;
        this.receiver = receiver;
        this.subscriberConfiguration = subscriberConfiguration;
    }

    /**
     *
     * @return SubscriptionPath
     */
    public SubscriptionPath getSubscriptionPath() {
        return subscriptionPath;
    }

    /**
     *
     * @return MessageReceiver to be registered for the created {@link com.google.cloud.pubsublite.cloudpubsub.Subscriber}
     */
    public MessageReceiver getReceiver() {
        return receiver;
    }

    /**
     *
     * @return name of the configuration passed via {@link io.micronaut.gcp.pubsublite.annotation.LiteSubscription}
     */
    public String getSubscriberConfiguration() {
        return subscriberConfiguration;
    }
}
