import java.io.Serializable;


/**
 * GameMessage which contains game action information from "senderName" 
 * @author denny
 *
 */
public class GameMessage implements Serializable, Comparable<GameMessage>{


	
	public static final int GAME_MESSAGE_TYPE_MOVE_PLAYER_FORWARD = 301;
	
	public static final int GAME_MESSAGE_TYPE_MOVE_PLAYER_BACKWARD = 302;
	
	public static final int GAME_MESSAGE_TYPE_TURN_LEFT = 304;
	
	public static final int GAME_MESSAGE_TYPE_TURN_RIGHT = 305;
	
	public static final int GAME_MESSAGE_TYPE_FIRE = 306;
	
	public static final int GAME_MESSAGE_TYPE_PROJECTILE_TICK = 307;
	
	public static final int GAME_MESSAGE_NULL = 400;
	
	
	public int clock;
	public String senderName;
	public int messageType;

	/* Compare priority => Lower sequence number, higher priority*/
	public int compareTo(GameMessage o) {
		if (! (o instanceof GameMessage)) {
			throw new IllegalArgumentException("Given object is not instance of MessagePacket.");
		}
		
		GameMessage otherMsg = (GameMessage) o;
		
		if (this.clock == otherMsg.clock) {
			return 0;
		}
		else if (this.clock < otherMsg.clock) {
			return -1;
		}
		else {
			return 1;
		}
		
	}
	
	public String toString() {
		return "Sender:" + senderName + " clock:" + clock + "messageType: " + messageType;
	}
}


