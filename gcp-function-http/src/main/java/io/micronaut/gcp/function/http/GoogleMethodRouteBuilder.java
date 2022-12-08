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
package io.micronaut.gcp.function.http;

import io.micronaut.context.ExecutionHandleLocator;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.context.ServerContextPathProvider;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.web.router.AnnotatedMethodRouteBuilder;
import io.micronaut.web.router.UriRoute;
import jakarta.inject.Singleton;

/**
 * Modifies the route builder to take into account the context path.
 *
 * @author graemerocher
 * @since 2.0.0
 */
@Singleton
@Internal
@Replaces(AnnotatedMethodRouteBuilder.class)
class GoogleMethodRouteBuilder extends AnnotatedMethodRouteBuilder {
    private final ServerContextPathProvider contextPathProvider;

    /**
     * @param executionHandleLocator The execution handler locator
     * @param uriNamingStrategy      The URI naming strategy
     * @param conversionService      The conversion service
     * @param contextPathProvider    The context path provider
     */
    public GoogleMethodRouteBuilder(
            ExecutionHandleLocator executionHandleLocator,
            UriNamingStrategy uriNamingStrategy,
            ConversionService conversionService,
            ServerContextPathProvider contextPathProvider) {
        super(executionHandleLocator, uriNamingStrategy, conversionService);
        this.contextPathProvider = contextPathProvider;
    }

    @Override
    protected UriRoute buildBeanRoute(String httpMethodName, HttpMethod httpMethod, String uri, BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        String cp = contextPathProvider.getContextPath();
        if (cp != null) {
            uri = StringUtils.prependUri(cp, uri);
        }
        return super.buildBeanRoute(httpMethodName, httpMethod, uri, beanDefinition, method);
    }
}
