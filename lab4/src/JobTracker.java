import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.Watcher.Event.EventType;

import java.io.IOException;

import java.net.*;
import java.io.*;
import java.util.*;

public class JobTracker {

    String myPath = "/jobtracker";
    String workingonPath = "/workingon";
    String solvedPath = "/solved";
    String donePath = "/done";


    ZkConnector zkc;
    Watcher watcher;
    static String server_conn;
    static int server_port;

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Usage: java -classpath lib/zookeeper-3.3.2.jar:lib/log4j-1.2.15.jar:. JobTracker zkServer:Port myOwnPort");
            return;
        }

        String zk_conn = args[0];
        server_port = Integer.parseInt(args[1]);


        try {
            //get our own ipaddress
            server_conn = InetAddress.getLocalHost().getHostName();

            //connect to zookeeper
            JobTracker t = new JobTracker(zk_conn);

            //create znode /jobtracker
            t.checkpath();

            //listen on port
            System.out.println("Listening for clients to connect");
            ServerSocket serverSocket = new ServerSocket(server_port);
            while (true) {
                Socket socket = serverSocket.accept();
                new JobTrackerThread(socket, zk_conn).start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    public JobTracker(String hosts) {
        zkc = new ZkConnector();
        try {
            zkc.connect(hosts);
        } catch(Exception e) {
            System.out.println("Zookeeper connect "+ e.getMessage());
        }

        watcher = new Watcher() { // Anonymous Watcher
            @Override
            public void process(WatchedEvent event) {
                handleEvent(event);

            } };
    }

    private void checkpath() {
        //System.out.println("Checking if path exists: "+myPath);
        Stat stat = zkc.exists(myPath, watcher);
        if (stat == null) {              // znode doesn't exist; let's try creating it
            System.out.println("Creating " + myPath);
            Code ret = zkc.create(
                    myPath,         // Path of znode
                    (server_conn+":"+server_port),  //jobtracker conn info for client
                    CreateMode.EPHEMERAL   // Znode type, set to EPHEMERAL.
            );
            if (ret == Code.OK) System.out.println("I am JobTracker Primary");
        }

        Stat stat2 = zkc.exists(workingonPath, watcher);
        if (stat2 == null) {              // znode doesn't exist; let's try creating it
            System.out.println("Creating " + workingonPath);
            Code ret = zkc.create(
                    workingonPath,         // Path of znode
                    (server_conn+":"+server_port),  //jobtracker conn info for client
                    CreateMode.PERSISTENT   // Znode type, set to EPHEMERAL.
            );
        }

        Stat stat3 = zkc.exists(solvedPath, watcher);
        if (stat3 == null) {              // znode doesn't exist; let's try creating it
            System.out.println("Creating " + solvedPath);
            Code ret = zkc.create(
                    solvedPath,         // Path of znode
                    (server_conn+":"+server_port),  //jobtracker conn info for client
                    CreateMode.PERSISTENT   // Znode type, set to EPHEMERAL.
            );
        }

        Stat stat4 = zkc.exists(donePath, watcher);
        if (stat4 == null) {              // znode doesn't exist; let's try creating it
            System.out.println("Creating " + donePath);
            Code ret = zkc.create(
                    donePath,         // Path of znode
                    (server_conn+":"+server_port),  //jobtracker conn info for client
                    CreateMode.PERSISTENT   // Znode type, set to EPHEMERAL.
            );
        }

    }

    private void handleEvent(WatchedEvent event) {

        String path = event.getPath();
        EventType type = event.getType();
        if(path.equalsIgnoreCase(myPath)) {
            if (type == EventType.NodeDeleted) {
                System.out.println(myPath + " deleted! Let's go!");
                checkpath(); // try to become the boss
            }
            if (type == EventType.NodeCreated) {
                System.out.println(myPath + " created!");
                try{ Thread.sleep(3000); } catch (Exception e) {}
                checkpath(); // re-enable the watch
            }
        }
    }

}