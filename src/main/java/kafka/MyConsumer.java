package kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public class MyConsumer implements Runnable {
    private Consumer<String, String> consumer;
    private String topic;
    private AtomicBoolean reading = new AtomicBoolean(true);
    private static Logger logger = LoggerFactory.getLogger(MyConsumer.class);

    public MyConsumer(Consumer<String, String> consumer, String topic) {
        this.consumer = consumer;
        this.topic = topic;
    }

    public Consumer<String, String> getConsumer() {
        return this.consumer;
    }

    @Override
    public void run() {

        consumer.subscribe(Collections.singletonList(topic));

        while (reading.get()) {
            receiveIt();
        }

    }

    public void stop() {
        reading.set(false);
    }

    public void receiveIt() {
        int counter = 0;

        ConsumerRecords<String, String> consumerRecords = consumer.poll(1000);

        if (consumerRecords.count() != 0) {
            consumerRecords.forEach(record -> {
                logger.info(String.format("Topic: %s. Key: %s. Value: '%s'", record.topic(), record.key(), record.value()));
            });
        }


    }

}
