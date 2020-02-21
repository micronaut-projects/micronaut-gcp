package example.background

import com.google.cloud.functions.*
import io.micronaut.function.executor.FunctionInitializer
import javax.inject.*

class Example extends FunctionInitializer // <1>
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
