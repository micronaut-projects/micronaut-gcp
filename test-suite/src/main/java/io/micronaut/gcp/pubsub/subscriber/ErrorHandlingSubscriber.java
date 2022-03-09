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
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.gcp.pubsub.annotation.PubSubListener;
import io.micronaut.gcp.pubsub.bind.PubSubConsumerState;
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverException;
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverExceptionHandler;
import io.micronaut.gcp.pubsub.support.Animal;
// end::imports[]

// tag::clazz[]
@PubSubListener
public class ErrorHandlingSubscriber implements PubSubMessageReceiverExceptionHandler { // <1>
    /**
     *
     * @param animal payload
     */
    public void onMessage(Animal animal) {
        throw new RuntimeException("error");
    }

    @Override
    public void handle(PubSubMessageReceiverException exception) { // <2>

        Object listener = exception.getListener(); // <3>
        PubSubConsumerState state = exception.getState(); // <4>
        PubsubMessage originalMessage = state.getPubsubMessage();
        String contentType = state.getContentType();
        //some logic
        state.getAckReplyConsumer().ack(); // <5>

    }
}
// end::clazz[]
