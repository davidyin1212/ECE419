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

		waitAndProcessForJoinMessage();
				
		while (true) {
			
		}
	}
	
	public void waitAndProcessForJoinMessage() {
		while (! this.acceptedClient) {
			Object o;
			try {
				o = inputStream.readObject();
				if (o instanceof MessagePacket) {
					MessagePacket joinMessage = (MessagePacket) o;
					/* New client initiated JOIN */
					if (joinMessage.messageType == MessagePacket.ADMIN_MESSAGE_TYPE_JOIN_GAME_REQUEST) {
						
						System.err.println("Received GAME JOIN MESSAGE FROM CLIENT : " + joinMessage.playerName);
						if (MazewarServer.hasPlayer(joinMessage.playerName)) {
							System.err.println("Username already exists - Rejecting");
						}
						
						acceptedClient = true;
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
