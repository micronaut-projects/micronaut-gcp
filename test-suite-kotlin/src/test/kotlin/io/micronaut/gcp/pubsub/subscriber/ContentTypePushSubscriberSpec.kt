package io.micronaut.gcp.pubsub.subscriber

import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Property
import io.micronaut.gcp.pubsub.push.PushRequest
import io.micronaut.gcp.pubsub.support.Animal
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.json.JsonMapper
import io.micronaut.pubsub.testcontainers.PubSubEmulator
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import jakarta.inject.Named
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import reactor.core.publisher.Mono
import tools.jackson.dataformat.xml.XmlMapper
import java.nio.charset.StandardCharsets
import java.util.Base64

//tag::clazzBegin[]
@MicronautTest
@Property(name = "spec.name", value = "ContentTypePushSubscriberSpec")
@Property(name = "gcp.projectId", value = "test-project")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ContentTypePushSubscriberSpec : TestPropertyProvider {

    override fun getProperties(): Map<String, String> = PubSubEmulator.getProperties()

    @Inject
    lateinit var jsonMapper: JsonMapper
//end::clazzBegin[]

//tag::injectClient[]
    @Inject
    @field:Client("/")
    lateinit var pushClient: HttpClient
//end::injectClient[]

    @Inject
    @field:Named("xml")
    lateinit var xmlMapper: XmlMapper

    var receivedMessage: Any? = null

    @BeforeEach
    fun setup() {
        receivedMessage = null
    }

    @Test
    fun testRawBytes() {
        val bytesSent = "foo".toByteArray(StandardCharsets.UTF_8)
        val encodedData = Base64.getEncoder().encodeToString(bytesSent)
        val request = PushRequest("projects/test-project/subscriptions/raw-push-subscription",
            PushRequest.PushMessage(HashMap(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        val response = pushClient.toBlocking().exchange<PushRequest, Any>(HttpRequest.POST("/push", request))

        assertEquals(HttpStatus.OK, response.status)
        assertTrue(receivedMessage is ByteArray)
        assertEquals("foo", String(receivedMessage as ByteArray, StandardCharsets.UTF_8))
    }

    @Test
    fun testNativeMessage() {
        val bytesSent = "foo".toByteArray(StandardCharsets.UTF_8)
        val encodedData = Base64.getEncoder().encodeToString(bytesSent)
        val request = PushRequest("projects/test-project/subscriptions/native-push-subscription",
            PushRequest.PushMessage(HashMap(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        val response = pushClient.toBlocking().exchange<PushRequest, Any>(HttpRequest.POST("/push", request))

        assertEquals(HttpStatus.OK, response.status)
        assertTrue(receivedMessage is PubsubMessage)
        assertEquals("foo", (receivedMessage as PubsubMessage).data.toString(StandardCharsets.UTF_8))
    }

//tag::testMethodBegin[]
    @Test
    fun testJsonPojo() {
        val dog = Animal("dog")

        val encodedData = Base64.getEncoder().encodeToString(jsonMapper.writeValueAsBytes(dog)) // <1>

        val request = PushRequest("projects/test-project/subscriptions/animals-push", // <2>
            PushRequest.PushMessage(HashMap(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        val response = pushClient.toBlocking().exchange<PushRequest, Any>(HttpRequest.POST("/push", request)) // <3>

        assertEquals(HttpStatus.OK, response.status)
//end::testMethodBegin[]
        assertNotNull(receivedMessage)
        assertTrue(receivedMessage is Animal)
        assertEquals("dog", (receivedMessage as Animal).name)
//tag::testMethodEnd[]
    }
//end::testMethodEnd[]

    @Test
    fun testXmlPojo() {
        val cat = Animal("cat")
        val encodedData = Base64.getEncoder().encodeToString(xmlMapper.writeValueAsBytes(cat))
        val request = PushRequest("projects/test-project/subscriptions/animals-legacy-push",
            PushRequest.PushMessage(HashMap(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        val response = pushClient.toBlocking().exchange<PushRequest, Any>(HttpRequest.POST("/push", request))

        assertEquals(HttpStatus.OK, response.status)
        assertNotNull(receivedMessage)
        assertTrue(receivedMessage is Animal)
        assertEquals("cat", (receivedMessage as Animal).name)
    }

    @MockBean(MessageProcessor::class)
    fun messageProcessor(): MessageProcessor {
        return object : MessageProcessor() {

            override fun handleByteArrayMessage(message: ByteArray): Mono<Boolean> {
                receivedMessage = message
                return Mono.just(java.lang.Boolean.TRUE)
            }

            override fun handlePubsubMessage(pubsubMessage: PubsubMessage): Mono<Boolean> {
                receivedMessage = pubsubMessage
                return Mono.just(java.lang.Boolean.TRUE)
            }

            override fun handleAnimalMessage(message: Animal): Mono<Boolean> {
                receivedMessage = message
                return Mono.just(java.lang.Boolean.TRUE)
            }
        }
    }
//tag::clazzEnd[]
}
//end::clazzEnd[]
