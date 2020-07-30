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
package io.micronaut.gcp.pubsub.quickstart;
//tag::imports[]
import io.micronaut.gcp.pubsub.annotation.PubSubListener;
import io.micronaut.gcp.pubsub.annotation.Subscription;
// end::imports[]

// tag::clazz[]
@PubSubListener // <1>
public class AnimalListener {


    /**
     *
     * @param data raw data
     */
    @Subscription("animals") // <2>
    public void onMessage(byte[] data) { // <3>
        System.out.println("Message received");
    }

}
// end::clazz[]
