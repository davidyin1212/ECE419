import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;



public class ClientCommManager {
	
	private ServerSocket serverSocket;
	private ClientInfo myInfo;
	private ObjectInputStream serverInStream;
	private ObjectOutputStream serverOutStream;
	
	public ClientCommManager() {
		try {
			serverSocket = new ServerSocket(0);
			myInfo = new ClientInfo();
			myInfo.addr = serverSocket.getInetAddress();
			myInfo.portNo = serverSocket.getLocalPort();
			
			
		} catch (IOException e) {
			System.err.println("ClinetCommManager - error while initializing socket");
			System.exit(1);;
		}
	}
	
	
	public ClientInfo getMyInfo() {
		return myInfo;
	}
	
	public void setClientUsername(String name) {
		myInfo.username = name;
	}
	
}
