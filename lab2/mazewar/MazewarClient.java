import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;


/*
 * Mazewar client from server's point of view.
 * Each MazewarClient object represents client connected to server
 * 
 * Responsible for reading game messages from client socket and delegating 
 * message to MazewarServer
 */
public class MazewarClient implements Runnable {

	public Socket socket;
	public ObjectInputStream inputStream;
	public ObjectOutputStream outputStream;
	private boolean acceptedClient = false;
	private String name;
	private static Random randomGen = new Random(System.currentTimeMillis());
	
	public MazewarClient(Socket socket) throws IOException {
		this.socket = socket;
		this.outputStream = new ObjectOutputStream(socket.getOutputStream());
		this.inputStream = new ObjectInputStream(socket.getInputStream());
		this.name = "";
	}
	
	public void run() {		
		waitAndProcessJoinMessage();
		
		if (acceptedClient) {
			while (! Thread.currentThread().isInterrupted()) {
		
					MessagePacket incomingMsg;
					try {
						incomingMsg = (MessagePacket) inputStream.readObject();
						
						if (incomingMsg.messageType == MessagePacket.GAME_MESSAGE_TYPE_SPAWN_PLAYER) {
							
							Point newLoc = MazeImpl.getRandomPoint(randomGen);
							incomingMsg.playerLocations = new HashMap<String, Point>();
							incomingMsg.playerDirections = new HashMap<String, Direction>();
							incomingMsg.playerLocations.put(name, newLoc);
							incomingMsg.playerDirections.put(name, Direction.random());
						}
						
						
						MazewarServer.enqueueMessage(incomingMsg);

					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					} catch (IOException e) {
						System.err.println("Client closed connection");
						break;
					}
					
	
				
			}
		}
				
	}
	
	public void waitAndProcessJoinMessage() {
		while (! this.acceptedClient) {

			Object o;
			try {
				o = inputStream.readObject();
				if (o instanceof MessagePacket) {
					MessagePacket joinMessage = (MessagePacket) o;
					
					/* New client initiated join */
					if (joinMessage.messageType == MessagePacket.ADMIN_MESSAGE_TYPE_JOIN_GAME_REQUEST) {
						String username = joinMessage.playerName;
						MessagePacket result = MazewarServer.testAndAcceptClient(username ,this);
						
						outputStream.writeObject(result);
						outputStream.flush();
						
						if (result.messageType == MessagePacket.ADMIN_MESSAGE_TYPE_JOIN_GAME_SUCCESS) {
							this.name = username;
							this.acceptedClient = true; 
							System.out.println(username + " successfully joined the game");
							/* Is server have enough players? If so, start Game*/
							if (MazewarServer.isFullServer()) {
								MazewarServer.startGame();
							}
						}
						
						else {
							if (result.reason == MessagePacket.ERROR_REASON_PLAYERNAME_EXISTS) {
								System.err.println("waitAndProcessJoinMessage Rejecting user- Playername Exists:" + username);
								
							}
							else if (result.reason == MessagePacket.ERROR_REASON_SERVER_FULL) {
								System.err.println("waitAndProcessJoinMessage  Rejecting user - Server Full");
								inputStream.close();
								outputStream.close();
								socket.close();
								return;
							}
							else {
								System.err.println("waitAndProcessJoinMessage : Unhandled Reason");
							}
						}
					}
				
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			} catch (IOException e) {
				System.err.println("Client closed connection");
				return;
			}
			
		}
	}
	
	public String getName() {
		return name;
	}
}
