import java.io.Serializable;
import java.util.Vector;


/**
 * Control Message used for administrative purpose such as 
 * 		- Contacting naming server
 * 		- setting up connection with peer
 * 
 * @author denny
 *
 */
public class ControlMessage implements Serializable {


	/* Messages to naming server to join games */
	public static final int JOIN_GAME_REQUEST = 100;
	public static final int JOIN_GAME_REQUEST_SUCCESS = 101;
	public static final int JOIN_GAME_REQUEST_FAILURE_USERNAME_EXISTS = 102;
	public static final int JOIN_GAME_REQUEST_FAILURE_MAX_CLIENT_REACHED = 103;
	
	public static final int CONN_INIT_REQUEST = 200;
	public static final int CONN_INIT_SUCCESS = 201;
	
	
	
	public int messageType; /* Message types defined above */
	public String username; /* My username */
	ClientInfo myInfo; /* My Information; inetaddr, port, etc */
	
	Vector<ClientInfo> clients; /* Other clients' info - filled in by server */
	
	public ControlMessage() {
		myInfo = new ClientInfo();
	}
	
}
