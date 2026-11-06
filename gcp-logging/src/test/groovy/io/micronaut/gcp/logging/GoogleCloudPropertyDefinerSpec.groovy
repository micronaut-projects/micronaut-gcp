package io.micronaut.gcp.logging

import com.sun.net.httpserver.HttpServer
import spock.lang.Requires
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicInteger

@Requires({ !System.getenv('MICRONAUT_ENVIRONMENTS') && !System.getProperty('micronaut.environments') })
class GoogleCloudPropertyDefinerSpec extends Specification {

    HttpServer server = HttpServer.create(new InetSocketAddress('127.0.0.1', 0), 0)

    AtomicInteger requests = new AtomicInteger()

    void cleanup() {
        server.stop(0)
    }

    void startMetadataServer(int failures) {
        server.createContext('/') { exchange ->
            if (requests.incrementAndGet() <= failures) {
                // what a service mesh proxy answers before the sidecar is ready
                exchange.sendResponseHeaders(503, -1)
            } else {
                exchange.responseHeaders.add('Metadata-Flavor', 'Google')
                exchange.sendResponseHeaders(200, -1)
            }
            exchange.close()
        }
        server.start()
    }

    GoogleCloudPropertyDefiner definer(int maxAttempts) {
        def definer = new GoogleCloudPropertyDefiner()
        definer.metadataUrl = "http://127.0.0.1:${server.address.port}"
        definer.maxAttempts = maxAttempts
        definer.retryDelay = 10
        definer
    }

    void "detects Google Cloud when the metadata server answers after a transient failure"() {
        given:
        startMetadataServer(1)

        expect:
        definer(3).propertyValue == 'CONSOLE_JSON'
        requests.get() == 2
    }

    void "gives up after the configured number of attempts"() {
        given:
        startMetadataServer(5)

        expect:
        definer(2).propertyValue == 'STDOUT'
        requests.get() == 2
    }

    void "a single attempt keeps the previous behaviour"() {
        given:
        startMetadataServer(1)

        expect:
        definer(1).propertyValue == 'STDOUT'
        requests.get() == 1
    }

    void "a metadata host that does not resolve is not retried"() {
        given:
        def definer = new GoogleCloudPropertyDefiner()
        definer.metadataUrl = 'http://metadata.invalid'
        definer.retryDelay = 5_000

        when:
        long start = System.nanoTime()
        String value = definer.propertyValue

        then:
        value == 'STDOUT'
        (System.nanoTime() - start) < 5_000_000_000L
    }
}
