# tag::imports[]
import java
from micronaut.context.event import StartupEvent
from micronaut.runtime.event.annotation import EventListener
from reactor.core.publisher import Mono

# TODO(python): java.type needed because importing `io.micronaut.gcp.secretmanager` collides with the Python snippet package of the same name
SecretManagerClient = java.type("io.micronaut.gcp.secretmanager.client.SecretManagerClient")
VersionedSecret = java.type("io.micronaut.gcp.secretmanager.client.VersionedSecret")
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
