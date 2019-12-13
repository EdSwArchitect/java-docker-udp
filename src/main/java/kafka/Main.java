package kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Main {
    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) {
        if (args.length == 0) {
            log.error("Not enough application parameters");
            System.exit(1);
        }

        String command = args[0].toLowerCase();

        if (command.equals("consumer")) {

            if (args.length != 3) {
                log.error("Not enough parameters: consumer <kafka brokers> <topic>");
                System.exit(1);
            }

            log.info(String.format("Consumer: Kafka servers: '%s'. Topic: '%s'", args[1], args[2]));

            Properties cprops = new Properties();
            cprops.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, args[1]);
            cprops.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            cprops.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            cprops.put(ConsumerConfig.GROUP_ID_CONFIG, "EdwinTestConsumerReader");
            cprops.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            cprops.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, "true");

            KafkaConsumer consumer = new KafkaConsumer(cprops);

            MyConsumer myConsumer = new MyConsumer(consumer, args[2]);

            Thread consumerThread = new Thread(myConsumer);
            consumerThread.start();

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    log.info("Interrupt caught. Exit");
                    myConsumer.stop();
                }
            });

        }
        else if (command.equals("udp")) {
            if (args.length != 4) {
                log.error("Not enough parameters: udp <port> <kafka brokers> <topic>");
                System.exit(1);
            }

            log.info("Sleeping 45 seconds before connecting");

            try {
                TimeUnit.SECONDS.sleep(45L);
            } catch (InterruptedException e) {

            }

            try {
                // port, kafkaServer, topic
                log.info(String.format("UDP port: %s. Kafka servers: '%s'. Topic: '%s'", args[1], args[2], args[3]));

                UdpReader reader = new UdpReader(Integer.parseInt(args[1]), args[2], args[3]);

                Thread thread = new Thread(reader);

                thread.start();

                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        log.info("Interrupt caught. Exit");
                        reader.stop();

                    }
                });

            } catch (IOException e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);

                e.printStackTrace(pw);

                log.error(sw.toString());
            }

        }
        else {
            log.error("Your parameters suck");
            System.exit(1);
        }
    }
}
