import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;



public class MazewarServerBroadcaster implements Runnable{
	
	public MazewarServerBroadcaster () {
		
	}
	
	public void run() {
		
		sendStartGameMessage();
		(new Thread(new ProjectileTickGenerator())).start();
		while (! Thread.currentThread().isInterrupted()) {
			if (MazewarServer.hasNextMessage()) {
				MessagePacket msg = MazewarServer.dequeueMessage();
				broadcast(msg);
			}
		}
	}

	private void broadcast(MessagePacket packet) {
		MazewarClient client;
		
		packet.seqNo = MazewarServer.getNextSeqNum();
		
		Iterator<MazewarClient> it = MazewarServer.getClientIter();

		/* Broadcast to all clients*/
		while (it.hasNext()) {
			client = it.next();
			try {
				client.outputStream.writeObject(packet);
				client.outputStream.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public void sendStartGameMessage() {
		
		System.err.println("Starting Game!");
		
		/* Setup start message with seqNum 0 */
		MessagePacket startMessage = new MessagePacket();
		
		startMessage.messageType = MessagePacket.ADMIN_MESSAGE_TYPE_GAME_START;
		assert(startMessage.seqNo == 0);

		Iterator <MazewarClient> it = MazewarServer.getClientIter();
		startMessage.playerLocations = new HashMap<String, Point>();
		startMessage.playerDirections = new HashMap<String, Direction>();
		MazewarClient client = null;
		Point newLoc;
		Direction newDirection;
		/* Determine starting location - Make sure not to include same location among different players */
		Random randomGen = new Random(System.currentTimeMillis());

		
		while (it.hasNext()) {
			client = it.next();
			newLoc = MazeImpl.getRandomPoint(randomGen);
			newDirection = Direction.random();
			while (startMessage.playerLocations.containsValue(newLoc)) {
				newLoc = MazeImpl.getRandomPoint(randomGen);
			}
			
			startMessage.playerLocations.put(client.getName(), newLoc);
			startMessage.playerDirections.put(client.getName(), newDirection);
		}

		broadcast(startMessage);

	}
	
	
}
