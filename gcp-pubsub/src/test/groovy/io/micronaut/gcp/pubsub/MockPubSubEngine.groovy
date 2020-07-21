package io.micronaut.gcp.pubsub

import com.google.cloud.pubsub.v1.AckReplyConsumer
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.pubsub.v1.PubsubMessage

import javax.annotation.PreDestroy
import javax.inject.Singleton
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

@Singleton
class MockPubSubEngine implements AutoCloseable {

    private final List<PublisherMessage> messages = new ArrayList<>(100);
    private final Map<String, MessageReceiver> receivers = new ConcurrentHashMap<>();
    private Worker worker = new Worker();
    private Thread workerThread;

    MockPubSubEngine() {
        this.workerThread = new Thread(this.worker);
        this.workerThread.start();
    }

    void publish(PubsubMessage pubsubMessage) {
        publish(pubsubMessage, "DEFAULT_TOPIC");
    }

    void publish(PubsubMessage pubsubMessage, String topic) {
        messages.add(new PublisherMessage(pubsubMessage, topic));
    }

    void registerReceiver(MessageReceiver receiver){
        registerReceiver(receiver, "DEFAULT_TOPIC");
    }

    void registerReceiver(MessageReceiver receiver, String topic){
        receivers.put(topic, receiver);
    }

    @Override
    @PreDestroy
    void close() throws Exception {
        this.worker.running = false;
    }


    class Worker implements Runnable {

        public volatile boolean running = true;
        //instead of using a BlockingQueue and run into issues of ordering of producer/consumer just use a plain list
        //and rely on polling every 200ms for available messages if a receiver is registered
        @Override
        void run() {
            try {
                while (running) {
                    for(Map.Entry<String, MessageReceiver> entry : receivers.entrySet()) {
                        List<PublisherMessage> availableMessages = messages.stream().filter({ publisherMessage -> publisherMessage.topic == entry.getKey() && !publisherMessage.published }).collect(Collectors.toList())
                        for (PublisherMessage availableMessage : availableMessages) {
                            entry.getValue().receiveMessage(availableMessage.message, new AckReplyConsumer() {
                                @Override
                                public void ack() {

                                }

                                @Override
                                public void nack() {

                                }
                            });
                            availableMessage.published = true;
                        }
                    }
                    Thread.sleep(250);
                }
            } catch (InterruptedException ex) {

            }
        }
    }

    static class PublisherMessage {

        public final PubsubMessage message;
        public final String topic;
        public boolean published = false;

        PublisherMessage(PubsubMessage message, String topic) {
            this.message = message;
            this.topic = topic;
        }
    }
}
