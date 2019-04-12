package hello.world;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class Greeting {
    private String message;

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

}