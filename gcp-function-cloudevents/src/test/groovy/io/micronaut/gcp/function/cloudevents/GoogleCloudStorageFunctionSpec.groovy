package io.micronaut.gcp.function.cloudevents

import com.google.events.cloud.storage.v1.StorageObjectData
import io.cloudevents.CloudEvent
import io.cloudevents.CloudEventContext
import io.cloudevents.core.builder.CloudEventBuilder
import io.micronaut.core.annotation.NonNull
import io.micronaut.core.annotation.Nullable
import spock.lang.Specification

import java.time.OffsetDateTime

class GoogleCloudStorageFunctionSpec extends Specification {

    void 'test google cloud storage function'() {
        given:
        TestFunction function = new TestFunction()

        and:
        String json = '''
{
  "bucket": "my-bucket",
  "contentType": "text/plain",
  "etag": "CNTohaymoPYCEAE=",
  "generation": "1647081924359252",
  "id": "my-bucket/my-file.txt/1647081924359252",
  "kind": "storage#object",
  "kmsKeyName": "projects/my-project/locations/us-central1/keyRings/my-keyring/cryptoKeys/my-key-us-central1/cryptoKeyVersions/2",
  "mediaLink": "https://www.googleapis.com/download/storage/v1/b/my-bucket/o/my-file.txt?generation=1647081924359252&alt=media",
  "metageneration": "1",
  "name": "my-file.txt",
  "selfLink": "https://www.googleapis.com/storage/v1/b/my-bucket/o/my-file.txt",
  "size": "7",
  "storageClass": "STANDARD",
  "timeCreated": "2022-01-01T12:30:00.123Z",
  "timeStorageClassUpdated": "2022-10-01T12:30:00.456Z",
  "updated": "2022-10-01T12:30:00.456Z"
}
'''
        CloudEvent event = CloudEventBuilder.v1()
                .withId('123')
                .withSource(URI.create('//storage.googleapis.com/projects/_/buckets/my-bucket'))
                .withType('google.cloud.storage.object.v1.finalized')
                .withData('application/json', json.bytes)
                .withSubject('objects/subject')
                .withTime(OffsetDateTime.now())
                .build()

        when:
        function.accept(event)

        then:
        function.context == event
        with(function.data) {
            kind == 'storage#object'
            //id == 'my-bucket/my-file.txt/1647081924359252'
            selfLink == 'https://www.googleapis.com/storage/v1/b/my-bucket/o/my-file.txt'
            mediaLink == 'https://www.googleapis.com/download/storage/v1/b/my-bucket/o/my-file.txt?generation=1647081924359252&alt=media'
            name == 'my-file.txt'
            bucket == 'my-bucket'
            generation == 1647081924359252
            metageneration == 1
            contentType == 'text/plain'
            storageClass == 'STANDARD'
            size == 7
            etag == 'CNTohaymoPYCEAE='
            kmsKeyName == 'projects/my-project/locations/us-central1/keyRings/my-keyring/cryptoKeys/my-key-us-central1/cryptoKeyVersions/2'
            timeCreated == OffsetDateTime.parse('2022-01-01T12:30:00.123Z')
            updated == OffsetDateTime.parse('2022-10-01T12:30:00.456Z')
            timeStorageClassUpdated == OffsetDateTime.parse('2022-10-01T12:30:00.456Z')
        }

        cleanup:
        function.close()
    }
/*
//tag::storageObjectDataStart[]
    public class TestFunction extends GoogleCloudEventsFunction<StorageObjectData> {
//end::storageObjectDataStart[]
*/
    static class TestFunction extends GoogleCloudEventsFunction<StorageObjectData> {
        CloudEventContext context
        StorageObjectData data
//tag::storageObjectDataMethod[]
        @Override
        protected void accept(@NonNull CloudEventContext context, @Nullable StorageObjectData data) throws Exception {
//end::storageObjectDataMethod[]
            this.context = context
            this.data = data
//tag::storageObjectDataClose[]
        }
    }
//end::storageObjectDataClose[]
}
