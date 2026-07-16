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
package example.http;
//tag::imports[]

import com.google.cloud.functions.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.multipart.StreamingFileUpload;

import java.io.IOException;
import java.io.InputStream;
//end::imports[]

//tag::clazz[]
@Controller("/upload")
public class MultipartUploadController {

    @Post(value = "/completed", consumes = MediaType.MULTIPART_FORM_DATA)
    HttpResponse<String> uploadCompleted(CompletedFileUpload file) throws IOException {
        return HttpResponse.ok(file.getFilename() + ":" + file.getBytes().length);
    }

    @Post(value = "/completed-named", consumes = MediaType.MULTIPART_FORM_DATA)
    HttpResponse<String> uploadCompletedNamed(@Body("file") CompletedFileUpload upload) throws IOException {
        return HttpResponse.ok(upload.getFilename() + ":" + upload.getBytes().length);
    }

    @Post(value = "/streaming", consumes = MediaType.MULTIPART_FORM_DATA)
    HttpResponse<String> uploadStreaming(StreamingFileUpload file) throws IOException {
        try (InputStream inputStream = file.asInputStream()) {
            return HttpResponse.ok(file.getFilename() + ":" + inputStream.readAllBytes().length);
        }
    }

    @Post(value = "/raw", consumes = MediaType.MULTIPART_FORM_DATA)
    HttpResponse<String> uploadRaw(@Part("file") HttpRequest.HttpPart file) {
        return HttpResponse.ok(file.getFileName().orElse("unnamed"));
    }
}
//end::clazz[]
