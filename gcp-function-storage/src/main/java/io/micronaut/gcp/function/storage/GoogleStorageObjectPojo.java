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

import static io.cloudevents.types.Time.parseTime;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.cloud.storage.BlobId;
import io.micronaut.core.annotation.Internal;
import io.micronaut.serde.annotation.Serdeable;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Default implementation of {@link GoogleStorageObject}.
 */
@Internal
@Serdeable.Deserializable
final class GoogleStorageObjectPojo implements GoogleStorageObject {

    private String bucket;
    private String cacheControl;
    private String contentType;
    private String etag;
    private Long generation;
    private String id;
    private String md5Hash;
    private String crc32c;
    private String contentEncoding;
    private String contentDisposition;
    private String contentLanguage;
    private Integer componentCount;
    private String kind;
    private String kmsKeyName;
    private String mediaLink;
    private Long metageneration;
    private String name;
    private String retentionExpirationTime;
    private String selfLink;
    private Long size;
    private String storageClass;
    private Boolean temporaryHold;
    private Boolean eventBasedHold;
    private String timeCreated;
    private String timeDeleted;
    private String timeStorageClassUpdated;
    private String updated;
    private String customTime;
    private Map<String, Object> metadata;

    @Override
    @JsonGetter
    public String getBucket() {
        return this.bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    @Override
    @JsonGetter
    public String getCacheControl() {
        return this.cacheControl;
    }

    public void setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
    }

    @Override
    @JsonGetter
    public String getContentType() {
        return this.contentType;
    }

    @JsonSetter
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    @JsonGetter
    public String getEtag() {
        return this.etag;
    }

    @JsonSetter
    public void setEtag(String etag) {
        this.etag = etag;
    }

    @Override
    @JsonGetter
    public Long getGeneration() {
        return this.generation;
    }

    @JsonSetter
    public void setGeneration(Long generation) {
        this.generation = generation;
    }

    @Override
    @JsonGetter
    public String getId() {
        return this.id;
    }

    @JsonSetter
    public void setId(String id) {
        this.id = id;
    }

    @Override
    @JsonGetter
    public String getMd5Hash() {
        return this.md5Hash;
    }

    @JsonSetter
    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }

    @Override
    @JsonGetter
    public String getCrc32c() {
        return this.crc32c;
    }

    @JsonSetter
    public void setCrc32c(String crc32c) {
        this.crc32c = crc32c;
    }

    @Override
    @JsonGetter
    public Integer getComponentCount() {
        return this.componentCount;
    }

    @JsonSetter
    public void setComponentCount(Integer componentCount) {
        this.componentCount = componentCount;
    }

    @Override
    @JsonGetter
    public String getContentEncoding() {
        return this.contentEncoding;
    }

    @JsonSetter
    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    @Override
    @JsonGetter
    public String getContentDisposition() {
        return this.contentDisposition;
    }

    @JsonSetter
    public void setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    @Override
    @JsonGetter
    public String getContentLanguage() {
        return this.contentLanguage;
    }

    @JsonSetter
    public void setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    @Override
    @JsonGetter
    public String getKind() {
        return this.kind;
    }

    @JsonSetter
    public void setKind(String kind) {
        this.kind = kind;
    }

    @Override
    @JsonGetter
    public String getKmsKeyName() {
        return this.kmsKeyName;
    }

    @JsonSetter
    public void setKmsKeyName(String kmsKeyName) {
        this.kmsKeyName = kmsKeyName;
    }

    @Override
    @JsonGetter
    public Map<String, Object> getMetadata() {
        return this.metadata;
    }

    @JsonSetter
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    @JsonGetter
    public String getMediaLink() {
        return this.mediaLink;
    }

    @JsonSetter
    public void setMediaLink(String mediaLink) {
        this.mediaLink = mediaLink;
    }

    @Override
    @JsonGetter
    public Long getMetageneration() {
        return this.metageneration;
    }

    @JsonSetter
    public void setMetageneration(Long metageneration) {
        this.metageneration = metageneration;
    }

    @Override
    @JsonGetter
    public String getName() {
        return this.name;
    }

    @JsonSetter
    public void setName(String name) {
        this.name = name;
    }

    @Override
    @JsonGetter
    public OffsetDateTime getRetentionExpirationTime() {
        return asOffsetDateTime(this.retentionExpirationTime);
    }

    @JsonSetter
    public void setRetentionExpirationTime(String retentionExpirationTime) {
        this.retentionExpirationTime = retentionExpirationTime;
    }

    @Override
    @JsonGetter
    public String getSelfLink() {
        return this.selfLink;
    }

    @JsonSetter
    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    @Override
    @JsonGetter
    public Long getSize() {
        return this.size;
    }

    @JsonSetter
    public void setSize(Long size) {
        this.size = size;
    }

    @Override
    @JsonGetter
    public String getStorageClass() {
        return this.storageClass;
    }

    @JsonSetter
    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    @Override
    @JsonGetter
    public Boolean getTemporaryHold() {
        return this.temporaryHold;
    }

    @JsonSetter
    public void setTemporaryHold(Boolean temporaryHold) {
        this.temporaryHold = temporaryHold;
    }

    @Override
    @JsonGetter
    public Boolean getEventBasedHold() {
        return this.eventBasedHold;
    }

    @JsonSetter
    public void setEventBasedHold(Boolean eventBasedHold) {
        this.eventBasedHold = eventBasedHold;
    }

    @Override
    @JsonGetter
    public OffsetDateTime getTimeCreated() {
        return asOffsetDateTime(this.timeCreated);
    }

    @JsonSetter
    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    @Override
    @JsonGetter
    public OffsetDateTime getTimeDeleted() {
        return asOffsetDateTime(this.timeDeleted);
    }

    @JsonSetter
    public void setTimeDeleted(String timeDeleted) {
        this.timeDeleted = timeDeleted;
    }

    @Override
    @JsonGetter
    public OffsetDateTime getTimeStorageClassUpdated() {
        return asOffsetDateTime(this.timeStorageClassUpdated);
    }

    @JsonSetter
    public void setTimeStorageClassUpdated(String timeStorageClassUpdated) {
        this.timeStorageClassUpdated = timeStorageClassUpdated;
    }

    @Override
    @JsonGetter
    public OffsetDateTime getCustomTime() {
        return asOffsetDateTime(this.customTime);
    }

    @JsonSetter
    public void setCustomTime(String customTime) {
        this.customTime = customTime;
    }

    @Override
    @JsonGetter
    public OffsetDateTime getUpdated() {
        return asOffsetDateTime(this.updated);
    }

    @JsonSetter
    public void setUpdated(String updated) {
        this.updated = updated;
    }

    @Override
    public String toString() {
        return this.id;
    }

    @Override
    public BlobId toBlobId() {
        return BlobId.of(this.bucket, this.name, this.generation);
    }

    private static OffsetDateTime asOffsetDateTime(String datetime) {
        return datetime != null ? parseTime(datetime) : null;
    }
}
