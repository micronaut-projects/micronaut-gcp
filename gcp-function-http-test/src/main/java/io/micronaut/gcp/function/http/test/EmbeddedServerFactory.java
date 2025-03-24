/*
 * Copyright 2017-2025 original authors
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
package io.micronaut.gcp.function.http.test;

import com.sun.net.httpserver.HttpHandler;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.Internal;
import io.micronaut.gcp.function.http.HttpFunction;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Internal
@Experimental
@Factory
class EmbeddedServerFactory {
    private final ApplicationContext applicationContext;

    EmbeddedServerFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Named("HttpServer")
    @Singleton
    ApplicationContextProvider createFunction() {
        return new HttpFunction() {
            @Override
            protected ApplicationContext buildApplicationContext(Object context) {
                ApplicationContext ctx = EmbeddedServerFactory.this.applicationContext;
                this.applicationContext = ctx;
                return ctx;
            }
        };
    }

    @Singleton
    HttpHandler createHandler(@Named("HttpServer") ApplicationContextProvider applicationContextProvider) {
        if (applicationContextProvider instanceof HttpFunction function) {
            return new GoogleCloudFunctionHttpHandler(function);
        }
        throw new ConfigurationException("ApplicationContextProvider with name qualifier HttpServer should be of type HttpFunction");
    }
}
