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

import com.google.cloud.functions.BackgroundFunction
import com.google.cloud.functions.Context
import io.micronaut.gcp.function.GoogleFunctionInitializer
import jakarta.inject.Inject
import jakarta.inject.Singleton

class Example : GoogleFunctionInitializer(), // <1>
        BackgroundFunction<PubSubMessage> { // <2>
    @Inject
    lateinit var loggingService: LoggingService // <3>

    override fun accept(message: PubSubMessage, context: Context) {
        loggingService.logMessage(message)
    }
}

class PubSubMessage {
    var data: String? = null
    var attributes: Map<String, String>? = null
    var messageId: String? = null
    var publishTime: String? = null
}

@Singleton
class LoggingService {

    fun logMessage(message: PubSubMessage) {
        // log the message
    }
}