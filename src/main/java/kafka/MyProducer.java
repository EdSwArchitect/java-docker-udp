package kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.UUID;

public class MyProducer {
    private Producer<String, String> producer;
    private String topic;

    public MyProducer(String topic, Producer<String, String> producer) {
        this.producer = producer;
        this.topic = topic;
    }

    public Producer<String, String>getProducer() {
        return this.producer;
    }

    public void sendIt(String value, Callback callback) {
        ProducerRecord<String, String> record;

        record = new ProducerRecord<>(topic, UUID.randomUUID().toString(), value);

        if (callback != null) {
            producer.send(record, callback);
        } else {
            producer.send(record);
        }
    }

}
