/*
 * Copyright 2017-2020 original authors
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
package example.background

import com.google.cloud.functions.*
import io.micronaut.gcp.function.GoogleFunctionInitializer

import javax.inject.*

class Example extends GoogleFunctionInitializer // <1>
        implements BackgroundFunction<PubSubMessage> { // <2>

    @Inject LoggingService loggingService // <3>

    @Override
    void accept(PubSubMessage message, Context context) {
        loggingService.logMessage(message)
    }
}

class PubSubMessage {
    String data
    Map<String, String> attributes
    String messageId
    String publishTime
}

@Singleton
class LoggingService {

    void logMessage(PubSubMessage message) {
        // log the message
    }
}
