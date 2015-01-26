import java.io.Serializable;


public abstract class Message implements Serializable{

	private static final int MESSAGE_TYPE_CONTROL_MESSAGE = 0;
	private static final int MESSAGE_TYPE_GAME_MESSAGE = 1;
	
	private int messageType;
	
	public Message(int messageType) {
		assert (messageType >= 0 && messageType < 2);
		this.messageType = messageType;
	}
	
	public boolean isGameMessage() {
		return (this.messageType == MESSAGE_TYPE_GAME_MESSAGE);
	}
	
	
	
}
