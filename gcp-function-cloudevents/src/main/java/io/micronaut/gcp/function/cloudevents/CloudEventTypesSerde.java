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
package io.micronaut.gcp.function.cloudevents;

import com.google.events.cloud.audit.v1.Auth;
import com.google.events.cloud.audit.v1.AuthenticationInfo;
import com.google.events.cloud.audit.v1.AuthorizationInfo;
import com.google.events.cloud.audit.v1.DestinationAttributes;
import com.google.events.cloud.audit.v1.Detail;
import com.google.events.cloud.audit.v1.FirstPartyPrincipal;
import com.google.events.cloud.audit.v1.LogEntryData;
import com.google.events.cloud.audit.v1.Operation;
import com.google.events.cloud.audit.v1.ProtoPayload;
import com.google.events.cloud.audit.v1.Request;
import com.google.events.cloud.audit.v1.RequestMetadata;
import com.google.events.cloud.audit.v1.ResourceLocation;
import com.google.events.cloud.audit.v1.ServiceAccountDelegationInfo;
import com.google.events.cloud.audit.v1.Severity;
import com.google.events.cloud.cloudbuild.v1.ArtifactTiming;
import com.google.events.cloud.cloudbuild.v1.Artifacts;
import com.google.events.cloud.cloudbuild.v1.BuildEventData;
import com.google.events.cloud.cloudbuild.v1.LogStreamingOption;
import com.google.events.cloud.cloudbuild.v1.Logging;
import com.google.events.cloud.cloudbuild.v1.MachineType;
import com.google.events.cloud.cloudbuild.v1.Objects;
import com.google.events.cloud.cloudbuild.v1.ObjectsTiming;
import com.google.events.cloud.cloudbuild.v1.Options;
import com.google.events.cloud.cloudbuild.v1.PullTiming;
import com.google.events.cloud.cloudbuild.v1.PushTiming;
import com.google.events.cloud.cloudbuild.v1.RepoSourceClass;
import com.google.events.cloud.cloudbuild.v1.RequestedVerifyOption;
import com.google.events.cloud.cloudbuild.v1.ResolvedRepoSourceClass;
import com.google.events.cloud.cloudbuild.v1.ResolvedStorageSourceClass;
import com.google.events.cloud.cloudbuild.v1.Results;
import com.google.events.cloud.cloudbuild.v1.Secret;
import com.google.events.cloud.cloudbuild.v1.Source;
import com.google.events.cloud.cloudbuild.v1.SourceProvenance;
import com.google.events.cloud.cloudbuild.v1.SourceProvenanceHash;
import com.google.events.cloud.cloudbuild.v1.StepTiming;
import com.google.events.cloud.cloudbuild.v1.StorageSourceClass;
import com.google.events.cloud.cloudbuild.v1.SubstitutionOption;
import com.google.events.cloud.cloudbuild.v1.TimeSpan;
import com.google.events.cloud.cloudbuild.v1.Type;
import com.google.events.cloud.cloudbuild.v1.Volume;
import com.google.events.cloud.firestore.v1.ArrayValue;
import com.google.events.cloud.firestore.v1.DocumentEventData;
import com.google.events.cloud.firestore.v1.GeoPointValue;
import com.google.events.cloud.firestore.v1.MapValue;
import com.google.events.cloud.firestore.v1.MapValueField;
import com.google.events.cloud.firestore.v1.NullValue;
import com.google.events.cloud.firestore.v1.OldValue;
import com.google.events.cloud.firestore.v1.OldValueField;
import com.google.events.cloud.firestore.v1.ValueElement;
import com.google.events.cloud.pubsub.v1.Message;
import com.google.events.cloud.pubsub.v1.MessagePublishedData;
import com.google.events.cloud.scheduler.v1.SchedulerJobData;
import com.google.events.cloud.storage.v1.CustomerEncryption;
import com.google.events.cloud.storage.v1.StorageObjectData;
import com.google.events.firebase.analytics.v1.AnalyticsLogData;
import com.google.events.firebase.analytics.v1.AnalyticsValue;
import com.google.events.firebase.analytics.v1.AppInfo;
import com.google.events.firebase.analytics.v1.BundleInfo;
import com.google.events.firebase.analytics.v1.DeviceInfo;
import com.google.events.firebase.analytics.v1.GeoInfo;
import com.google.events.firebase.analytics.v1.LtvInfo;
import com.google.events.firebase.analytics.v1.TrafficSource;
import com.google.events.firebase.analytics.v1.UserDim;
import com.google.events.firebase.auth.v1.AuthEventData;
import com.google.events.firebase.database.v1.ReferenceEventData;
import com.google.events.firebase.remoteconfig.v1.RemoteConfigEventData;
import com.google.events.firebase.remoteconfig.v1.UpdateOrigin;
import com.google.events.firebase.remoteconfig.v1.UpdateType;
import io.micronaut.serde.annotation.SerdeImport;

/**
 * @see <a href="https://github.com/googleapis/google-cloudevents-java">Google Cloud Events</a>.
 * @author Sergio del Amo
 * @since 4.8.0
 */
@SerdeImport(ReferenceEventData.class)
@SerdeImport(com.google.events.cloud.audit.v1.Metadata.class)
@SerdeImport(com.google.events.firebase.auth.v1.Metadata.class)
@SerdeImport(AuthEventData.class)
@SerdeImport(UpdateType.class)
@SerdeImport(UpdateOrigin.class)
@SerdeImport(RemoteConfigEventData.class)
@SerdeImport(AnalyticsLogData.class)
@SerdeImport(AppInfo.class)
@SerdeImport(UserDim.class)
@SerdeImport(AnalyticsValue.class)
@SerdeImport(BundleInfo.class)
@SerdeImport(DeviceInfo.class)
@SerdeImport(TrafficSource.class)
@SerdeImport(com.google.events.firebase.analytics.v1.Value.class)
@SerdeImport(com.google.events.cloud.firestore.v1.Value.class)
@SerdeImport(LtvInfo.class)
@SerdeImport(GeoInfo.class)
@SerdeImport(OldValue.class)
@SerdeImport(GeoPointValue.class)
@SerdeImport(OldValueField.class)
@SerdeImport(DocumentEventData.class)
@SerdeImport(MapValue.class)
@SerdeImport(NullValue.class)
@SerdeImport(MapValueField.class)
@SerdeImport(ValueElement.class)
@SerdeImport(ArrayValue.class)
@SerdeImport(Artifacts.class)
@SerdeImport(ObjectsTiming.class)
@SerdeImport(BuildEventData.class)
@SerdeImport(com.google.events.cloud.audit.v1.Status.class)
@SerdeImport(com.google.events.cloud.cloudbuild.v1.Status.class)
@SerdeImport(ResolvedRepoSourceClass.class)
@SerdeImport(Objects.class)
@SerdeImport(Type.class)
@SerdeImport(PushTiming.class)
@SerdeImport(Secret.class)
@SerdeImport(LogStreamingOption.class)
@SerdeImport(MachineType.class)
@SerdeImport(RequestedVerifyOption.class)
@SerdeImport(StorageSourceClass.class)
@SerdeImport(TimeSpan.class)
@SerdeImport(ArtifactTiming.class)
@SerdeImport(Results.class)
@SerdeImport(SourceProvenance.class)
@SerdeImport(PullTiming.class)
@SerdeImport(Options.class)
@SerdeImport(Source.class)
@SerdeImport(RepoSourceClass.class)
@SerdeImport(SourceProvenanceHash.class)
@SerdeImport(SubstitutionOption.class)
@SerdeImport(ResolvedStorageSourceClass.class)
@SerdeImport(Volume.class)
@SerdeImport(Logging.class)
@SerdeImport(StepTiming.class)
@SerdeImport(SchedulerJobData.class)
@SerdeImport(StorageObjectData.class)
@SerdeImport(CustomerEncryption.class)
@SerdeImport(AuthorizationInfo.class)
@SerdeImport(ProtoPayload.class)
@SerdeImport(ResourceLocation.class)
@SerdeImport(Auth.class)
@SerdeImport(Operation.class)
@SerdeImport(DestinationAttributes.class)
@SerdeImport(Severity.class)
@SerdeImport(FirstPartyPrincipal.class)
@SerdeImport(RequestMetadata.class)
@SerdeImport(Detail.class)
@SerdeImport(LogEntryData.class)
@SerdeImport(ServiceAccountDelegationInfo.class)
@SerdeImport(Request.class)
@SerdeImport(AuthenticationInfo.class)
@SerdeImport(Message.class)
@SerdeImport(MessagePublishedData.class)
@SerdeImport(CustomerEncryption.class)
public class CloudEventTypesSerde {
}
