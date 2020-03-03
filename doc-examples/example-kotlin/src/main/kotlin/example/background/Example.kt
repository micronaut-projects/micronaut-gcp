package example.background

import com.google.cloud.functions.BackgroundFunction
import com.google.cloud.functions.Context
import io.micronaut.function.executor.FunctionInitializer
import javax.inject.*

class Example : FunctionInitializer(), // <1>
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