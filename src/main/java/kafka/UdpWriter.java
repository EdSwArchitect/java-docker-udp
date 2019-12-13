package kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UdpWriter {
    private static Logger logger = LoggerFactory.getLogger(UdpWriter.class);

    public static void main(String... args) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress ip = InetAddress.getByName(args[0]);
            Scanner scanner = new Scanner(System.in);
            int port = Integer.parseInt(args[1]);
            byte[] buffer;
            String line;
            DatagramPacket packet;

            logger.info(String.format("host name %s. Canonical host name: %s. Address: %s", ip.getHostName(), ip.getCanonicalHostName(),
                    ip.getHostAddress()));

            while (true) {
                System.out.print("Data> ");
                System.out.flush();

                line = scanner.nextLine();

                buffer = line.getBytes("UTF-8");

                packet = new DatagramPacket(buffer, buffer.length, ip, port);

                socket.send(packet);

                if (line.equals("exit")) {
                    break;
                }
            } // while (true) {
        }
        catch(Exception exp) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            exp.printStackTrace(pw);

            logger.error(sw.toString());
        }
    }
}
