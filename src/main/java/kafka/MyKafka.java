package kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MyKafka {
    private Producer<String, String> producer;
    private Consumer<String, String> consumer;
    private static Logger logger = LoggerFactory.getLogger(MyKafka.class);

    public MyKafka(Producer<String, String> producer, Consumer<String, String> consumer) {
        this.producer = producer;
        this.consumer = consumer;
    }

    public Producer<String, String>getProducer() {
        return this.producer;
    }

    public Consumer<String, String>getConsumer() {
        return this.consumer;
    }

    public void sendIt(String value, Callback callback) {
        ProducerRecord<String, String> record;

        record = new ProducerRecord<>("edwin.test.topic", UUID.randomUUID().toString(), value);

        if (callback != null) {
            producer.send(record, callback);
        } else {
            producer.send(record);
        }
    }

    public void receiveIt() {
        boolean reading = true;
        int counter = 0;

        while (reading) {
            ConsumerRecords<String, String> consumerRecords = consumer.poll(1000);

            if (consumerRecords.count() != 0) {
                consumerRecords.forEach(record -> {
                    logger.info(String.format("Topic: %s. Key: %s. Value: '%s'", record.topic(), record.key(), record.value()));
                });
            }
            else {
                if (++counter >= 3) {
                    reading = false;
                }
            }
        }


    }

    public static void main(String... args) {
        try {

            InetAddress ip = InetAddress.getByName("kafka");

            TimeUnit.SECONDS.sleep(30L);

            logger.error(String.format("The IP address for kafka is: %s", ip.getHostAddress()));

            Properties cprops = new Properties();
//            cprops.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "d-crd-esp07.dev.cyber.sas.com:9092");
            cprops.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
            cprops.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            cprops.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            cprops.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
            cprops.put(ConsumerConfig.GROUP_ID_CONFIG, "EdwinTestReader");
            cprops.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            cprops.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, "true");

            KafkaConsumer consumer = new KafkaConsumer(cprops);

            Properties props = new Properties();
//            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "d-crd-esp07.dev.cyber.sas.com:9092");
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");

            props.put(ProducerConfig.CLIENT_ID_CONFIG, "EdwinKafkaTesting");
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            KafkaProducer producer = new KafkaProducer(props);

            MyKafka myKafka = new MyKafka(producer, consumer);

            MyCallback callback = new MyCallback();

            for (int i = 0; i < 1000; i++) {

                String value = String.format("Edwin record: %04d, %s", i, new Date());

                myKafka.sendIt(value, callback);
            }

            producer.flush();
            producer.close();

            consumer.subscribe(Collections.singletonList("edwin.test.topic"));

            myKafka.receiveIt();

            consumer.close();

        } catch (Exception exp) {
            exp.printStackTrace();
            System.exit(1);
        }
    }
}
