package io.micronaut.gcp.serde.cloudevents

import com.google.events.cloud.audit.v1.LogEntryData
import com.google.events.cloud.cloudbuild.v1.BuildEventData
import com.google.events.cloud.firestore.v1.DocumentEventData
import com.google.events.cloud.pubsub.v1.MessagePublishedData
import com.google.events.cloud.scheduler.v1.SchedulerJobData
import com.google.events.cloud.storage.v1.StorageObjectData
import com.google.events.firebase.analytics.v1.AnalyticsLogData
import com.google.events.firebase.auth.v1.AuthEventData
import com.google.events.firebase.database.v1.ReferenceEventData
import com.google.events.firebase.remoteconfig.v1.RemoteConfigEventData
import com.google.protobuf.util.Timestamps
import io.micronaut.context.annotation.Property
import io.micronaut.json.JsonMapper
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
@Property(name = "micronaut.serde.write-dates-as-timestamps", value = "false")
class CloudEventTypesSerdeSpec extends Specification {
    @Inject
    JsonMapper jsonMapper

    void "creates serdes for supported event types"() {
        given:
        CloudEventTypesSerde factory = new CloudEventTypesSerde()

        expect:
        factory.referenceEventDataSerde()
        factory.authEventDataSerde()
        factory.remoteConfigEventDataSerde()
        factory.analyticsLogDataSerde()
        factory.documentEventDataSerde()
        factory.buildEventDataSerde()
        factory.schedulerJobDataSerde()
        factory.storageObjectDataSerde()
        factory.logEntryDataSerde()
        factory.messagePublishedDataSerde()

        and:
        ReferenceEventData.defaultInstance
        AuthEventData.defaultInstance
        RemoteConfigEventData.defaultInstance
        AnalyticsLogData.defaultInstance
        DocumentEventData.defaultInstance
        BuildEventData.defaultInstance
        SchedulerJobData.defaultInstance
        LogEntryData.defaultInstance
        MessagePublishedData.defaultInstance
    }

    void "deserialize storage object"() {

        given:
        String json = '''
{
  "kind": "storage#object",
  "id": "my-bucket/dir/my-file.txt/123456789",
  "selfLink": "https://www.googleapis.com/storage/v1/b/my-bucket/o/dir/my-file.txt",
  "mediaLink": "https://www.googleapis.com/download/storage/v1/b/my-bucket/o/dir/my-file.txt?generation=123456789&alt=media",
  "name": "dir/my-file.txt",
  "bucket": "my-bucket",
  "generation": 123456789,
  "metageneration": 1,
  "contentType": "text/plain",
  "storageClass": "STANDARD",
  "size": 1024,
  "md5Hash": "5d41402abc4b2a76b9719d911017c592",
  "contentEncoding": "gzip",
  "contentDisposition": "inline",
  "contentLanguage": "en-US",
  "cacheControl": "public, max-age=3600",
  "crc32c": "3610a686",
  "componentCount": 1,
  "etag": "etag1",
  "kmsKeyName": "projects/my-project/locations/us-central1/keyRings/my-keyring/cryptoKeys/my-key-us-central1/cryptoKeyVersions/2",
  "temporaryHold": false,
  "eventBasedHold": true,
  "retentionExpirationTime": "2022-12-01T12:30:00.123Z",
  "timeCreated": "2022-01-01T12:30:00.123Z",
  "updated": "2022-04-01T12:30:00.123Z",
  "timeDeleted": "2022-02-01T12:30:00.123Z",
  "timeStorageClassUpdated": "2022-03-01T12:30:00.123Z",
  "customTime": "2022-05-01T12:30:00.123Z",
  "metadata": {"foo": "hello", "bar": "world"}
}
'''

        when:
        StorageObjectData data = jsonMapper.readValue(json, StorageObjectData)

        then:
        with(data) {
            kind == 'storage#object'
            //id == 'my-bucket/dir/my-file.txt/123456789'
            selfLink == 'https://www.googleapis.com/storage/v1/b/my-bucket/o/dir/my-file.txt'
            mediaLink == 'https://www.googleapis.com/download/storage/v1/b/my-bucket/o/dir/my-file.txt?generation=123456789&alt=media'
            name == 'dir/my-file.txt'
            bucket == 'my-bucket'
            generation == 123456789
            metageneration == 1
            contentType == 'text/plain'
            storageClass == 'STANDARD'
            size == 1024
            md5Hash == '5d41402abc4b2a76b9719d911017c592'
            contentEncoding == 'gzip'
            contentDisposition == 'inline'
            contentLanguage == 'en-US'
            cacheControl == 'public, max-age=3600'
            //crc32c == '3610a686'
            componentCount == 1
            etag == 'etag1'
            kmsKeyName == 'projects/my-project/locations/us-central1/keyRings/my-keyring/cryptoKeys/my-key-us-central1/cryptoKeyVersions/2'
            temporaryHold == false
            eventBasedHold == true
            retentionExpirationTime == Timestamps.parse('2022-12-01T12:30:00.123Z')
            timeCreated == Timestamps.parse('2022-01-01T12:30:00.123Z')
            updated == Timestamps.parse('2022-04-01T12:30:00.123Z')
            timeDeleted == Timestamps.parse('2022-02-01T12:30:00.123Z')
            timeStorageClassUpdated == Timestamps.parse('2022-03-01T12:30:00.123Z')
            metadata.foo == 'hello'
            metadata.bar == 'world'
        }
    }

    void "serialize storage object"() {

        given:
        StorageObjectData data = StorageObjectData.newBuilder()
                .setKind('storage#object')
                .setId('my-bucket/dir/my-file.txt/123456789')
                .setSelfLink('https://www.googleapis.com/storage/v1/b/my-bucket/o/dir/my-file.txt')
                .setMediaLink('https://www.googleapis.com/download/storage/v1/b/my-bucket/o/dir/my-file.txt?generation=123456789&alt=media')
                .setName('dir/my-file.txt')
                .setBucket('my-bucket')
                .setGeneration(123456789)
                .setMetageneration(1)
                .setContentType('text/plain')
                .setStorageClass('STANDARD')
                .setSize(1024)
                .setMd5Hash('5d41402abc4b2a76b9719d911017c592')
                .setContentEncoding('gzip')
                .setContentDisposition('inline')
                .setContentLanguage('en-US')
                .setCacheControl('public, max-age=3600')
                .setCrc32C('3610a686')
                .setComponentCount(1)
                .setEtag('etag1')
                .setKmsKeyName('projects/my-project/locations/us-central1/keyRings/my-keyring/cryptoKeys/my-key-us-central1/cryptoKeyVersions/2')
                .setTemporaryHold(false)
                .setEventBasedHold(true)
                .setRetentionExpirationTime(Timestamps.parse('2022-12-01T12:30:00.123Z'))
                .setTimeCreated(Timestamps.parse('2022-01-01T12:30:00.123Z'))
                .setUpdated(Timestamps.parse('2022-04-01T12:30:00.123Z'))
                .setTimeDeleted(Timestamps.parse('2022-02-01T12:30:00.123Z'))
                .setTimeStorageClassUpdated(Timestamps.parse('2022-03-01T12:30:00.123Z'))
                .putMetadata('foo', 'hello')
                .putMetadata('bar', 'world')
                .build()

        when:
        String json = jsonMapper.writeValueAsString(data)
        Map map = jsonMapper.readValue(json, Map)

        then:
        with(map) {
            kind == 'storage#object'
            id == 'my-bucket/dir/my-file.txt/123456789'
            selfLink == 'https://www.googleapis.com/storage/v1/b/my-bucket/o/dir/my-file.txt'
            mediaLink == 'https://www.googleapis.com/download/storage/v1/b/my-bucket/o/dir/my-file.txt?generation=123456789&alt=media'
            name == 'dir/my-file.txt'
            bucket == 'my-bucket'
            generation == '123456789'
            metageneration == '1'
            contentType == 'text/plain'
            storageClass == 'STANDARD'
            size == '1024'
            md5Hash == '5d41402abc4b2a76b9719d911017c592'
            contentEncoding == 'gzip'
            contentDisposition == 'inline'
            contentLanguage == 'en-US'
            cacheControl == 'public, max-age=3600'
            crc32c == '3610a686'
            componentCount == 1
            etag == 'etag1'
            kmsKeyName == 'projects/my-project/locations/us-central1/keyRings/my-keyring/cryptoKeys/my-key-us-central1/cryptoKeyVersions/2'
            !containsKey('temporaryHold')
            eventBasedHold == true
            retentionExpirationTime == '2022-12-01T12:30:00.123Z'
            timeCreated == '2022-01-01T12:30:00.123Z'
            updated == '2022-04-01T12:30:00.123Z'
            timeDeleted == '2022-02-01T12:30:00.123Z'
            timeStorageClassUpdated == '2022-03-01T12:30:00.123Z'
            metadata.foo == "hello"
            metadata.bar == "world"
        }
    }
}
