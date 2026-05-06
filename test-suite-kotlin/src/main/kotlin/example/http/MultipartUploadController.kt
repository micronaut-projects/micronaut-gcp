/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example.http
//tag::imports[]

import com.google.cloud.functions.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Part
import io.micronaut.http.annotation.Post
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.http.multipart.StreamingFileUpload
//end::imports[]

//tag::clazz[]
@Controller("/upload")
class MultipartUploadController {

    @Post(value = "/completed", consumes = [MediaType.MULTIPART_FORM_DATA])
    fun uploadCompleted(file: CompletedFileUpload): HttpResponse<String> =
        HttpResponse.ok("${file.filename}:${file.bytes.size}")

    @Post(value = "/completed-named", consumes = [MediaType.MULTIPART_FORM_DATA])
    fun uploadCompletedNamed(@Body("file") upload: CompletedFileUpload): HttpResponse<String> =
        HttpResponse.ok("${upload.filename}:${upload.bytes.size}")

    @Post(value = "/streaming", consumes = [MediaType.MULTIPART_FORM_DATA])
    fun uploadStreaming(file: StreamingFileUpload): HttpResponse<String> =
        file.asInputStream().use { inputStream ->
            HttpResponse.ok("${file.filename}:${inputStream.readBytes().size}")
        }

    @Post(value = "/raw", consumes = [MediaType.MULTIPART_FORM_DATA])
    fun uploadRaw(@Part("file") file: HttpRequest.HttpPart): HttpResponse<String> =
        HttpResponse.ok(file.fileName.orElse("unnamed"))
}
//end::clazz[]
