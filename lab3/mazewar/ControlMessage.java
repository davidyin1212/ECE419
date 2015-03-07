import java.net.InetAddress;
import java.util.Vector;


public class ControlMessage {

	/* Messages to naming server to join games */
	public static final int JOIN_GAME_REQUEST = 100;
	public static final int JOIN_GAME_REQUEST_SUCCESS = 101;
	public static final int JOIN_GAME_REQUEST_FAILURE_USERNAME_EXISTS = 102;
	public static final int JOIN_GAME_REQUEST_FAILURE_MAX_CLIENT_REACHED = 103;
	
	public int messageType; /* Message types defined above */
	public String username; /* My username */
	ClientInfo myInfo; /* My Information; inetaddr, port, etc */
	
	Vector<ClientInfo> clients; /* Other clients' info - filled in by server */
	
	
	
}
