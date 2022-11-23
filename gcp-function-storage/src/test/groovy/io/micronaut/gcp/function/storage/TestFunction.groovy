package io.micronaut.gcp.function.storage

import io.cloudevents.CloudEventContext
import io.micronaut.core.annotation.NonNull
import io.micronaut.core.annotation.Nullable
import jakarta.inject.Singleton

@Singleton
class TestFunction extends GoogleCloudStorageFunction {

    CloudEventContext context
    GoogleStorageObject data

    @Override
    void accept(@NonNull CloudEventContext context, @Nullable GoogleStorageObject data) {
        this.context = context
        this.data = data
    }
}
