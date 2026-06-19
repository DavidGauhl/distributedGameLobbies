package ds.assignment.game;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.ZooKeeper;

public class LobbyManager {

    public static void main(String[] args) throws Exception {
        String managerId = "lobby-" + UUID.randomUUID().toString().substring(0, 5);
        String region = "EU";
        String connectString = "localhost:2181";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--id") && i + 1 < args.length) {
                managerId = args[i + 1];
            }

            if (args[i].equals("--region") && i + 1 < args.length) {
                region = args[i + 1];
            }

            if (args[i].equals("--zk") && i + 1 < args.length) {
                connectString = args[i + 1];
            }
        }

        System.out.println("Starting Lobby Manager");
        System.out.println("managerId=" + managerId);
        System.out.println("region=" + region);
        System.out.println("zookeeper=" + connectString);

        ZooKeeperConnection connection = new ZooKeeperConnection(connectString, 5000);
        ZooKeeper zooKeeper = connection.connect();

        final String finalManagerId = managerId;

        MatchmakingService matchmakingService = new MatchmakingService(managerId);

        LeaderElection leaderElection = new LeaderElection(zooKeeper, managerId, region);
        leaderElection.setOnBecameLeader(matchmakingService::start);
        leaderElection.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println(finalManagerId + " shutting down.");
                connection.close();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));

        new CountDownLatch(1).await();
    }
}