
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

public class ZKConnector {
	private int timeout = 5000;
	private ZooKeeper zk;
	
	public ZooKeeper connect(String host) throws IOException, InterruptedException, IllegalStateException {
		CountDownLatch cdl = new CountDownLatch(1);
		
		Watcher watcher = new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				if (event.getState() == KeeperState.SyncConnected)
					cdl.countDown();	
			}
		};
		
		System.out.println("Connecting to ZooKeeper");
		zk = new ZooKeeper(host, timeout, watcher); //hostname, timeout, watcher 
		cdl.await();
		System.out.println("Connection to ZooKeeper established");
		return zk;
	}
	
	public void close() throws InterruptedException {
		System.out.println("Connection to ZooKeeper closed");
		zk.close();
	}
}

