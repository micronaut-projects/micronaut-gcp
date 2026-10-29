# tag::imports[]
from com.google.cloud.secretmanager.v1 import AccessSecretVersionRequest, AccessSecretVersionResponse, SecretManagerServiceClient, SecretVersionName
from micronaut.context.event import StartupEvent
from micronaut.runtime.event.annotation import EventListener
# end::imports[]


# tag::clazz[]
class GoogleLocationClientExample:

    def __init__(self, google_secret_manager_client: SecretManagerServiceClient):  # <1>
        self.client = google_secret_manager_client

    @EventListener
    def on_startup(self, event: StartupEvent) -> None:
        response: AccessSecretVersionResponse = self.client.accessSecretVersion(AccessSecretVersionRequest
                .newBuilder()
                .setName(SecretVersionName.ofProjectLocationSecretSecretVersionName("my-cloud-project", "us-central1", "secretName", "latest").toString())
                .build())
        secret = response.getPayload().getData().toStringUtf8()
# end::clazz[]
