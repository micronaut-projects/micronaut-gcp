/*
 * Copyright 2017-2026 original authors
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
package io.micronaut.gcp.serde.cloudevents;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.events.cloud.audit.v1.LogEntryData;
import com.google.events.cloud.cloudbuild.v1.BuildEventData;
import com.google.events.cloud.firestore.v1.DocumentEventData;
import com.google.events.cloud.pubsub.v1.MessagePublishedData;
import com.google.events.cloud.scheduler.v1.SchedulerJobData;
import com.google.events.cloud.storage.v1.StorageObjectData;
import com.google.events.firebase.analytics.v1.AnalyticsLogData;
import com.google.events.firebase.auth.v1.AuthEventData;
import com.google.events.firebase.database.v1.ReferenceEventData;
import com.google.events.firebase.remoteconfig.v1.RemoteConfigEventData;
import com.google.protobuf.Internal;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.Deserializer;
import io.micronaut.serde.Encoder;
import io.micronaut.serde.Serde;
import io.micronaut.serde.Serializer;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * @see <a href="https://github.com/googleapis/google-cloudevents-java">Google Cloud Events</a>.
 * @author Sergio del Amo
 * @since 4.8.0
 */
@Singleton
@Factory
final class CloudEventTypesSerde {
    private static final Gson GSON = new Gson();
    private static final JsonFormat.Parser PARSER = JsonFormat.parser().ignoringUnknownFields();
    private static final JsonFormat.Printer PRINTER = JsonFormat.printer().omittingInsignificantWhitespace();

    @Singleton
    Serde<ReferenceEventData> referenceEventDataSerde() {
        return serde(ReferenceEventData.class);
    }

    @Singleton
    Serde<AuthEventData> authEventDataSerde() {
        return serde(AuthEventData.class);
    }

    @Singleton
    Serde<RemoteConfigEventData> remoteConfigEventDataSerde() {
        return serde(RemoteConfigEventData.class);
    }

    @Singleton
    Serde<AnalyticsLogData> analyticsLogDataSerde() {
        return serde(AnalyticsLogData.class);
    }

    @Singleton
    Serde<DocumentEventData> documentEventDataSerde() {
        return serde(DocumentEventData.class);
    }

    @Singleton
    Serde<BuildEventData> buildEventDataSerde() {
        return serde(BuildEventData.class);
    }

    @Singleton
    Serde<SchedulerJobData> schedulerJobDataSerde() {
        return serde(SchedulerJobData.class);
    }

    @Singleton
    Serde<StorageObjectData> storageObjectDataSerde() {
        return serde(StorageObjectData.class);
    }

    @Singleton
    Serde<LogEntryData> logEntryDataSerde() {
        return serde(LogEntryData.class);
    }

    @Singleton
    Serde<MessagePublishedData> messagePublishedDataSerde() {
        return serde(MessagePublishedData.class);
    }

    private static <T extends Message> Serde<T> serde(Class<T> type) {
        return new ProtobufMessageSerde<>(type);
    }

    private static final class ProtobufMessageSerde<T extends Message> implements Serde<T> {
        private final Class<T> type;

        private ProtobufMessageSerde(Class<T> type) {
            this.type = type;
        }

        @Override
        public T deserialize(Decoder decoder,
                             Deserializer.DecoderContext context,
                             Argument<? super T> type) throws IOException {
            Message defaultInstance = Internal.getDefaultInstance(this.type);
            Message.Builder builder = defaultInstance.newBuilderForType();
            PARSER.merge(GSON.toJson(decoder.decodeArbitrary()), builder);
            return this.type.cast(builder.build());
        }

        @Override
        public void serialize(Encoder encoder,
                              Serializer.EncoderContext context,
                              Argument<? extends T> type,
                              T value) throws IOException {
            if (value == null) {
                encoder.encodeNull();
                return;
            }
            encodeJson(encoder, JsonParser.parseString(PRINTER.print(value)), type);
        }

        private static void encodeJson(Encoder encoder, JsonElement value, Argument<?> type) throws IOException {
            if (value.isJsonNull()) {
                encoder.encodeNull();
            } else if (value.isJsonObject()) {
                encodeObject(encoder, value.getAsJsonObject(), type);
            } else if (value.isJsonArray()) {
                encodeArray(encoder, value.getAsJsonArray());
            } else {
                encodePrimitive(encoder, value.getAsJsonPrimitive());
            }
        }

        private static void encodeObject(Encoder encoder, JsonObject value, Argument<?> type) throws IOException {
            try (Encoder objectEncoder = encoder.encodeObject(type)) {
                for (Map.Entry<String, JsonElement> entry : value.entrySet()) {
                    objectEncoder.encodeKey(entry.getKey());
                    encodeJson(objectEncoder, entry.getValue(), Argument.OBJECT_ARGUMENT);
                }
            }
        }

        private static void encodeArray(Encoder encoder, JsonArray value) throws IOException {
            try (Encoder arrayEncoder = encoder.encodeArray(Argument.OBJECT_ARGUMENT)) {
                for (JsonElement element : value) {
                    encodeJson(arrayEncoder, element, Argument.OBJECT_ARGUMENT);
                }
            }
        }

        private static void encodePrimitive(Encoder encoder, JsonPrimitive value) throws IOException {
            if (value.isBoolean()) {
                encoder.encodeBoolean(value.getAsBoolean());
            } else if (value.isNumber()) {
                encoder.encodeBigDecimal(new BigDecimal(value.getAsString()));
            } else {
                encoder.encodeString(value.getAsString());
            }
        }
    }
}
