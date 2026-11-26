package io.micronaut.gcp.utils

import com.google.cloud.parametermanager.v1.ParameterManagerClient
import com.google.cloud.parametermanager.v1.stub.ParameterManagerStub
import io.micronaut.context.annotation.BootstrapContextCompatible
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.gcp.parametermanager.ParameterManagerFactory
import jakarta.inject.Singleton
import spock.lang.Specification

@Factory
@BootstrapContextCompatible
class LocalParameterManagerClientFactory extends Specification {

    @Singleton
    @Replaces(value = ParameterManagerClient, factory = ParameterManagerFactory)
    ParameterManagerClient parameterManagerClient() {
        def stub = Mock(ParameterManagerStub)
        stub.getParameterVersionCallable() >> new SettableUnaryCallableFetch()
        stub.renderParameterVersionCallable() >> new SettableUnaryCallableRender()
        return ParameterManagerClient.create(stub)

    }
}
