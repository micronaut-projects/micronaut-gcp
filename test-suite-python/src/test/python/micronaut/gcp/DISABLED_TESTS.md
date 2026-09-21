# Python Docs Disabled Test Inventory

This file tracks Python docs examples of Micronaut GCP that are present but disabled, or that deviate from the
Java example because the direct port currently fails compilation or at runtime. Use it as the bug-fixing task list
for the final migration wave.

## Reconciliation

- Last generated active `@Disabled` count: 0.
- Last generated command: `rg -n "@Disabled\\(" test-suite-python/src/test/python`.
- Last full-suite command: `./gradlew :test-suite-python:test -Ppython-ci --max-workers=1`.
- Last full-suite result: build successful, 24 tests executed, 0 skipped (micronaut-core 5.2.3, micronaut-build 8.1.2).

## Migration Rules

- Do not define local copies of Micronaut annotation helpers or custom annotation shims in docs snippets. Standard
  Micronaut and Pub/Sub annotations are generated from imports (`from micronaut.gcp.pubsub.annotation import
  PubSubClient, PubSubListener, Subscription, PushSubscription, Topic, MessageId, OrderingKey`).
- `@PubSubClient` interfaces are abstract classes (`ABC`) whose abstract methods have `...` bodies; `@PubSubListener`
  beans are plain classes with `@Subscription`/`@PushSubscription` methods (empty listener bodies are `...`, as in Java).
- Python has no method overloading: the overloads of `SimpleClient`/`CustomSerDesClient` (`send(PubsubMessage)`,
  `send(byte[])`, `send(Animal)`) are ported as `send_message`, `send_bytes`, `send_animal`, ...
- Do not add Java-style getters or setters to Python docs models. Prefer `@Serdeable @dataclass` models.
- Methods that implement or override a Java interface keep the Java (camelCase) name; other methods are snake_case.
- Java classes are imported (`from reactor.core.publisher import Mono`, `from com.google.pubsub.v1 import PubsubMessage`,
  `from micronaut.gcp.pubsub.bind import PubSubAnnotatedArgumentBinder, PubSubConsumerState`); imported classes are
  also accepted as runtime type arguments (`java.instanceof(received, PubsubMessage)`), and nested classes are
  attributes of the imported outer class (`PushRequest.PushMessage(...)`, `ArgumentBinder.BindingResult[object]`).
  `java.type(...)` is used only where the import form cannot work (see "java.type usages" below), marked `# TODO(python)`.
- The documentation classes live in `src/main/python` (`source="main"` snippets) and the tests in `src/test/python`;
  the tests import them with absolute module paths (`from micronaut.gcp.pubsub.support.Animal import Animal`).
- The test publishers (`@PubSubClient` classes declared in the test modules) are injected into the test class
  (`publisher: Annotated[AcknowledgementTestPublisher, Inject]`) like in the Java tests.
- The Java, Kotlin and Groovy suites run the Pub/Sub emulator in Docker (`test-suite-utils`); the Python tests
  replace `DefaultPublisherFactory`/`DefaultSubscriberFactory` with Python `@Replaces` beans backed by an in-memory
  `MockPubSubEngine` (`micronaut.gcp.pubsub.testsupport`), which also records the ack/nack replies the
  acknowledgement tests assert on (the emulator based Java tests assert redelivery instead).

## Active `@Disabled` Tests

None.

## Commented Unsupported Snippet Ports

None.

## java.type usages

| Target | Usage | Reason |
| --- | --- | --- |
| `io.micronaut.gcp.pubsub.bind.MessagePublishTimeAnnotationBinder` | `MessagePublishTimeClass` (`micronaut.gcp.pubsub.bind.MessagePublishTime`) | `getAnnotationType()` must return the generated Java annotation `Class`; a Python-defined annotation is a decorator function at runtime and is not converted to a `Class` ("Cannot convert '<function MessagePublishTime>' to Java type 'java.lang.Class'"), unlike imported Java annotations. |

## Intentionally Unsupported Snippet Targets

| Target | Reason |
| --- | --- |
| `example.background.Example`, `example.cloudevents.Example` (`languages="java,kotlin,groovy"`) | A Google Cloud Function entry point is instantiated reflectively by the Functions Framework before any application context, and with it the GraalPy runtime, exists. A Python `Example(GoogleFunctionInitializer, BackgroundFunction[PubSubMessage])` compiles, but its generated no-argument constructor fails with "GraalPy context has not been initialized" when instantiated that way. The guide shows a `[.lang-python]` note instead. |
