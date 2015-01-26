import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class MazewarClient implements Runnable{
	private final int CONNECT_FAILURE_SLEEP_INTERVAL = 3000;
	
	private int portNumber;
	private String hostname; 
	private boolean isConnected;
	private Socket clientSocket;
	
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	
	public MazewarClient(String hostname, int portNumber) {
		this.hostname = hostname;
		this.portNumber = portNumber;
		this.isConnected = false;
	}


	@Override
	public void run() {

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
		
		
		System.out.println("Successfully connected to server");
		
	}
	 
	/*
	 * Returns true upon success, false on failure
	 * */
	public boolean sendJoinMessage(String playerName) throws IOException {
		
		GameMessagePacket response; 
		boolean isSetupMessageForMe = false;
		/* Send Join Request */
		GameMessagePacket gmp = new GameMessagePacket();
		gmp.messageType = GameMessagePacket.JOIN_GAME_REQUEST;
		gmp.playerName = playerName;
		
		this.outputStream.writeObject(gmp);
		this.outputStream.flush();
		
		/* Upon Success, we may start game */
		try {
			
			while (! isSetupMessageForMe) {
				response = (GameMessagePacket) this.inputStream.readObject();
				isSetupMessageForMe = 
						(response.messageType == GameMessagePacket.JOIN_GAME_SUCCESS
						|| response.messageType == GameMessagePacket.JOIN_GAME_FAILURE)
						&& response.messageTarget == playerName;
				
				if (isSetupMessageForMe) {
					if (response.messageType == GameMessagePacket.JOIN_GAME_SUCCESS){
						/* Now ready to start game. Setup by processing setupMessage */
						SetupMessagePacket smp = response.setupMessagePacket;
						// Do whatever I want to setup new game.
						return true;
					}
			
				/* Failure likely due to duplicate name */
				return false;
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Exception while receiving Join Response");
			e.printStackTrace();
			System.exit(1);
		}
		return false;
		
	}
	
	
}
