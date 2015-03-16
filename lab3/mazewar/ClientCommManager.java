import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.TimerTask;
import java.util.Vector;




public class ClientCommManager implements Runnable {
	private final static int randomGenSeed = 777;
	public static Random randomGen = new Random(randomGenSeed);
	public ServerSocket connListeningSocket;
	public ClientConnectionListener connectionListener;
	private ClientInfo myInfo;

	private static PriorityQueue<GameMessage>localMessageQueue = new PriorityQueue<GameMessage>();

	public Vector<GameMessage> eventBuffer = new Vector<GameMessage>(); /*Game Message to be multicasted*/
	public Vector<ClientCommWorker> peers;
	public Mazewar game;
	private int expectedNextMsgClock = 0 ;
	private int nextClock = 0;
	long lastMessageSentAt = 0; /* Milliseconds */
	
	public Vector<GameMessage> toBeDispatched = new Vector<GameMessage>();
	
	Comparator<GameMessage> msgComparator = new Comparator<GameMessage>() {
		@Override
		public int compare(GameMessage m1, GameMessage m2) {

			return m1.senderName.compareTo(m2.senderName);
		}
	};
	
	public ClientCommManager(Mazewar game) {
		try {
			this.game = game;
			
			connListeningSocket = new ServerSocket(0);
			myInfo = new ClientInfo();
			myInfo.addr = connListeningSocket.getInetAddress();
			myInfo.portNo = connListeningSocket.getLocalPort();
			connectionListener = new ClientConnectionListener(this);
			peers = new Vector<ClientCommWorker>();
			
			(new Thread(connectionListener)).start();
		} catch (IOException e) {
			System.err.println("ClinetCommManager - error while initializing socket");
			System.exit(1);;
		}

	}
	
	public ClientInfo getMyInfo() {
		return myInfo;
	}
	
	public String getMyname() {
		return myInfo.username;
	}
	public void setClientUsername(String name) {
		myInfo.username = name;
	}
	
	public boolean initializeConnection(Vector<ClientInfo> clientList) {
		System.out.println("Sending INIT_CONN message to following clients");
		Iterator<ClientInfo> it= clientList.iterator();
		
		while (it.hasNext()) {
			ClientInfo peer = it.next();
			
			// Ignore myself in client list (this client is in the clientList)
			if (! peer.equals(myInfo)) {
				try {
					Socket socket = new Socket(peer.addr, peer.portNo);
					ClientCommWorker ccm = new ClientCommWorker(socket, this);
					addPeer(ccm);
					ccm.sendConnInitMsg(getMyInfo(), peer);
					
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println(peer.toString());
			}
			
		}
		
		return true;
	}
	
	public synchronized void addPeer(ClientCommWorker peer) {
		peers.addElement(peer);
	}
	
	
	public synchronized boolean startGameIfReady() {

		if (peers.size() == NamingServer.maxClients - 1) {
			System.out.println(" OKAY ! STARTING GAME");
			game.startGame();
			
			for (ClientCommWorker worker : peers) {
				(new Thread(worker)).start();
			}
			(new Thread(this)).start();
			return true;
		}
		System.out.println("Need more clients to start game");
		return false;
	}
	
	public synchronized void multicast(GameMessage message) {
		message.clock = nextClock;
		nextClock++;
		System.out.println("multicasting!");
		message.senderName = myInfo.username;
		
		// Buffer my own message
		localMessageQueue.add(message);

		Iterator<ClientCommWorker> workers = peers.iterator();
		while (workers.hasNext()) {
			ClientCommWorker peer = workers.next();
			peer.sendMessage(message);
		}
	}

	public synchronized boolean hasNextLocalMessage(int clock) {
		if (localMessageQueue.peek() == null) return false;
		
		return localMessageQueue.peek().clock == clock;
	}
	
	public synchronized GameMessage getNextLocalMessage() {
		return localMessageQueue.poll();
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub

		while (! Thread.currentThread().isInterrupted()) {

			boolean msgRdy = true;
			toBeDispatched.clear();
			
			// Periodically send message, even though there was no event
			long curTime = new Date().getTime();
			if (curTime - lastMessageSentAt >= 100) {
				if (eventBuffer.size() > 0) {
					multicast(eventBuffer.remove(0));
				}
				else {
					GameMessage msg = new GameMessage();
					msg.senderName = myInfo.username;
					msg.messageType = GameMessage.GAME_MESSAGE_NULL;
					multicast(msg);
				}
				lastMessageSentAt = curTime;
			}
			
			if (hasNextLocalMessage(expectedNextMsgClock)) {

				
				for (ClientCommWorker cm : peers) {
					if (! cm.hasNextMessage(expectedNextMsgClock)) {
						msgRdy = false;
					}
				}

				if (msgRdy) {
			
					toBeDispatched.add(getNextLocalMessage());
					
					for (ClientCommWorker cm : peers) {
						GameMessage msg = cm.getNextMessage();

						toBeDispatched.add(msg);
					}

					toBeDispatched.sort(msgComparator);
					for (GameMessage msg : toBeDispatched) {
						// process message here
						System.out.println(msg.clock);
					}
					
					expectedNextMsgClock++;
				}
				
			}
			
		}
	}
	
	
}
