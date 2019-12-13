package kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MyKafkaTest {
    private static final Logger logger = LoggerFactory.getLogger(MyKafkaTest.class);
    private MyKafka myKafka;
    private MyCallback myCallback;

    @Before
    public void setUp() throws Exception {

        logger.info("setUp()");

        MockProducer<String, String> producer = new MockProducer();
        MockConsumer<String, String> consumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);

        this.myKafka = new MyKafka(producer, consumer);

        this.myCallback = new MyCallback();

    }

    @After
    public void tearDown() throws Exception {
        this.myKafka = null;
    }

    @Test
    public void sendIt() {

        for (int i = 1; i <= 100; i++) {
            this.myKafka.sendIt(String.format("This is test: %03d", i), myCallback);
        }

        MockProducer<String, String> mock = (MockProducer<String, String>) this.myKafka.getProducer();

        List<ProducerRecord<String, String>> history = mock.history();

        Assert.assertEquals("Size should be 100", 100, history.size());

        for (int i = 0; i < history.size(); i++) {
            String value = history.get(i).value();

            String answer = String.format("This is test: %03d", i + 1);

            Assert.assertEquals("Values don't match, but should", answer, value);

            logger.info(value);
        }
    }

    @Test
    public void receiveIt() {
        MockConsumer consumer = (MockConsumer) myKafka.getConsumer();

        consumer.assign(Arrays.asList(new TopicPartition("mock.topic", 0)));

        HashMap<TopicPartition, Long> beginningOffsets = new HashMap<>();
        beginningOffsets.put(new TopicPartition("mock.topic", 0), 0L);

        consumer.updateBeginningOffsets(beginningOffsets);

        for (int i = 0; i < 100; i++) {

            consumer.addRecord(new ConsumerRecord("mock.topic", 0, i,
                    UUID.randomUUID().toString(), String.format("Hi: %04d", i)));
        }

        ConsumerRecords<String, String> consumerRecords = consumer.poll(1000);

        logger.info(String.format("Number of records in consumer: %d", consumerRecords.count()));

        Assert.assertEquals("The count should 100", consumerRecords.count(), 100);

        if (consumerRecords.count() > 0) {
            int recCount = consumerRecords.count();

            Iterator<ConsumerRecord<String, String>> recordIterator = consumerRecords.iterator();

            int j = 0;

            while (recordIterator.hasNext()) {
                ConsumerRecord<String, String> record = recordIterator.next();

                logger.info(String.format("Topic: %s. Consumer key: %s. Consumer value: %s", record.topic(), record.key(), record.value()));

                Assert.assertEquals("Record value doesn't match", String.format("Hi: %04d", j++), record.value());
            }
        }
    }


}
