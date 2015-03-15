import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;



public class ClientCommManager {
	
	public ServerSocket connListeningSocket;
	public ClientConnectionListener connectionListener;
	private ClientInfo myInfo;
//	private ObjectInputStream serverInStream;
//	private ObjectOutputStream serverOutStream;
	private Vector<ClientCommWorker> peers;
	
	public ClientCommManager() {
		try {
			connListeningSocket = new ServerSocket(0);
			myInfo = new ClientInfo();
			myInfo.addr = connListeningSocket.getInetAddress();
			myInfo.portNo = connListeningSocket.getLocalPort();
			connectionListener = new ClientConnectionListener(this);
			peers = new Vector<ClientCommWorker>();
			
			(new Thread(connectionListener)).start();
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
	
	public boolean initializeConnection(Vector<ClientInfo> clientList) {
		System.out.println("Sending INIT_CONN message to following clients");
		Iterator<ClientInfo> it= clientList.iterator();
		
		while (it.hasNext()) {
			ClientInfo peer = it.next();
			
			// Ignore myself in client list (this client is in the clientList)
			if (! peer.equals(myInfo)) {
				try {
					System.out.println("gg");
					Socket socket = new Socket(peer.addr, peer.portNo);
					ClientCommWorker ccm = new ClientCommWorker(socket);
					System.out.println("gg2");
					ccm.sendConnInitMsg(getMyInfo(), peer);
					
					peers.addElement(ccm);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println(peer.toString());
			}
			
		}
		
		return true;
	}
	
}
