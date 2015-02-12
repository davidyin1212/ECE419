import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.PriorityQueue;


public class ClientCommunicator implements Runnable{
	
	Mazewar game;
	
	private final int CONNECT_FAILURE_SLEEP_INTERVAL = 3000;
	
	private int portNumber;
	private String hostname; 
	private boolean isConnected;
	private Socket clientSocket;
	
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	private PriorityQueue<MessagePacket> orderedMessageQueue;
	
	public ClientCommunicator(String hostname, int portNumber, Mazewar game) {
		this.game = game;
		this.hostname = hostname;
		this.portNumber = portNumber;
		this.isConnected = false;
		orderedMessageQueue = new PriorityQueue<MessagePacket>();
	}


	@Override
	public void run() {
		while (! Thread.currentThread().isInterrupted()) {
			MessagePacket msg = null;
			
				try {
					msg = (MessagePacket) inputStream.readObject();
					assert(msg != null);
					ClientEventDispatcher.addMessage(msg);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	
	public void connectToServer() {
		System.out.println("Trying to connect to server...");
		/* Try until successfully opens socket */
		while (! isConnected) {

			try {
				this.clientSocket = new Socket(hostname, portNumber);
				isConnected = true;
			}
			/* Upon failure, sleep thread for  3 secs */
			catch (Exception e) {
				System.err.println ("Could not connect to server.. will try again in 3 seconds");
				e.printStackTrace();

				try {
					Thread.sleep(CONNECT_FAILURE_SLEEP_INTERVAL);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		
		/* Open IO Stream */
		try {
			this.outputStream = new ObjectOutputStream(this.clientSocket.getOutputStream());
			this.inputStream = new ObjectInputStream(this.clientSocket.getInputStream());
			
			System.out.println("opened io stream");
		} catch (IOException e) {
			System.err.println("Error while opening I/O Stream");
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
		
		System.out.println("Initialized Socket and I/O Stream");
		
	}
	 
	/*
	 * Returns true upon success, false on failure
	 * */
	public MessagePacket sendJoinMessage(String playerName) throws IOException {

		MessagePacket response; 
		boolean isSetupMessage = false;
		/* Send Join Request */
		MessagePacket gmp = new MessagePacket();
		gmp.messageType = MessagePacket.ADMIN_MESSAGE_TYPE_JOIN_GAME_REQUEST;
		gmp.playerName = playerName;
		
		this.outputStream.writeObject(gmp);
		this.outputStream.flush();
		
		/* Upon Success, we may start game */
		try {
			
			while (! isSetupMessage) {
				response = (MessagePacket) this.inputStream.readObject();
				isSetupMessage = 
						(response.messageType == MessagePacket.ADMIN_MESSAGE_TYPE_JOIN_GAME_SUCCESS
						|| response.messageType == MessagePacket.ADMIN_MESSAGE_TYPE_JOIN_GAME_FAILURE);
				
				if (isSetupMessage)
					return response;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Exception while receiving Join Response");
			e.printStackTrace();
			System.exit(1);
		}
		
		return null;
	}
	
	public void sendGameMessage(MessagePacket msg) {
		try {
			this.outputStream.writeObject(msg);
			this.outputStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
}
