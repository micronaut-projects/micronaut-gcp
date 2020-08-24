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
package io.micronaut.gcp.function.http.jackson;

import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.jackson.JacksonConfiguration;

import javax.inject.Singleton;

/**
 * Disables module scan for Jackson which is slow in function context.
 *
 * @author graemerocher
 * @since 1.2.0
 */
@Singleton
public class GoogleJacksonConfiguration implements BeanCreatedEventListener<JacksonConfiguration> {
    @Override
    public JacksonConfiguration onCreated(BeanCreatedEvent<JacksonConfiguration> event) {
        JacksonConfiguration jacksonConfiguration = event.getBean();
        jacksonConfiguration.setModuleScan(false);
        jacksonConfiguration.setBeanIntrospectionModule(true);
        return jacksonConfiguration;
    }
}
