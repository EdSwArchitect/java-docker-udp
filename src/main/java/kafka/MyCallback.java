package kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyCallback implements Callback {
    private static final Logger logger = LoggerFactory.getLogger(MyCallback.class);

    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        if (exception == null) {

            logger.info(String.format("Topic: %s. Offset: %d. Partition: %d. Key size: %d",
                    metadata.topic(), metadata.offset(), metadata.partition(), metadata.serializedValueSize()));

        }
        else {
            logger.error(String.format("Exception: %s\n", exception.getMessage()));
        }
    }
}
