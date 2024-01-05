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
package io.micronaut.gcp.pubsub.push;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.core.util.StringUtils;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Map;

@Serdeable
public record PushRequest(@NotBlank String subscription, @Valid @NotNull PushMessage message) {

    @Serdeable
    @ValidPushMessage
    public record PushMessage(Map<String, String> attributes, String data, @NotBlank String messageId, @NotBlank String publishTime) {

        PubsubMessage asPubsubMessage() {

            Instant publishTimeInstant = ZonedDateTime.parse(publishTime()).toInstant();
            Timestamp publishTimeStamp = Timestamp.newBuilder().setSeconds(publishTimeInstant.getEpochSecond()).setNanos(publishTimeInstant.getNano()).build();
            PubsubMessage.Builder messageBuilder = PubsubMessage.newBuilder().setMessageId(messageId())
                .setPublishTime(publishTimeStamp);

            if (StringUtils.isNotEmpty(data)) {
                messageBuilder.setData(ByteString.copyFrom(Base64.getDecoder().decode(data())));
            }

            if (attributes != null && !attributes.isEmpty()) {
                messageBuilder.putAllAttributes(attributes);
            }

            return messageBuilder.build();
        }
    }
}
