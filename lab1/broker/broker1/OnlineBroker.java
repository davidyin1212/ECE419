import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

public class OnlineBroker {
    public static void main (String[] args) {
        int port = Integer.parseInt(args[0]);

        try {
            new OnlineBrokerThread(port).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class OnlineBrokerThread extends Thread{
        protected DatagramSocket socket = null;
        protected BufferedReader in = null;
        protected boolean moreQuotes = true;
        protected HashMap <String, Long> nasdaqTable = new HashMap<String, Long>();

        public OnlineBrokerThread(int port) throws IOException {
            socket = new DatagramSocket(port);
            in = new BufferedReader(new FileReader("nasdaq.txt"));
            String tmp;
            while ((tmp = in.readLine()) != null) {
                nasdaqTable.put(tmp.split(" ")[0], Long.parseLong(tmp.split(" ")[1]));
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    byte[] buf = new byte[256];
                    //recieve request
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);

                    ByteArrayInputStream in = new ByteArrayInputStream(buf);
                    ObjectInputStream is = new ObjectInputStream(in);
                    BrokerPacket receiveBrokerPacket = (BrokerPacket) is.readObject();
                    BrokerPacket sendBrokerPacket = new BrokerPacket();;
                    if (receiveBrokerPacket.type == BrokerPacket.BROKER_NULL) {
                        sendBrokerPacket.type = BrokerPacket.BROKER_FORWARD;
                    } else if (receiveBrokerPacket.type == BrokerPacket.BROKER_REQUEST) {
                        if (nasdaqTable.containsKey(receiveBrokerPacket.symbol)) {
                            sendBrokerPacket.type = BrokerPacket.BROKER_QUOTE;
                            sendBrokerPacket.symbol = receiveBrokerPacket.symbol;
                            sendBrokerPacket.quote = nasdaqTable.get(receiveBrokerPacket.symbol);
                        } else {
                            sendBrokerPacket.type = BrokerPacket.BROKER_ERROR;
                        }
                    }

                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    ObjectOutputStream os = new ObjectOutputStream(out);
                    os.writeObject(sendBrokerPacket);
                    buf = out.toByteArray();
                    packet = new DatagramPacket(buf, buf.length, address, port);
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}