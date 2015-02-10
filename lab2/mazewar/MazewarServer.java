import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Vector;


public class MazewarServer {
	private static int portNumber;
	private static Vector<MazewarServerWorker> serverWorkers; /* Vector containing worker per client*/
	private static ServerSocket serverSocket;
	private static PrintWriter out;
	private static BufferedReader in;
	private static BufferedReader stdIn;
	private static int nextSeqNum;
	private static int maxClients;
	private static boolean gameStarted;
	
	private static void serverInit() {
		serverWorkers = new Vector<MazewarServerWorker>();
		nextSeqNum = 0;
		maxClients = 4;
		gameStarted = false;
	}
	
	
	private static void startServer() {
		
		serverInit();
		
		/* Create server socket for incoming connection */
		try {
			serverSocket = new ServerSocket(portNumber);
		}
		catch (IOException e) {
			System.out.println("runServer() - Could not listen on port " + portNumber);
			System.exit(1);
		}
		
		/* Accept Clients, spawn thread as delegate */
		while (true) {
			try {
				MazewarServerWorker worker;
				/* Accept Incoming connection */
				System.err.println("Waiting for clients' connection");
				worker = new MazewarServerWorker(serverSocket.accept());
				System.err.println("Accepted Client");
				
				/* Register designated client socket into our vector*/
	
				
				/* Start worker thread */
				(new Thread(worker)).start(); 
			}
			catch(IOException e) {
				System.out.println("runServer() - IOException from accept()");
				System.exit(1);
			}
		}
		
//		/* We reached max clients - Send GameStart Message */
//		 
//		/* Further requests will be rejected */
//		while (true) {
//			try {
//				ObjectOutputStream outputStream;
//				
//				Socket s = serverSocket.accept();
//				
//				/* Create Reject Message Packet */
//				MessagePacket rejectPacket = new MessagePacket();
//				rejectPacket.messageType = MessagePacket.ADMIN_MESSAGE_TYPE_JOIN_GAME_FAILURE;
//				rejectPacket.reason = MessagePacket.ERROR_REASON_SERVER_FULL;
//				
//				outputStream = new ObjectOutputStream(s.getOutputStream());
//				outputStream.writeObject(rejectPacket);
//				outputStream.close();
//				s.close();
//				
//				System.err.println("Rejected Client - Reason: Server FULL");
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		}
	}
	
	/*  */
	public static synchronized MessagePacket testAndAcceptClient(String username, MazewarServerWorker worker) {
		MessagePacket packet = new MessagePacket();
		
		if (gameStarted || serverWorkers.size() == maxClients) {
			packet.messageType = MessagePacket.ADMIN_MESSAGE_TYPE_JOIN_GAME_FAILURE;
			packet.reason = MessagePacket.ERROR_REASON_SERVER_FULL;
		}
		
		else if (hasPlayer(username)) {
			packet.messageType = MessagePacket.ADMIN_MESSAGE_TYPE_JOIN_GAME_FAILURE;
			packet.reason = MessagePacket.ERROR_REASON_PLAYERNAME_EXISTS;
		}
		else {
			serverWorkers.addElement(worker);
			packet.messageType = MessagePacket.ADMIN_MESSAGE_TYPE_JOIN_GAME_SUCCESS;
		}
		
		return packet;
	}
	
	public static synchronized boolean hasPlayer(String username) {
		Iterator<MazewarServerWorker> it = serverWorkers.iterator();
		while (it.hasNext()) {
			if (it.next().getName().equals(username))
				return true;
		}
		return false;
	}
	
	public static void main(String [ ] args)
	{
		
		if (args.length != 1) {
			System.err.println("Usage: java MazewarServer <port number>");
			System.exit(1);
		}
		
		portNumber = Integer.parseInt(args[0]);
		
		startServer();
		
	
	}

}

