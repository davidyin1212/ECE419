import java.io.Serializable;


public class GameMessagePacket implements Serializable {
	
	public static final String TARGET_BROADCAST = "";
	
	public static final int JOIN_GAME_REQUEST = 100;
	public static final int JOIN_GAME_SUCCESS = 101;
	public static final int JOIN_GAME_FAILURE = 102;
	
	public static final int BROADCAST_SPAWN_PLAYER = 200;
	
	
	
	public int messageType;
	public String playerName;
	public int seqNo;
	public Point point;
	public String messageTarget;/* Empty string for broadcast | playerName for specific target */
	public SetupMessagePacket setupMessagePacket;

	
}
