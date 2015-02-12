import java.util.Iterator;
import java.util.PriorityQueue;


public class ClientEventDispatcher implements Runnable{
	
	private static PriorityQueue<MessagePacket> orderedMessageQueue = new PriorityQueue<MessagePacket>();
	private static int nextMsgSeqNo = 0;
	
	Mazewar game;

	
	public ClientEventDispatcher(Mazewar game) {
		this.game = game;
	}
	
	public static synchronized void addMessage(MessagePacket message) {
		orderedMessageQueue.add(message);
	}
	
	private static synchronized MessagePacket getNextMessage() {
		MessagePacket msg = orderedMessageQueue.peek();
		
		if (msg != null && msg.seqNo == nextMsgSeqNo) {
			nextMsgSeqNo++;
			return orderedMessageQueue.poll();
		}
		
		return null;
				
	}

	
	public void run() {
		while (! Thread.currentThread().isInterrupted()) {
			MessagePacket msg = getNextMessage();
			
			if (msg != null) {
				
				/* Initial game start message need to be dealt specially */
				if (msg.seqNo == 0 && msg.messageType == MessagePacket.ADMIN_MESSAGE_TYPE_GAME_START) {
					game.startGame(msg);
				}
				
				else {
					if (msg.messageType == MessagePacket.GAME_MESSAGE_TYPE_PROJECTILE_TICK) {
						game.missileTick();
					}
					
					/* Is it for GUIClient ?*/
					else if (msg.playerName.equals(game.guiClient.getName())) {
						game.guiClient.receiveMessage(msg);

					}
					/* It's for remote client*/
					else {
						Iterator<RemoteClient> it = game.remoteClients.iterator();
						RemoteClient rc;
						while (it.hasNext()) {
							rc = it.next();
							if (msg.playerName == rc.getName()) {
								rc.receiveMessage(msg);
							}
						}
						
					}
				}
			}
		}
		
	}
	
	
	
}
