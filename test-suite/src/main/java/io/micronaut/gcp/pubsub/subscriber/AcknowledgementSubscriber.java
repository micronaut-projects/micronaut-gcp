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
package io.micronaut.gcp.pubsub.subscriber;
//tag::imports[]

import io.micronaut.context.annotation.Requires;
import io.micronaut.gcp.pubsub.annotation.PubSubListener;
import io.micronaut.gcp.pubsub.annotation.Subscription;
import io.micronaut.gcp.pubsub.support.Animal;
import io.micronaut.messaging.Acknowledgement;
import reactor.core.publisher.Mono;
// end::imports[]

@Requires(property = "spec.name", value = "AcknowledgementSubscriberTest")
// tag::clazz[]
@PubSubListener
public class AcknowledgementSubscriber {

    private final MessageProcessor messageProcessor;

    public AcknowledgementSubscriber(MessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    @Subscription("animals")
    public void onMessage(Animal animal, Acknowledgement acknowledgement) {
        if (Boolean.TRUE.equals(messageProcessor.handleAnimalMessage(animal).block())) {
            acknowledgement.ack();
        } else {
            acknowledgement.nack();
        }
    }

    @Subscription("animals-async")
    public Mono<Boolean> onReactiveMessage(Mono<Animal> animal, Acknowledgement acknowledgement) {
        return animal.flatMap(messageProcessor::handleAnimalMessage)
            .doOnNext(result -> {
                if (Boolean.TRUE.equals(result)) {
                    acknowledgement.ack();
                } else {
                    acknowledgement.nack();
                }
            });
    }

}
// end::clazz[]
