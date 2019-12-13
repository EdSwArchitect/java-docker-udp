package kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class UdpReader implements Runnable {
    private static Logger log = LoggerFactory.getLogger(UdpReader.class);
    private static AtomicBoolean running = new AtomicBoolean(true);

    private DatagramSocket serverSocket = null;
    private DatagramChannel serverChannel = null;
    private InetSocketAddress address = null;
    private ByteBuffer buffer = ByteBuffer.allocate(2056);
    private Selector selector;
    private SelectionKey selectorKey;
    private MyProducer myProducer;
    private KafkaProducer<String, String>producer;

    /**
     *
     * @param port
     * @param kafkaServers
     * @param topic
     * @throws IOException
     */
    public UdpReader(int port, String kafkaServers, String topic) throws IOException {

        try {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);

            props.put(ProducerConfig.CLIENT_ID_CONFIG, "EdwinKafkaTestingProducer");
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

            log.info(String.format("Connecting to Kafka broker: '%s'", kafkaServers));

            producer = new KafkaProducer(props);

            myProducer = new MyProducer(topic, producer);

            serverChannel = DatagramChannel.open();
            serverSocket = serverChannel.socket();
            address = new InetSocketAddress(port);

            log.info("Binding UDP to the below");
            log.info(String.format("HostName: '%s'. HostAddress: '%s'", address.getHostName(), address.getAddress().getHostAddress()));

            serverSocket.bind(address);

            serverChannel.configureBlocking(false);
            serverSocket.setReuseAddress(true);

            selector = Selector.open();
            selectorKey = serverChannel.register(selector, SelectionKey.OP_READ);
        }
        catch(IOException ioe) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ioe.printStackTrace(pw);

            log.error(sw.toString());
        }
    }

    /**
     * Thread method
     */
    @Override
    public void run() {
        int pos;

        log.info("Starting the thread.");
        MyCallback callback = new MyCallback();

        while (running.get()) {
            try {
                selector.select();

                if (!selectorKey.isReadable()) {
                    log.info("not readable");
                    continue;
                }

                SocketAddress from = serverChannel.receive(buffer);

                if (from != null) {

                    pos = buffer.position();

                    String s = new String(buffer.array(), 0, pos, "UTF-8");

                    log.info(s);

                    buffer.position(0);


                    myProducer.sendIt(s, callback);
                }
            } catch (IOException ioe) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);

                log.error(sw.toString());
                log.error("Terminating UDP reading.");
                running.set(false);
            }

        } // while (running.get()) {

        try {
            if (selectorKey != null) {
                selectorKey.cancel();
            }

            if (selector != null) {
                selector.close();
            }

            if (serverChannel != null) {
                serverChannel.close();

            }
        } catch (IOException e) {

        }

    }

    public void stop() {
        running.set(false);
    }

    public static void main(String... args) {
        try {

            // port, Kafka server, topic
            UdpReader reader = new UdpReader(5555, args[0], args[1]);

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
            System.exit(1);
        }
    }
}
