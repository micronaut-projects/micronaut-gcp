package io.micronaut.gcp.pubsub;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.PubsubMessage;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple Producer/Consumer to mock interaction between {@link com.google.cloud.pubsub.v1.Publisher}
 * and {@link com.google.cloud.pubsub.v1.MessageReceiver}.
 * Users an internal BlockinQueue to store messages.
 */
@Singleton
public class MockPubSubEngine implements AutoCloseable{

    private final ArrayBlockingQueue<PublisherMessage> messages = new ArrayBlockingQueue<>(100);
    private final Map<String, MessageReceiver> receivers = new ConcurrentHashMap<>();
    private Worker worker = new Worker();
    private Thread workerThread;

    public MockPubSubEngine() {
        this.workerThread = new Thread(this.worker);
        this.workerThread.start();
    }

    public void publish(PubsubMessage pubsubMessage) {
        publish(pubsubMessage, "DEFAULT_TOPIC");
    }

    public void publish(PubsubMessage pubsubMessage, String topic) {
        messages.offer(new PublisherMessage(pubsubMessage, topic));
    }

    public void registerReceiver(MessageReceiver receiver){
        registerReceiver(receiver, "DEFAULT_TOPIC");
    }

    public void registerReceiver(MessageReceiver receiver, String topic){
        receivers.put(topic, receiver);
    }

    @Override
    @PreDestroy
    public void close() throws Exception {
        this.worker.running = false;
    }



    class Worker implements Runnable {

        public volatile boolean running = true;

        @Override
        public void run() {
            try {
                while (running) {
                    PublisherMessage publisherMessage = messages.take();
                    MessageReceiver receiver = receivers.get(publisherMessage.topic);
                    if(receiver != null) {
                        receiver.receiveMessage(publisherMessage.message, new AckReplyConsumer() {
                            @Override
                            public void ack() {

                            }

                            @Override
                            public void nack() {

                            }
                        });
                    }
                }
            } catch (InterruptedException ex) {

            }
        }
    }

    static class PublisherMessage {

        public final PubsubMessage message;
        public final String topic;

        PublisherMessage(PubsubMessage message, String topic) {
            this.message = message;
            this.topic = topic;
        }
    }
}
