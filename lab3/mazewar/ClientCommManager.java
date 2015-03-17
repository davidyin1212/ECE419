import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.TimerTask;
import java.util.Vector;



/**
 * Communication Manager which is responsible for 
 *  	- setting up connection to each peers
 * 		- propagating received messages to our game
 * 		
 * @author denny
 *
 */
public class ClientCommManager implements Runnable, MazeListener{
	
	/* Random Generator */
	private final static int randomGenSeed = 777;
	public static Random randomGen = new Random(randomGenSeed);
	
	/* Socket and delegate class for listening incoming connection*/
	public ServerSocket connListeningSocket;
	public ClientConnectionListener connectionListener;
	
	/* My information - username, location, etc*/
	private ClientInfo myInfo;

	/* Local game message buffer for sent messages */
	private static PriorityQueue<GameMessage>localMessageQueue = new PriorityQueue<GameMessage>();

	/* Local game message buffer for unsent message (queued for multicast) */
	public Vector<GameMessage> eventBuffer = new Vector<GameMessage>(); /*Game Message to be multicasted*/
	
	/* Communication workers for each peer */
	public Vector<ClientCommWorker> peers;
	
	/* Mazewar game holding this class*/
	public Mazewar game;
	
	/* Clock(seqNo) we expect to process next */
	private int expectedNextMsgClock = 0 ;
	
	/* Clock(seqNo) that we will bind for next multicast */
	private int nextClock = 0;
	
	/* Timestamp of last multicast in miliseconds */
	long lastMessageSentAt = 0; /* Milliseconds */
	
	/* Vector holding game message from each client that will be propagated next to the game*/
	public Vector<GameMessage> toBeDispatched = new Vector<GameMessage>();
	
	/* Comparator: sort based on sender's name */
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
			//myInfo.addr = connListeningSocket.getInetAddress();
			myInfo.addr = InetAddress.getByName( InetAddress.getLocalHost().getHostAddress());
			//myInfo.addr = InetAddress.getLocalHost().getHostAddress();
			System.out.println("fucking shit" +  InetAddress.getLocalHost().getHostAddress());
			
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
			System.out.println(" Connection setup with peers done - starting game!");
			game.startGame();
			
			for (ClientCommWorker worker : peers) {
				(new Thread(worker)).start();
			}
			(new Thread(this)).start();
			return true;
		}

		return false;
	}
	
	public synchronized void multicast(GameMessage message) {
		message.clock = nextClock;
		nextClock++;
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
	
	public synchronized void registerLocalEvent(GameMessage message) {
		message.senderName = myInfo.username;
		eventBuffer.addElement(message);
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
			
			// Propagate buffered message if received from each peers 
			if (hasNextLocalMessage(expectedNextMsgClock)) {
				
				for (ClientCommWorker cm : peers) {
					if (! cm.hasNextMessage(expectedNextMsgClock)) {
						msgRdy = false;
					}
				}
				// Yes we received message from each peer
				if (msgRdy) {
			
					toBeDispatched.add(getNextLocalMessage());
					
					for (ClientCommWorker cm : peers) {
						GameMessage msg = cm.getNextMessage();

						toBeDispatched.add(msg);
					}

					//toBeDispatched.sort(msgComparator); // Sort based on username
					Collections.sort(toBeDispatched, msgComparator);
					// Propagate game message
					for (GameMessage msg : toBeDispatched) {

						Client client = game.getClient(msg.senderName);
						switch (msg.messageType) {
							case (GameMessage.GAME_MESSAGE_TYPE_FIRE):
								client.fire();
								break;
							case (GameMessage.GAME_MESSAGE_TYPE_MOVE_PLAYER_BACKWARD):
								client.backup();
								break;
							
							case (GameMessage.GAME_MESSAGE_TYPE_MOVE_PLAYER_FORWARD):
								client.forward();
								break;
							
							case (GameMessage.GAME_MESSAGE_TYPE_SPAWN_PLAYER):
								game.maze.respawnClient(client, randomGen);
								break;
							
							case (GameMessage.GAME_MESSAGE_TYPE_TURN_LEFT):
								client.turnLeft();
								break;
							
							case (GameMessage.GAME_MESSAGE_TYPE_TURN_RIGHT):
								client.turnRight();
								break;
							default:
								
								break;
						}
	
					}
					
					/*
					 * Missile tick every 200ms
					 * Since we send/receive message every 100ms, we have to tick
					 * missile on every second message 
					 */
					if ((expectedNextMsgClock + 1) % 2 == 0) {
						game.maze.tickMissile();
					}
					expectedNextMsgClock++;
				}
				
			}
			
		}
	}

	@Override
	public void mazeUpdate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clientKilled(Client source, Client target) {
		if (target instanceof GUIClient) {
			GameMessage msg = new GameMessage();
			msg.senderName = myInfo.username;
			msg.messageType = GameMessage.GAME_MESSAGE_TYPE_SPAWN_PLAYER;
			
			registerLocalEvent(msg);
		}
		
	}

	@Override
	public void clientAdded(Client client) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clientFired(Client client) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clientRemoved(Client client) {
		// TODO Auto-generated method stub
		
	}
	
	
}
