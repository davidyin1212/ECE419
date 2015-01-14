import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class BrokerClient {
	public static void main (String[] args) {
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        System.out.println("Enter queries or x for exit: ");
        while (true) {
            try {
                DatagramSocket socket = new DatagramSocket();
                byte[] buf = new byte[256];
                BrokerPacket statusPacket = new BrokerPacket();

                //check status
                statusPacket.type = BrokerPacket.BROKER_NULL;
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream os = new ObjectOutputStream(out);
                os.writeObject(statusPacket);
                buf = out.toByteArray();
                InetAddress address = InetAddress.getByName(args[0]);
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
                socket.send(packet);

                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                ByteArrayInputStream in = new ByteArrayInputStream(buf);
                ObjectInputStream is = new ObjectInputStream(in);
                BrokerPacket recievedPacket = (BrokerPacket) is.readObject();

                if (recievedPacket.type == BrokerPacket.BROKER_FORWARD) {
                    System.out.print("> ");
                    Scanner sc = new Scanner(System.in);
                    String input = sc.next();
                    if (!input.equals("x")) {
                        //send request
                        BrokerPacket requestPacket = new BrokerPacket();
                        requestPacket.type = BrokerPacket.BROKER_REQUEST;
                        out = new ByteArrayOutputStream();
                        os = new ObjectOutputStream(out);
                        os.writeObject(statusPacket);
                        buf = out.toByteArray();
                        address = InetAddress.getByName(args[0]);
                        packet = new DatagramPacket(buf, buf.length, address, 4445);
                        socket.send(packet);
                    } else {
                        break;
                    }
                } else if (recievedPacket.type == BrokerPacket.BROKER_QUOTE) {
                    System.out.println("Quote from broker: " + recievedPacket.quote);
                } else {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}