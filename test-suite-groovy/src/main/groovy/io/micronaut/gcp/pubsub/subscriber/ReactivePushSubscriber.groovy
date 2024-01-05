/*
 * Copyright 2017-2023 original authors
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
package io.micronaut.gcp.pubsub.subscriber

import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.annotation.MessageId
import io.micronaut.gcp.pubsub.annotation.PubSubListener;
//tag::imports[]

import io.micronaut.gcp.pubsub.annotation.PushSubscription
import io.micronaut.gcp.pubsub.support.Animal
import reactor.core.publisher.Mono

// end::imports[]

@Requires(property = "spec.name", value = "ReactivePushSubscriberSpec")
// tag::clazz[]
@PubSubListener
class ReactivePushSubscriber {

    private final MessageProcessor messageProcessor

    ReactivePushSubscriber(MessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor
    }

    @PushSubscription("raw-push-subscription") // <1>
    Mono<Object> receiveRaw(Mono<byte[]> data, @MessageId String id) {
        return data.flatMap(messageProcessor::handleByteArrayMessage)
    }

    @PushSubscription("native-push-subscription") // <2>
    Mono<Object> receiveNative(Mono<PubsubMessage> message) {
        return message.flatMap(messageProcessor::handlePubSubMessage)
    }

    @PushSubscription("animals-push") // <3>
    Mono<Object> receivePojo(Mono<Animal> animal, @MessageId String id) {
        return animal.flatMap(messageProcessor::handleAnimalMessage)
    }

    @PushSubscription(value = "animals-legacy-push", contentType = "application/xml") // <4>
    Mono<Object> receiveXML(Mono<Animal> animal, @MessageId String id) {
        return animal.flatMap(messageProcessor::handleAnimalMessage)
    }

}
// end::clazz[]

