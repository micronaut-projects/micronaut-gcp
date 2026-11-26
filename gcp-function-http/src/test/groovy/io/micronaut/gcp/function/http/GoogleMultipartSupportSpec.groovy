package io.micronaut.gcp.function.http

import io.micronaut.core.type.Argument
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.http.multipart.StreamingFileUpload
import spock.lang.Specification

import java.util.concurrent.Executor

class GoogleMultipartSupportSpec extends Specification {

    private static final Executor DIRECT = { Runnable r -> r.run() } as Executor

    void "a part does not bind to an argument that is not a file upload"() {
        expect:
        !GoogleMultipartSupport.bindFileUpload(Argument.of(String), "file", new MockGoogleHttpPart("file.txt", "Some text", "text/plain"), DIRECT).present
    }

    void "an unreadable part is rejected as a bad request for #type.simpleName"() {
        when:
        GoogleMultipartSupport.bindFileUpload(Argument.of(type), "file", new UnreadablePart(), DIRECT)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.BAD_REQUEST
        e.message == "Unable to read part [file]: boom"

        where:
        type << [CompletedFileUpload, StreamingFileUpload]
    }

    void "a streaming upload can be read when the part length is unknown"() {
        when:
        StreamingFileUpload upload = GoogleMultipartSupport.streamingFileUpload("file", new UnknownLengthPart(), DIRECT)

        then:
        upload.filename == "file.txt"
        upload.asInputStream().text == "Some text"
    }

    static class UnreadablePart extends MockGoogleHttpPart {
        UnreadablePart() {
            super("file.txt", "", "text/plain")
        }

        @Override
        InputStream getInputStream() throws IOException {
            throw new IOException("boom")
        }
    }

    static class UnknownLengthPart extends MockGoogleHttpPart {
        UnknownLengthPart() {
            super("file.txt", "Some text", "text/plain")
        }

        @Override
        long getContentLength() {
            return -1
        }
    }
}
