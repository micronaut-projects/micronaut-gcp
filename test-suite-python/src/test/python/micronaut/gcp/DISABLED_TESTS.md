# Python Docs Disabled Test Inventory

This file tracks Python docs examples of Micronaut GCP that are present but disabled, or that deviate from the
Java example because the direct port currently fails compilation or at runtime. Use it as the bug-fixing task list
for the final migration wave.

## Reconciliation

- Last generated active `@Disabled` count: 2.
- Last generated command: `rg -n "@Disabled\\(" test-suite-python/src/test/python`.
- Last full-suite command: `./gradlew :test-suite-python:test -Ppython-ci --max-workers=1`.
- Last full-suite result: build successful, 24 tests executed, 2 skipped.

## Migration Rules

- Do not define local copies of Micronaut annotation helpers or custom annotation shims in docs snippets. Standard
  Micronaut and Pub/Sub annotations are generated from imports (`from micronaut.gcp.pubsub.annotation import
  PubSubClient, PubSubListener, Subscription, PushSubscription, Topic, MessageId, OrderingKey`).
- `@PubSubClient` interfaces are abstract classes (`ABC`) whose abstract methods have `...` bodies; `@PubSubListener`
  beans are plain classes with `@Subscription`/`@PushSubscription` methods. A listener method body must not be a bare
  `...`: a class whose methods all have `...` bodies is compiled as an abstract type and is not an injectable bean
  ("Could not find the bean to execute the method"), so empty Java bodies are ported as `pass`.
- Python has no method overloading: the overloads of `SimpleClient`/`CustomSerDesClient` (`send(PubsubMessage)`,
  `send(byte[])`, `send(Animal)`) are ported as `send_message`, `send_bytes`, `send_animal`, ...
- Do not add Java-style getters or setters to Python docs models. Prefer `@Serdeable @dataclass` models.
- Methods that implement or override a Java interface keep the Java (camelCase) name; other methods are snake_case.
- Java classes are imported (`from reactor.core.publisher import Mono`, `from com.google.pubsub.v1 import PubsubMessage`,
  `from micronaut.gcp.pubsub.push import PushRequest`); imported classes are also accepted as runtime type arguments
  (`java.instanceof(received, PubsubMessage)`), and nested classes are attributes of the imported outer class
  (`PushRequest.PushMessage(...)`, `ArgumentBinder.BindingResult[object]`). `java.type(...)` is used only where the
  import form cannot work (see "java.type usages" below), each marked with a `# TODO(python)` comment.
- Python sources cannot live in a package whose Java counterpart is imported: the Pub/Sub module packages
  `io.micronaut.gcp.pubsub.bind`, `.serdes`, `.support` and `io.micronaut.gcp.secretmanager` are also snippet
  packages, so their Java types are referenced with `java.type(...)` (`PubSubAnnotatedArgumentBinder`,
  `PubSubConsumerState`, `PubSubMessageSerDes`, `SecretManagerClient`, `PublisherFactory`, ...) instead of
  `from micronaut.gcp.pubsub.bind import ...`, which generates a shim module colliding with the Python package
  ("Failed to write Python code to [.../__init__.py]: Output stream or writer has already been opened"). A Python
  compiler fix for this collision is pending; once released these `java.type` calls become plain imports.
- The test publishers (`@PubSubClient` classes declared in the test modules) are injected into the test class
  (`publisher: Annotated[AcknowledgementTestPublisher, Inject]`) like in the Java tests.
- The documentation classes live in `src/main/python` (`source="main"` snippets) and the tests in `src/test/python`.
  The Python compiler resolves the imports of a source file only within its own source root, and two compiled
  roots yield two GraalPy VFS roots shadowing each other, so `test-suite-python/build.gradle` merges both roots
  into one directory (`mergePythonSources`) compiled with the tests.
- The Java, Kotlin and Groovy suites run the Pub/Sub emulator in Docker (`test-suite-utils`); the Python tests
  replace `DefaultPublisherFactory`/`DefaultSubscriberFactory` with Python `@Replaces` beans backed by an in-memory
  `MockPubSubEngine` (`micronaut.gcp.pubsub.testsupport`), which also records the ack/nack replies the
  acknowledgement tests assert on (the emulator based Java tests assert redelivery instead).

## Active `@Disabled` Tests

| Test | Reason |
| --- | --- |
| `io.micronaut.gcp.pubsub.subscriber.ReactiveSubscriberTest#test_raw_bytes`, `io.micronaut.gcp.pubsub.subscriber.ReactivePushSubscriberTest#test_raw_bytes` | `bytes` as a generic type argument (`data: Mono[bytes]`) is compiled to `Mono<Byte>` (and `java.type("byte[]")` to `Mono<Object>`), so `PubSubBodyBinder` cannot resolve `byte[]` as the body type and tries to decode the raw payload as JSON. The `receive_raw` snippet methods are ported as in Java. |

## Commented Unsupported Snippet Ports

None.

## Workarounds Kept In Snippets

| Target | Reason |
| --- | --- |
| `io.micronaut.gcp.pubsub.bind.MessagePublishTimeAnnotationBinder` | Implements the raw `PubSubAnnotatedArgumentBinder` interface: with `PubSubAnnotatedArgumentBinder[MessagePublishTime]` the generated `bind` method returns `BindingResult<MessagePublishTime>` instead of `BindingResult<Object>` and does not compile. `getAnnotationType()` returns the generated annotation class via `java.type("micronaut.gcp.pubsub.bind.MessagePublishTime")`. |
| `io.micronaut.gcp.pubsub.subscriber.ReactiveSubscriber`, `ReactivePushSubscriber`, `AcknowledgementSubscriber`, `AcknowledgementPushSubscriber` | Reactive listener methods are declared to return `Publisher[...]` instead of `Mono[...]`: the generated bridge converts the Python result with `PythonHttpConversion.convertPublisher(...)` to a plain `Publisher` and then casts it to `Mono` (`ClassCastException`). The `Mono` *parameters* work. |
| `io.micronaut.gcp.pubsub.subscriber.MessageProcessor` | An `ABC` with abstract methods (compiled to a Java interface) instead of the Java interface with default methods: a Python class extending a plain Python class is not compiled as a subtype of it, so the test mocks would not be candidates for the `MessageProcessor` injection point. |
| `io.micronaut.gcp.pubsub.PythonRuntimeInitializer` (Java, `src/test/java`) | `@Executable(processOnStartup = true)` processors such as the Pub/Sub consumer advice are created before the `@Context` beans, so the Python beans they depend on (SerDes, subscriber factory, binders, listeners) would be instantiated before the GraalPy runtime exists ("GraalPy context has not been initialized"). The Java `TypeConverterRegistrar` injects the GraalPy context bean, which creates it during `initializeTypeConverters()`, before those processors. |

## java.type usages

Remaining `java.type(...)` calls (all marked with `# TODO(python): java.type needed because ...`):

| Target | Usage | Reason |
| --- | --- | --- |
| `io.micronaut.gcp.pubsub.bind.MessagePublishTimeAnnotationBinder` | `PubSubAnnotatedArgumentBinder`, `PubSubConsumerState` | Importing `io.micronaut.gcp.pubsub.bind` generates a shim module colliding with the Python snippet package `micronaut.gcp.pubsub.bind` (compiler fix pending). |
| `io.micronaut.gcp.pubsub.bind.MessagePublishTimeAnnotationBinder` | `MessagePublishTimeClass` (`micronaut.gcp.pubsub.bind.MessagePublishTime`) | `getAnnotationType()` must return the generated Java annotation `Class`; the Python annotation is a decorator function, not a `Class`. |
| `io.micronaut.gcp.pubsub.subscriber.ErrorHandlingSubscriber` | `PubSubConsumerState` | Package collision with `micronaut.gcp.pubsub.bind` (see above). |
| `io.micronaut.gcp.pubsub.serdes.JavaMessageSerDes`, `XmlMessageSerDes` | `PubSubMessageSerDes` | Package collision with `micronaut.gcp.pubsub.serdes`. |
| `io.micronaut.gcp.secretmanager.ClientExample` | `SecretManagerClient`, `VersionedSecret` | Package collision with `micronaut.gcp.secretmanager` (`from micronaut.gcp.secretmanager.client import ...` also writes `micronaut/gcp/secretmanager/__init__.py`). |
| `micronaut.gcp.pubsub.testsupport.MockPublisherFactory` (test support) | `PublisherFactory`, `PublisherFactoryConfig` | Package collision with `micronaut.gcp.pubsub.support`. |
| `micronaut.gcp.pubsub.testsupport.MockSubscriberFactory` (test support) | `SubscriberFactory`, `SubscriberFactoryConfig` | Package collision with `micronaut.gcp.pubsub.bind`. |

## Intentionally Unsupported Snippet Targets

| Target | Reason |
| --- | --- |
| `example.background.Example`, `example.cloudevents.Example` (`languages="java,kotlin,groovy"`) | A Google Cloud Function needs a Java entry point class extending `GoogleFunctionInitializer` that the Functions Framework instantiates before any application context, and with it the GraalPy runtime, exists; a Python class cannot extend a Java class either. The guide shows a `[.lang-python]` note instead. |
