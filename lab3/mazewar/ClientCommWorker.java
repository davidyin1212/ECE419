import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class ClientCommWorker implements Runnable {

	Socket socket;
	ObjectInputStream inputStream;
	ObjectOutputStream outputStream;
	String username; /* Target client's username */
	ClientCommManager commManager;

	
	public ClientCommWorker(Socket socket, ClientCommManager commManager) {

		this.socket = socket;
		this.commManager = commManager;
		
		try {

			this.outputStream = new ObjectOutputStream(this.socket.getOutputStream());

			this.inputStream = new ObjectInputStream(this.socket.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendConnInitMsg(ClientInfo myInfo, ClientInfo target) {
		ControlMessage cm = new ControlMessage();
		cm.messageType = ControlMessage.CONN_INIT_REQUEST;
		cm.username = myInfo.username;
		
		try {
			outputStream.writeObject(cm);
			System.out.println("Connection established with user: " + target.username);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Error in ClientCommWorker - sendConnInitMsg()");
		}

		commManager.startGameIfReady();
		(new Thread(this)).start();
		
		
	}
	
	public void receiveConnInitMsg() {
		try {

			
			Object o = this.inputStream.readObject();
			
			if (o instanceof ControlMessage) {
				ControlMessage cm = (ControlMessage) o;
				
				if (cm.messageType == ControlMessage.CONN_INIT_REQUEST) {
					this.username = cm.username;
					System.out.println("Connection established with user: "+  this.username);
					commManager.startGameIfReady();
					(new Thread(this)).start();
					
				}
			}
			else {
				System.err.println("ClientCommWorker - receiveConnInitMsg(): Message given is not control message");
			}

			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		
	}
	

	public void sendMessage() {
		//send message
	}

}
