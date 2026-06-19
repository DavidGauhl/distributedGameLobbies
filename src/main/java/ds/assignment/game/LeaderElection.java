package ds.assignment.game;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

public class LeaderElection {

    private final ZooKeeper zooKeeper;
    private final String managerId;
    private final String electionPath;

    private String currentZnodeName;
    private boolean leader = false;
    private Runnable onBecameLeader;

    public LeaderElection(ZooKeeper zooKeeper, String managerId, String region) {
        this.zooKeeper = zooKeeper;
        this.managerId = managerId;
        this.electionPath = "/game/" + region.toLowerCase() + "/lobby-election";
    }

    // onBecameLeader is what to do once becoming the leader
    public void setOnBecameLeader(Runnable onBecameLeader) {
        this.onBecameLeader = onBecameLeader;
    }

    public boolean isLeader() {
        return leader;
    }

    public void start() throws KeeperException, InterruptedException {
        ensurePath("/game");
        ensurePath(parentPath(electionPath));
        ensurePath(electionPath);

        volunteerForLeadership();
        electLeader();
    }

    // Check if a znode exists and if not create it
    private void ensurePath(String path) throws KeeperException, InterruptedException {
        if (zooKeeper.exists(path, false) == null) {
            try {
                zooKeeper.create(
                        path,
                        new byte[0],
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT
                );
            } catch (KeeperException.NodeExistsException ignored) {
                // other process maybe created it already
            }
        }
    }

    private String parentPath(String path) {
        int index = path.lastIndexOf('/');
        return path.substring(0, index);
    }

    // method for lobbie manager to register itself
    private void volunteerForLeadership() throws KeeperException, InterruptedException {
        String znodePrefix = electionPath + "/candidate-";

        String fullPath = zooKeeper.create(
                znodePrefix,
                managerId.getBytes(StandardCharsets.UTF_8),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, // for simplification reason everyone connected has access to all znodes
                CreateMode.EPHEMERAL_SEQUENTIAL //node configuration
        );

        this.currentZnodeName = fullPath.substring(electionPath.length() + 1);

        System.out.println(managerId + " created election znode: " + currentZnodeName);
    }

    // election logic
    private void electLeader() throws KeeperException, InterruptedException {
        List<String> children = zooKeeper.getChildren(electionPath, false);
        Collections.sort(children);

        String smallestChild = children.get(0);

        if (smallestChild.equals(currentZnodeName)) {
            leader = true;
            System.out.println(managerId + " is now LEADER");

            if (onBecameLeader != null) {
                onBecameLeader.run();
            }
        } else {
            leader = false;

            int currentIndex = children.indexOf(currentZnodeName);
            String predecessor = children.get(currentIndex - 1);

            System.out.println(managerId + " is FOLLOWER. Watching " + predecessor);

            watchPredecessor(predecessor);
        }
    }

    private void watchPredecessor(String predecessor) throws KeeperException, InterruptedException {
        String predecessorPath = electionPath + "/" + predecessor;

        Watcher watcher = event -> {
            switch (event.getType()) {
                case NodeDeleted -> {
                    System.out.println(managerId + " noticed predecessor disappeared. Re-running election.");
                    try {
                        electLeader();
                    } catch (KeeperException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                default -> {
                    // no need to check other events for this
                }
            }
        };

        if (zooKeeper.exists(predecessorPath, watcher) == null) {
            electLeader();
        }
    }

}