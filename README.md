# ZooKeeper Game Lobby Leader Election

This project demonstrates **leader election with Apache ZooKeeper** in a simplified multiplayer game backend.

Several Java lobby-manager processes run at the same time, but only one is allowed to create matches. ZooKeeper coordinates which process is the current leader.

## Idea

- Each process creates an ephemeral sequential znode in ZooKeeper.
- The process with the smallest sequence number becomes leader.
- The leader simulates matchmaking.
- Followers wait on standby.
- If the leader stops, another process becomes leader automatically.

## Run ZooKeeper

```bash
docker compose up -d
```

ZooKeeper runs on:

```text
localhost:2181
```

## Run the Demo

Suggested to open three terminals.

```bash
mvn exec:java -Dexec.args="--id lobby-a --region EU"
```

```bash
mvn exec:java -Dexec.args="--id lobby-b --region EU"
```

```bash
mvn exec:java -Dexec.args="--id lobby-c --region EU"
```

One process becomes leader and starts creating matches. The others become followers.

## Test Failover

Stop the current leader process.

ZooKeeper removes the leader’s ephemeral znode. The next lobby manager detects this and becomes the new leader.

## Main Concepts

- ZooKeeper coordination service
- Ephemeral sequential znodes
- Leader election
- Watches
- Session-based failure detection
- Automatic failover
