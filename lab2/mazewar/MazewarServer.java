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
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * Centralized server for Mazewar
 */
public class MazewarServer {
	
	private static int portNumber;
	private static Vector<MazewarClient> clients; /* Vector containing worker per client*/
	private static ServerSocket serverSocket;
	private static int nextSeqNum;
	private static int maxClients;
	private static boolean gameStarted;
	private static ConcurrentLinkedQueue<MessagePacket> messageQueue;
	private static MazewarServerBroadcaster broadcaster;
	
	public static synchronized void enqueueMessage(MessagePacket packet) {
		messageQueue.add(packet);
	}
	
	public static synchronized MessagePacket dequeueMessage() {
		return messageQueue.poll();
	}
	public static synchronized boolean hasNextMessage() {
		return messageQueue.peek() != null;
	}
	
	public static void addClient(MazewarClient client) {
		clients.add(client);
		System.out.println("Add Client :" + clients.size());
	}
	
	public static boolean isFullServer() {
		return clients.size() == maxClients;
	}
	
	
	public static Iterator<MazewarClient> getClientIter() {
		return clients.iterator();
	}

	public static int getNextSeqNum() {
		return nextSeqNum++;
	}	
	
	private static void serverInit() {
		clients = new Vector<MazewarClient>();
		nextSeqNum = 0;
		gameStarted = false;
		messageQueue = new ConcurrentLinkedQueue<MessagePacket>();
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
		
		System.err.println("Waiting for clients' connection");
		/* Accept Clients, spawn thread as delegate */
		while (true) {
			try {
				MazewarClient worker;
				/* Accept Incoming connection */
				
				worker = new MazewarClient(serverSocket.accept());
				
				/* Start worker thread */
				(new Thread(worker)).start(); 
			}
			catch(IOException e) {
				System.out.println("runServer() - IOException from accept()");
				System.exit(1);
			}
		}
	
	}
	
	public static void startGame() {
		assert (clients.size() == maxClients);
		
		broadcaster = new MazewarServerBroadcaster();
		(new Thread(broadcaster)).start();
	}
	
	/*  */
	public static synchronized MessagePacket testAndAcceptClient(String username, MazewarClient worker) {
		MessagePacket packet = new MessagePacket();
		
		if (gameStarted || clients.size() == maxClients) {
			packet.messageType = MessagePacket.ADMIN_MESSAGE_TYPE_JOIN_GAME_FAILURE;
			packet.reason = MessagePacket.ERROR_REASON_SERVER_FULL;
		}
		
		else if (hasPlayer(username)) {
			packet.messageType = MessagePacket.ADMIN_MESSAGE_TYPE_JOIN_GAME_FAILURE;
			packet.reason = MessagePacket.ERROR_REASON_PLAYERNAME_EXISTS;
		}
		else {
			clients.addElement(worker);
			packet.messageType = MessagePacket.ADMIN_MESSAGE_TYPE_JOIN_GAME_SUCCESS;
		}
		
		return packet;
	}
	
	public static synchronized boolean hasPlayer(String username) {
		Iterator<MazewarClient> it = clients.iterator();
		while (it.hasNext()) {
			if (it.next().getName().equals(username))
				return true;
		}
		return false;
	}
	
	public static void main(String [ ] args)
	{
		maxClients = 4;
		
		if (args.length < 1 || args.length > 2) {
			System.err.println("Usage: java MazewarServer <port number> [MAX_NUM_PLAYER]");
			System.exit(1);
		}
		
		portNumber = Integer.parseInt(args[0]);
		
		if (args.length == 2) {
			maxClients = Integer.parseInt(args[1]);
		}
		
		startServer();
		
	
	}

}

