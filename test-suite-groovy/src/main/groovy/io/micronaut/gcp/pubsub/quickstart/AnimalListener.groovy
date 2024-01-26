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
package io.micronaut.gcp.pubsub.quickstart

import io.micronaut.context.annotation.Requires;
//tag::imports[]
import io.micronaut.gcp.pubsub.annotation.PubSubListener;
// end::imports[]
//tag::pushimport[]
import io.micronaut.gcp.pubsub.annotation.PushSubscription;
//end::pushimport[]
//tag::pullimport[]
import io.micronaut.gcp.pubsub.annotation.Subscription;
//end::pullimport[]

// There are currently no tests for this class. It is disabled in the test environment
// in order to prevent clashes with other subscribers.
@Requires(notEnv = "test")
// tag::clazz[]
@PubSubListener // <1>
class AnimalListener {
// end::clazz[]
// tag::pull[]
    @Subscription("animals") // <2>
    void onMessage(byte[] data) { // <3>
        System.out.println("Message received")
    }
// end::pull[]
// tag::push[]
    @PushSubscription("animals-push") // <2>
    void onPushMessage(byte[] data) { // <3>
        System.out.println("Message received")
    }
// end::push[]
// tag::clazzend[]
}
// end::clazzend[]
