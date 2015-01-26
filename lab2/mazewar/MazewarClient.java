import java.net.Socket;


public class MazewarClient implements Runnable{
	private final int CONNECT_FAILURE_SLEEP_INTERVAL = 3000;
	
	private int portNumber;
	private String hostname; 
	private boolean isConnected;
	private Socket clientSocket; 
	
	public MazewarClient(String hostname, int portNumber) {
		this.hostname = hostname;
		this.portNumber = portNumber;
		this.isConnected = false;
	}


	@Override
	public void run() {
		connectToServer();
		
	}
	
	
	public void connectToServer() {
		System.out.println("Trying to connect to server...");
		/* Try until */
		while (! isConnected) {
			try {
				this.clientSocket = new Socket(hostname, portNumber);
				isConnected = true;
			}
			/* Upon failure, sleep thread for  3 secs */
			catch (Exception e) {
				System.out.println ("Could not connect to server.. will try again in 3 seconds");
				e.printStackTrace();

				try {
					Thread.sleep(CONNECT_FAILURE_SLEEP_INTERVAL);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		
		System.out.println("Successfully connected to server");
	}
	 
	
	
	
}
