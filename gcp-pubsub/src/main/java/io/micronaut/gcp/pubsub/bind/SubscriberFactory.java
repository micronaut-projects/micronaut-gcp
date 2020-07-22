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
import com.google.cloud.pubsub.v1.SubscriberInterface;
import com.google.pubsub.v1.ProjectSubscriptionName;

/**
 * Factory to create {@link SubscriberInterface} using default configurations.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
public interface SubscriberFactory {
    /**
     * Creates an {@link SubscriberInterface} implementation and register the receiver to it. Implementations should also
     * handle start/stop life cycle events.
     * @param projectSubscriptionName The FQN of the project subscription.
     * @param receiver The receiver that will handle new messages
     * @return An implementation of SubscriberInterface
     */
    SubscriberInterface createSubscriber(ProjectSubscriptionName projectSubscriptionName, MessageReceiver receiver);
}
