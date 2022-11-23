/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.gcp.function.storage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.cloud.storage.BlobId;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Represents an object within Cloud Storage.
 * <p>
 * See <a href="https://cloud.google.com/storage/docs/json_api/v1/objects#resource-representations">GCP Documentation</a>.
 *
 * @author Guillermo Calvo
 */
@JsonDeserialize(as = GoogleStorageObjectPojo.class)
public interface GoogleStorageObject {

  /**
   * Returns the kind of item this is.
   *
   * @return The kind of item this is.
   */
  String getKind();

  /**
   * Returns the ID of the object.
   *
   * @return The ID of the object.
   */
  String getId();

  /**
   * Returns the link to this object.
   *
   * @return The link to this object.
   */
  String getSelfLink();

  /**
   * Returns media download link.
   *
   * @return Media download link.
   */
  String getMediaLink();

  /**
   * Returns the name of the object.
   *
   * @return The name of the object.
   */
  String getName();

  /**
   * Returns the name of the bucket containing this object.
   *
   * @return The name of the bucket containing this object.
   */
  String getBucket();

  /**
   * Returns the content generation of this object.
   *
   * @return The content generation of this object.
   */
  Long getGeneration();

  /**
   * Returns the version of the metadata for this object at this generation.
   *
   * @return The version of the metadata for this object at this generation.
   */
  Long getMetageneration();

  /**
   * Returns Content-Type of the object data.
   *
   * @return Content-Type of the object data.
   */
  String getContentType();

  /**
   * Returns storage class of the object.
   *
   * @return Storage class of the object.
   */
  String getStorageClass();

  /**
   * Returns Content-Length of the data in bytes.
   *
   * @return Content-Length of the data in bytes.
   */
  Long getSize();

  /**
   * Returns MD5 hash of the data.
   *
   * @return MD5 hash of the data.
   */
  String getMd5Hash();

  /**
   * Returns Content-Encoding of the object data.
   *
   * @return Content-Encoding of the object data.
   */
  String getContentEncoding();

  /**
   * Returns Content-Disposition of the object data.
   *
   * @return Content-Disposition of the object data.
   */
  String getContentDisposition();

  /**
   * Returns Content-Language of the object data.
   *
   * @return Content-Language of the object data.
   */
  String getContentLanguage();

  /**
   * Returns Cache-Control directive for the object data.
   *
   * @return Cache-Control directive for the object data.
   */
  String getCacheControl();

  /**
   * Returns CRC32c checksum.
   *
   * @return CRC32c checksum.
   */
  String getCrc32c();

  /**
   * Returns number of non-composite objects in the composite object.
   *
   * @return Number of non-composite objects in the composite object.
   */
  Integer getComponentCount();

  /**
   * Returns HTTP 1.1 Entity tag for the object.
   *
   * @return HTTP 1.1 Entity tag for the object.
   */
  String getEtag();

  /**
   * Returns cloud KMS key used to encrypt this object.
   *
   * @return Cloud KMS key used to encrypt this object.
   */
  String getKmsKeyName();

  /**
   * Returns whether or not the object is subject to a temporary hold.
   *
   * @return Whether or not the object is subject to a temporary hold.
   */
  Boolean getTemporaryHold();

  /**
   * Returns whether or not the object is subject to an event-based hold.
   *
   * @return Whether or not the object is subject to an event-based hold.
   */
  Boolean getEventBasedHold();

  /**
   * Returns the earliest time that the object can be deleted in RFC 3339 format.
   *
   * @return The earliest time that the object can be deleted in RFC 3339 format.
   */
  OffsetDateTime getRetentionExpirationTime();

  /**
   * Returns the creation time of the object in RFC 3339 format.
   *
   * @return The creation time of the object in RFC 3339 format.
   */
  OffsetDateTime getTimeCreated();

  /**
   * Returns the modification time of the object metadata in RFC 3339 format.
   *
   * @return The modification time of the object metadata in RFC 3339 format.
   */
  OffsetDateTime getUpdated();

  /**
   * Returns the deletion time of the object in RFC 3339 format.
   *
   * @return The deletion time of the object in RFC 3339 format.
   */
  OffsetDateTime getTimeDeleted();

  /**
   * Returns the time at which the object's storage class was last changed.
   *
   * @return The time at which the object's storage class was last changed.
   */
  OffsetDateTime getTimeStorageClassUpdated();

  /**
   * Returns a user-specified timestamp for the object in RFC 3339 format.
   *
   * @return A user-specified timestamp for the object in RFC 3339 format.
   */
  OffsetDateTime getCustomTime();

  /**
   * Returns user-provided metadata, in key/value pairs.
   *
   * @return User-provided metadata, in key/value pairs.
   */
  Map<String, Object> getMetadata();

  /**
   * Creates a BlobId object based on this GoogleBlobInfo.
   *
   * @return A BlobId object based on this GoogleBlobInfo.
   */
  BlobId toBlobId();
}
