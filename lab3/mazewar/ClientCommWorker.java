import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.PriorityQueue;


public class ClientCommWorker implements Runnable {

	Socket socket;
	ObjectInputStream inputStream;
	ObjectOutputStream outputStream;
	String username; /* Target client's username */
	ClientCommManager commManager;
	private PriorityQueue<GameMessage> receivedMessageQueue;

	
	public ClientCommWorker(Socket socket, ClientCommManager commManager) {

		this.socket = socket;
		this.commManager = commManager;
		this.receivedMessageQueue = new PriorityQueue<GameMessage>();
		
		try {

			this.outputStream = new ObjectOutputStream(this.socket.getOutputStream());

			this.inputStream = new ObjectInputStream(this.socket.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendConnInitMsg(ClientInfo myInfo, ClientInfo target) {
		
		this.username = target.username;
		
		ControlMessage cm = new ControlMessage();
		cm.messageType = ControlMessage.CONN_INIT_REQUEST;
		cm.username = myInfo.username;
		
		
		
		try {
			outputStream.writeObject(cm);		
			System.out.println("Connection established with user: " + target.username);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Error in ClientCommWorker - sendConnInitMsg()");
		}

		commManager.startGameIfReady();

		
		
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
		
		while (! Thread.currentThread().isInterrupted()) {

			try {
				GameMessage m = (GameMessage) inputStream.readObject();
		
				addReceivedMessage(m);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public synchronized void addReceivedMessage(GameMessage msg) {

		receivedMessageQueue.add(msg);
	}
	
	public synchronized boolean hasNextMessage(int clock) {
		if (receivedMessageQueue.peek() == null) return false;

		return receivedMessageQueue.peek().clock == clock;
	}
	
	public synchronized GameMessage getNextMessage() {
		if (receivedMessageQueue.peek() == null) {
			System.out.println("you should never fucking call this idiot");
		}
		return receivedMessageQueue.poll();
	}
	


	public void sendMessage(GameMessage message) {
		try {
			outputStream.writeObject(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
