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
package io.micronaut.gcp.pubsub.annotation;

/**
 * Defines the value of they to be used for message ordering.
 * See: <a href="https://cloud.google.com/pubsub/docs/publisher#using_ordering_keys">ordering keys</a>.
 *
 * Note that without setting the topic endpoint, message ordering will not work. @see {@link Topic}
 *
 * @author Vinicius Carvalho
 * @since 3.2.2
 */
public @interface OrderingKey {
	/**
	 * Value to be used as ordering key.
	 * @return key value
	 */
	String value();
}
