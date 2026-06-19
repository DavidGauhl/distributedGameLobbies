package ds.assignment.game;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class ZooKeeperConnection {

    private final String connectString;
    private final int sessionTimeoutMs;
    private ZooKeeper zooKeeper;

    public ZooKeeperConnection(String connectString, int sessionTimeoutMs) {
        this.connectString = connectString;
        this.sessionTimeoutMs = sessionTimeoutMs; // Telling Zookeeper how long a session is "alive" while not communicating
    }

    public ZooKeeper connect() throws IOException, InterruptedException {
        CountDownLatch connectedSignal = new CountDownLatch(1); //gateWay to continue is closed

        this.zooKeeper = new ZooKeeper(connectString, sessionTimeoutMs, event -> {
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                connectedSignal.countDown(); //gateWay to continue is opened
            }
        });

        connectedSignal.await();
        return zooKeeper;
    }

    public void close() throws InterruptedException {
        if (zooKeeper != null) {
            zooKeeper.close();
        }
    }
}