import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;


public class MazewarServer {
	private static int portNumber;
	private static Vector<MazewarServerWorker> serverWorkers; /* Vector containing worker per client*/
	private static int nextClientID;
	
	
	
	private static ServerSocket serverSocket;
	private static PrintWriter out;
	private static BufferedReader in;
	private static BufferedReader stdIn;

	
	private static void serverInit() {
		serverWorkers = new Vector<MazewarServerWorker>();
		nextClientID = 0; /* Initially 0, incremented every time when assigned to client */
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
		
		/* Accept clients */ 
		while (true) {
			try {
				MazewarServerWorker worker;
				/* Accept Incoming connection */
				System.out.println("Waiting for clients' connection");
				worker = new MazewarServerWorker(serverSocket.accept());
				System.out.println("Accepted Client");
				
				/* Register designated client socket into our vector*/
				serverWorkers.add(worker);
				
				/* Start worker thread */
				(new Thread(worker)).start(); 
			}
			catch(IOException e) {
				System.out.println("runServer() - IOException from accept()");
				System.exit(1);
			}
		}
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

