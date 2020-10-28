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
package io.micronaut.gcp.pubsub.ordering;
//tag::imports[]
import io.micronaut.gcp.pubsub.support.Order;
import javax.inject.Singleton;
//end::imports[]

//tag::clazz[]
@Singleton
public class OrderService {

    private final OrderClient client;

    public OrderService(OrderClient client) {
        this.client = client;
    }

    public void placeOrder() {
        Order order = new Order(100, "GOOG");
        client.send(order, order.getSymbol());
    }

}
//end::clazz[]
