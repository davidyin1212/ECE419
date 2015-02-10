import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class MazewarServerWorker implements Runnable {

	public Socket socket;
	public ObjectInputStream inputStream;
	public ObjectOutputStream outputStream;
	private boolean acceptedClient = false;
	private String name;
	
	public MazewarServerWorker(Socket socket) throws IOException {
		this.socket = socket;
		this.outputStream = new ObjectOutputStream(socket.getOutputStream());
		this.inputStream = new ObjectInputStream(socket.getInputStream());
		this.name = "";
	}
	
	public void run() {		
		
		System.out.println("Thread Started");

		waitAndProcessJoinMessage();
		
		if (acceptedClient) {
			while (! Thread.currentThread().isInterrupted()) {
				
			}
		}
		System.out.println("Disposing Thread");
				
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
						
						System.err.println("Received GAME JOIN MESSAGE FROM CLIENT : " + username);
						MessagePacket result = MazewarServer.testAndAcceptClient(username ,this);
						
						outputStream.writeObject(result);
						outputStream.flush();
						
						if (result.messageType == MessagePacket.ADMIN_MESSAGE_TYPE_JOIN_GAME_SUCCESS) {
							this.name = username;
							this.acceptedClient = true;
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
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public String getName() {
		return name;
	}
}
