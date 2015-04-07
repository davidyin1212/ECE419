import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;



public class FileServer {
	private final static String DICTIONARY_FILE_PATH = "dictionary/lowercase.rand";
	private final static String FILE_SERVER_PATH = "/FileServer";
	private ZKConnector zkConnector;
	private ZooKeeper zk;
	private ServerSocket serverSocket;
	public static List<String> dictionary = new Vector<String>();
	
	public FileServer(String zkHostname, String zkPortNo) throws IllegalStateException, IOException, InterruptedException, KeeperException {
		
		// Load dictionary in memory 
		loadDictionary();
		
		String zkAddr = zkHostname + ":" + zkPortNo;
		zkConnector = new ZKConnector();
		
		// Connect to ZooKeeper
		zk = zkConnector.connect(zkAddr); // Synchronous connect
		
		
		// If there is no FILE_SERVER_PATH, create one
		if (zk.exists(FILE_SERVER_PATH, false) == null) {
			// Create root of fileserver
			zk.create(FILE_SERVER_PATH, 
					"".getBytes(), 
					Ids.OPEN_ACL_UNSAFE, 
					CreateMode.PERSISTENT);
			
			String data = InetAddress.getLocalHost().toString() + ":" + 0;
			
			// Assign this as primary
			zk.create("primary", 
					data.getBytes(), 
					Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL_SEQUENTIAL);
			
		}
		
		else {
			
		}
		
		
	}
	
	public void startServer() throws IOException {
		
		
		serverSocket = new ServerSocket();
		while (true) {
			Socket socket = serverSocket.accept();
			// Start new thread here
		}
	}
	
	
	public static void loadDictionary() throws FileNotFoundException {

		Scanner scanner = new Scanner(new FileReader(new File(DICTIONARY_FILE_PATH)));
		
		while (scanner.hasNextLine()) {
			dictionary.add(scanner.nextLine());
		}
	}
	
	
	
	public static void main (String[] args) {
		
		String zkHostname;
		String zkPortNo;
		
		// Handle command line args
		if (args.length != 2) {
			System.err.println("Usage: java FileServer Hostname PortNo");
			System.exit(1);
		}
		
		zkHostname = args[0];
		zkPortNo = args[1];
		
		
	}
}
