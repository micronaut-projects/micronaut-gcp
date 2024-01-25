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
package io.micronaut.gcp.pubsub.subscriber

//tag::imports[]
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.annotation.PubSubListener
import io.micronaut.gcp.pubsub.annotation.PushSubscription
import io.micronaut.gcp.pubsub.support.Animal
import io.micronaut.messaging.Acknowledgement
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import reactor.core.publisher.Mono

// end::imports[]

@Requires(property = "spec.name", value = "AcknowledgementPushSubscriberSpec")
// tag::clazz[]
@PubSubListener
class AcknowledgementPushSubscriber(private val messageProcessor: MessageProcessor) {

    @ExecuteOn(TaskExecutors.BLOCKING)
    @PushSubscription("animals-push")
    fun onMessage(animal: Animal, acknowledgement: Acknowledgement) {
        if (messageProcessor.handleAnimalMessage(animal).block() == true) {
            acknowledgement.ack()
            messageProcessor.recordAcknowledgement(acknowledgement)
        } else {
            acknowledgement.nack()
            messageProcessor.recordAcknowledgement(acknowledgement)
        }
    }

    @PushSubscription("animals-async-push")
    fun onReactiveMessage(message: Mono<Animal>, acknowledgement: Acknowledgement): Mono<Boolean> {
        return message.flatMap { animal -> messageProcessor.handleAnimalMessage(animal) }
            .doOnNext { result ->
                if (result) {
                    acknowledgement.ack()
                    messageProcessor.recordAcknowledgement(acknowledgement)
                } else {
                    acknowledgement.nack()
                    messageProcessor.recordAcknowledgement(acknowledgement)
                }
            }
    }
}
// tag::clazz[]
