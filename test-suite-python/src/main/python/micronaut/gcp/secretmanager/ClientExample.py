# tag::imports[]
from micronaut.context.event import StartupEvent
from micronaut.gcp.secretmanager.client import SecretManagerClient, VersionedSecret
from micronaut.runtime.event.annotation import EventListener
from reactor.core.publisher import Mono
# end::imports[]


# tag::clazz[]
class ClientExample:

    def __init__(self, client: SecretManagerClient):
        self.client = client

    @EventListener
    def on_startup(self, event: StartupEvent) -> None:
        secret: Mono[VersionedSecret] = Mono.from_(self.client.getSecret("secretId"))  # <1>
        v2: Mono[VersionedSecret] = Mono.from_(self.client.getSecret("secretId", "v2"))  # <2>
        from_other_project: Mono[VersionedSecret] = Mono.from_(self.client.getSecret("secretId", "latest", "another-project-id"))  # <3>
# end::clazz[]
