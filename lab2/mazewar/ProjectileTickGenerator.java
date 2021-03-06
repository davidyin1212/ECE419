
/*
 * ProjectileTickGenerator for synchronizing projectile
 * This thread adds PROJECTILE_TICK game message to 
 * message queue every 200 miliseconds.
 */
public class ProjectileTickGenerator implements Runnable {

	@Override
	public void run() {

		while (! Thread.currentThread().isInterrupted()) {
			MessagePacket msg = new MessagePacket();
			msg.messageType = MessagePacket.GAME_MESSAGE_TYPE_PROJECTILE_TICK;
			MazewarServer.enqueueMessage(msg);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				
				System.err.println("Error from ProjectileTickGenerator thread");
			}
			
		}
	}

}
