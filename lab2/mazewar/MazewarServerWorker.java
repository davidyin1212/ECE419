import java.net.Socket;


public class MazewarServerWorker implements Runnable {

	public Socket socket;
	
	public MazewarServerWorker(Socket socket) {
		this.socket = socket;
	}
	
	public void run() {
		System.out.println("Thread Started");
		while (true) {
			
		}
	}
}
