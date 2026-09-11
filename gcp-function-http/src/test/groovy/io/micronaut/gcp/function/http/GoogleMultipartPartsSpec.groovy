package io.micronaut.gcp.function.http

import com.google.cloud.functions.HttpRequest
import io.micronaut.http.HttpHeaders
import io.micronaut.http.MediaType
import io.micronaut.http.client.multipart.MultipartBody
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.nio.file.Files

class GoogleMultipartPartsSpec extends Specification {

    void "parsing requires a multipart/form-data content type: #contentType"() {
        when:
        GoogleMultipartParts.parse(contentType, 'name=Fred'.bytes)

        then:
        IllegalStateException e = thrown()
        e.message.contains("multipart/form-data")

        where:
        contentType << [null, MediaType.TEXT_PLAIN, MediaType.APPLICATION_FORM_URLENCODED]
    }

    void "parses the fields and files of a multipart body"() {
        given:
        String body = "--XyZ\r\n" +
                "Content-Disposition: form-data; name=\"name\"\r\n" +
                "\r\n" +
                "Fred\r\n" +
                "--XyZ\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"file.txt\"\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "Some text\r\n" +
                "--XyZ--\r\n"

        when:
        Map<String, HttpRequest.HttpPart> parts = GoogleMultipartParts.parse(MediaType.MULTIPART_FORM_DATA + "; boundary=XyZ", body.getBytes(StandardCharsets.UTF_8))

        then:
        parts.keySet() as List == ['name', 'file']

        and:
        HttpRequest.HttpPart field = parts.name
        !field.fileName.present
        !field.contentType.present
        field.headers.isEmpty()
        field.characterEncoding.get() == StandardCharsets.UTF_8.name()
        field.contentLength == 4
        field.reader.text == 'Fred'

        and:
        HttpRequest.HttpPart file = parts.file
        file.fileName.get() == 'file.txt'
        file.contentType.get() == MediaType.TEXT_PLAIN
        file.headers == [(HttpHeaders.CONTENT_TYPE): [MediaType.TEXT_PLAIN]]
        file.contentLength == 9
        file.inputStream.text == 'Some text'
    }

    void "a body that is not a multipart body has no parts: #body"() {
        expect:
        !GoogleMultipartParts.fromBody(body).present

        where:
        body << [null, 'text', [name: 'Fred']]
    }

    void "resolves the parts of a MultipartBody"() {
        given:
        File file = Files.createTempFile("multipart", ".txt").toFile()
        file.deleteOnExit()
        file.text = 'From a file'
        MultipartBody body = MultipartBody.builder()
                .addPart("name", "Fred")
                .addPart("bytes", "bytes.bin", MediaType.APPLICATION_OCTET_STREAM_TYPE, 'Some bytes'.bytes)
                .addPart("file", "file.txt", MediaType.TEXT_PLAIN_TYPE, file)
                .addPart("stream", "stream.txt", MediaType.TEXT_PLAIN_TYPE, new ByteArrayInputStream('From a stream'.bytes), 13)
                .build()

        when:
        Map<String, HttpRequest.HttpPart> parts = GoogleMultipartParts.fromBody(body).get()

        then:
        parts.keySet() as List == ['name', 'bytes', 'file', 'stream']
        !parts.name.fileName.present
        parts.name.reader.text == 'Fred'
        parts.bytes.fileName.get() == 'bytes.bin'
        parts.bytes.contentType.get() == MediaType.APPLICATION_OCTET_STREAM
        parts.bytes.inputStream.bytes == 'Some bytes'.bytes
        parts.file.reader.text == 'From a file'
        parts.file.contentLength == 11
        parts.stream.reader.text == 'From a stream'
    }
}
