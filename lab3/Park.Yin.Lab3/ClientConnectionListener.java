import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 
 * ConnectionListener which is responsible for new incoming connection from peer
 * @author denny
 *
 */
public class ClientConnectionListener implements Runnable {

	/* Reference to communication manager */
	ClientCommManager cm;
	
	/* Socket for listening incoming connection request */
	private ServerSocket listeningSocket;
	
	public ClientConnectionListener(ClientCommManager cm) {
		this.cm = cm;
		listeningSocket = cm.connListeningSocket;
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				Socket socket = listeningSocket.accept();
				ClientCommWorker worker = new ClientCommWorker(socket, cm);
				cm.addPeer(worker);
				worker.receiveConnInitMsg();
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}
