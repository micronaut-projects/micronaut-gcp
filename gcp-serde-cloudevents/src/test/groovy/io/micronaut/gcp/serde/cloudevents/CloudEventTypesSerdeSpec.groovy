package io.micronaut.gcp.serde.cloudevents

import com.google.events.cloud.storage.v1.StorageObjectData
import io.micronaut.context.annotation.Property
import io.micronaut.json.JsonMapper
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import java.time.OffsetDateTime

@MicronautTest
@Property(name = "micronaut.serde.write-dates-as-timestamps", value = "false")
class CloudEventTypesSerdeSpec extends Specification {
    @Inject
    JsonMapper jsonMapper

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
            retentionExpirationTime == OffsetDateTime.parse('2022-12-01T12:30:00.123Z')
            timeCreated == OffsetDateTime.parse('2022-01-01T12:30:00.123Z')
            updated == OffsetDateTime.parse('2022-04-01T12:30:00.123Z')
            timeDeleted == OffsetDateTime.parse('2022-02-01T12:30:00.123Z')
            timeStorageClassUpdated == OffsetDateTime.parse('2022-03-01T12:30:00.123Z')
            metadata.foo == 'hello'
            metadata.bar == 'world'
        }
    }

    void "serialize storage object"() {

        given:
        StorageObjectData data = [
                kind                   : 'storage#object',
                ID                     : 'my-bucket/dir/my-file.txt/123456789',
                selfLink               : 'https://www.googleapis.com/storage/v1/b/my-bucket/o/dir/my-file.txt',
                mediaLink              : 'https://www.googleapis.com/download/storage/v1/b/my-bucket/o/dir/my-file.txt?generation=123456789&alt=media',
                name                   : 'dir/my-file.txt',
                bucket                 : 'my-bucket',
                generation             : 123456789,
                metageneration         : 1,
                contentType            : 'text/plain',
                storageClass           : 'STANDARD',
                size                   : 1024,
                md5Hash                : '5d41402abc4b2a76b9719d911017c592',
                contentEncoding        : 'gzip',
                contentDisposition     : 'inline',
                contentLanguage        : 'en-US',
                cacheControl           : 'public, max-age=3600',
                crc32C                 : '3610a686',
                componentCount         : 1,
                etag                   : 'etag1',
                kmsKeyName             : 'projects/my-project/locations/us-central1/keyRings/my-keyring/cryptoKeys/my-key-us-central1/cryptoKeyVersions/2',
                temporaryHold          : false,
                eventBasedHold         : true,
                retentionExpirationTime: OffsetDateTime.parse('2022-12-01T12:30:00.123Z'),
                timeCreated            : OffsetDateTime.parse('2022-01-01T12:30:00.123Z'),
                updated                : OffsetDateTime.parse('2022-04-01T12:30:00.123Z'),
                timeDeleted            : OffsetDateTime.parse('2022-02-01T12:30:00.123Z'),
                timeStorageClassUpdated: OffsetDateTime.parse('2022-03-01T12:30:00.123Z'),
                metadata               : [foo: 'hello', bar: 'world']
        ]

        when:
        String json = jsonMapper.writeValueAsString(data)
        Map map = jsonMapper.readValue(json, Map)

        then:
        with(map) {
            kind == 'storage#object'
            ID == 'my-bucket/dir/my-file.txt/123456789'
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
            crc32C == '3610a686'
            componentCount == 1
            etag == 'etag1'
            kmsKeyName == 'projects/my-project/locations/us-central1/keyRings/my-keyring/cryptoKeys/my-key-us-central1/cryptoKeyVersions/2'
            temporaryHold == false
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
