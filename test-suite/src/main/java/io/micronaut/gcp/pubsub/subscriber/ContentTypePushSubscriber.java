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
package io.micronaut.gcp.pubsub.subscriber;
//tag::imports[]

import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.context.annotation.Requires;
import io.micronaut.gcp.pubsub.annotation.MessageId;
import io.micronaut.gcp.pubsub.annotation.PubSubListener;
import io.micronaut.gcp.pubsub.annotation.PushSubscription;
import io.micronaut.gcp.pubsub.support.Animal;
import io.micronaut.http.MediaType;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
// end::imports[]

@Requires(property = "spec.name", value = "ContentTypePushSubscriberTest")
// tag::clazz[]
@PubSubListener
@ExecuteOn(TaskExecutors.BLOCKING) // <1>
public class ContentTypePushSubscriber {

    @PushSubscription("raw-push-subscription") // <2>
    void receiveRaw(byte[] data, @MessageId String id) {
        //process with blocking code
    }

    @PushSubscription("native-push-subscription") // <3>
    void receiveNative(PubsubMessage message) {
        //process with blocking code
    }

    @PushSubscription("animals-push") // <4>
    void receivePojo(Animal animal, @MessageId String id) {
        //process with blocking code
    }

    @PushSubscription(value = "animals-legacy-push", contentType = MediaType.APPLICATION_XML) // <5>
    void receiveXML(Animal animal, @MessageId String id) {
        //process with blocking code
    }

}
// end::clazz[]
