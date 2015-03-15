import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ClientConnectionListener implements Runnable {

	ClientCommManager cm;
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
				ClientCommWorker worker = new ClientCommWorker(socket);
				worker.receiveConnInitMsg();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}
