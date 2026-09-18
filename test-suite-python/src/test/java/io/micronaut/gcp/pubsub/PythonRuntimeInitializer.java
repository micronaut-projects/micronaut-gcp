package io.micronaut.gcp.pubsub;

import io.micronaut.core.convert.MutableConversionService;
import io.micronaut.core.convert.TypeConverterRegistrar;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.graalvm.polyglot.Context;

/**
 * TODO(python): {@code @Executable(processOnStartup = true)} processors such as the Pub/Sub consumer
 * advice are created before the {@code @Context} beans are initialized, so the Python beans they
 * depend on (message SerDes, subscriber factory, argument binders, {@code @PubSubListener} beans)
 * would be instantiated before the GraalPy runtime exists ("GraalPy context has not been
 * initialized"). The type converter registrars are created before those processors, so injecting
 * the GraalPy context into this registrar makes sure the runtime is installed before the first
 * Python bean is instantiated.
 */
@Singleton
public class PythonRuntimeInitializer implements TypeConverterRegistrar {

    public PythonRuntimeInitializer(@Named("python") Context graalPyContext) {
        // the injection of the GraalPy context is all that is needed
    }

    @Override
    public void register(MutableConversionService conversionService) {
        // nothing to register
    }
}
