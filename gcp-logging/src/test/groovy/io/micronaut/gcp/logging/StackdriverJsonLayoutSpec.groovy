package io.micronaut.gcp.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.LoggingEvent
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class StackdriverJsonLayoutSpec extends Specification {
    StackdriverJsonLayout stackdriverJsonLayout = new StackdriverJsonLayout()
    LoggerContext loggerContext = new LoggerContext()

    void "should properly map log level to GCP severity"() {
        given:
        def event = new LoggingEvent(
                this.class.canonicalName, loggerContext.getLogger(this.class), logLevel, 'the message', new IllegalArgumentException('exception message'), new Object[]{}
        )
        event.setMDCPropertyMap([:])

        when:
        def result = stackdriverJsonLayout.toJsonMap(event)

        then:
        result['severity'] == expectedSeverity
        result['message'] == 'the message\njava.lang.IllegalArgumentException: exception message\n'
        result['thread'] == 'main'
        result['logger'] == this.class.canonicalName
        result['timestampSeconds'] == TimeUnit.MILLISECONDS.toSeconds(event.timeStamp)
        result['timestampNanos'] == TimeUnit.MILLISECONDS.toNanos(event.getTimeStamp() % 1_000)

        where:
        logLevel    || expectedSeverity
        Level.ALL   || 'DEBUG'
        Level.TRACE || 'DEBUG'
        Level.DEBUG || 'DEBUG'
        Level.INFO  || 'INFO'
        Level.WARN  || 'WARNING'
        Level.ERROR || 'ERROR'
    }
}
