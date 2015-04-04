import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * Centralized server for Mazewar
 * 
 * Responsible for initial server setup and accepting clients.
 * Clients will be rejected if game already has begun
 */
public class NamingServer {
	
	public static final int maxClients = 4;
	private static int portNumber;
	private static ServerSocket serverSocket;
	private static Vector<ClientInfo> clients;
	
	
	/*
	 * Initialize server 
	 * */
	private static void serverInit() {
		clients = new Vector<ClientInfo>();
		
		/* Create server socket for incoming connection */
		try {
			serverSocket = new ServerSocket(portNumber);
		}
		catch (IOException e) {
			System.out.println("runServer() - Could not listen on port " + portNumber);
			System.exit(1);
		}
		
	}
	
	/* 
	 * Server main loop: handle incoming requests from clients accordingly 
	 * */
	private static void run() {
		
		serverInit();


		
		/* Accept Clients, spawn thread as delegate */
		while (true) {
			try {
				System.err.println("Waiting for clients' connection");
				/* Accept Incoming connection */
				Socket clientSocket = serverSocket.accept();
				System.err.println("Accepted Client");
				ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
				ObjectInputStream inStream= new ObjectInputStream(clientSocket.getInputStream());
				try {
					ControlMessage msg = (ControlMessage) inStream.readObject();
					ControlMessage response = testAndAcceptClient(msg);
					outStream.writeObject(response);
					outStream.flush();

					clientSocket.close();
					inStream.close();
					outStream.close();
				}
				catch (ClassNotFoundException e) {
					System.err.println("run() - received message is not control message");
				}

			}
			catch(IOException e) {
				System.out.println("runServer() - IOException from accept()");
				System.exit(1);
			}
		}
	
	}
	
	
	/* 
	 * Takes control message packet from client and create response message as
	 * as following rules
	 * 	- game is full or given username exists:
	 * 		respond to user with appropriate error message
	 * - otherwise (success)
	 * 		add user to our clients vector and respond to user with success message  
	 * 
	 * */
	public static synchronized ControlMessage testAndAcceptClient(ControlMessage cmsg) {
		ControlMessage packet = new ControlMessage();
		
		if (clients.size() == maxClients) {
			System.err.println("Rejected Client - Max Clients Reached");
			packet.messageType = ControlMessage.JOIN_GAME_REQUEST_FAILURE_MAX_CLIENT_REACHED;
		}
		
		
		else if (hasPlayer(cmsg.username)) {
			System.err.println("Rejected Client - username exists");
			packet.messageType = ControlMessage.JOIN_GAME_REQUEST_FAILURE_USERNAME_EXISTS;
		}
		
		else {
			System.err.println("Accepted Client");
			clients.addElement(cmsg.myInfo);
			packet.messageType = ControlMessage.JOIN_GAME_REQUEST_SUCCESS;
			packet.clients = clients;
		}
		
		return packet;
	}
	
	/*
	 * Test whether given username already exists in our system.
	 */
	public static synchronized boolean hasPlayer(String username) {
		Iterator<ClientInfo> it = clients.iterator();
		while (it.hasNext()) {
			if (it.next().username.equals(username)) return true;
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
		run();
		
	
	}

}

