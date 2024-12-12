package io.micronaut.gcp.credentials

import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.Property
import io.micronaut.core.util.StringUtils
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class DefaultOAuth2HttpTransportFactoryDisabledSpec extends Specification {

    @Inject
    BeanContext beanContext

    void testDefaultOAuth2HttpTransportFactoryDisabled() {
        expect:
        !beanContext.containsBean(DefaultOAuth2HttpTransportFactory.class)
        !beanContext.containsBean(OAuth2MessageBodyWriter.class)
    }
}