import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class ClientCommWorker implements Runnable {

	Socket socket;
	ObjectInputStream inputStream;
	ObjectOutputStream outputStream;
	String username; /* Target client's username */
	
	public ClientCommWorker(Socket socket) {
		this.socket = socket;
		
		try {
			System.out.println("done1");
			this.outputStream = new ObjectOutputStream(this.socket.getOutputStream());
			System.out.println("done2");
			this.inputStream = new ObjectInputStream(this.socket.getInputStream());
			
			
			System.out.println("done3");
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
		// TODO Auto-generated method stub
		// receive message
	}
	
	public void sendMessage() {
		//send message
	}

}
