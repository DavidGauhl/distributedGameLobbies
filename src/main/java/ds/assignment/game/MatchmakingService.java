package ds.assignment.game;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MatchmakingService implements Runnable {

    private final String managerId;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger matchCounter = new AtomicInteger(1);
    private final Random random = new Random();

    private final Queue<String> players = new ArrayDeque<>(List.of(
            "Alice", "Bob", "Carlos", "Dina",
            "Eli", "Fatima", "George", "Hana",
            "Ivan", "Julia", "Kaito", "Lina",
            "Marco", "Nora", "Omar", "Priya"
    ));

    private final List<String> servers = List.of(
            "GameServer-EU-1",
            "GameServer-EU-2",
            "GameServer-EU-3"
    );

    private final List<String> maps = List.of(
            "Harbor",
            "Desert Base",
            "Mountain Pass"
    );

    public MatchmakingService(String managerId) {
        this.managerId = managerId;
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            Thread thread = new Thread(this, "matchmaking-" + managerId);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void run() {
        System.out.println(managerId + " started matchmaking loop.");

        while (running.get()) {
            try {
                createMatchIfPossible();
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void createMatchIfPossible() {
        if (players.size() < 4) {
            System.out.println("[LEADER " + managerId + "] Not enough players. Refilling queue.");
            refillPlayers();
            return;
        }

        String p1 = players.poll();
        String p2 = players.poll();
        String p3 = players.poll();
        String p4 = players.poll();

        String server = servers.get(random.nextInt(servers.size()));
        String map = maps.get(random.nextInt(maps.size()));
        int matchId = matchCounter.getAndIncrement();

        System.out.println(
                "[LEADER " + managerId + "] Created Match-" + matchId
                        + " | players=[" + p1 + ", " + p2 + ", " + p3 + ", " + p4 + "]"
                        + " | server=" + server
                        + " | map=" + map
        );
    }

    private void refillPlayers() {
        players.addAll(List.of(
                "Alice", "Bob", "Carlos", "Dina",
                "Eli", "Fatima", "George", "Hana"
        ));
    }
}