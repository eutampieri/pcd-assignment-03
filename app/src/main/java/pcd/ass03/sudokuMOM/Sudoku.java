package pcd.ass03.sudokuMOM;

import com.rabbitmq.client.Channel;
import de.sfuhrm.sudoku.Riddle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class Sudoku implements Game {
    static final String EXCHANGE_NAME = "sudoku_exchange";

    private final Riddle riddle;
    private final Channel channel;
    private final String id;
    private final BlockingQueue<GameUpdate> updates = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<Pair<Integer, Integer>> clicks = new ArrayBlockingQueue<>(10);
    private boolean streamGenerated = false;
    private boolean clickStreamGenerated = false;
    private final int nodeId;

    public Sudoku(Riddle riddle, Channel channel, String id) throws IOException {
        this.riddle = riddle;
        this.channel = channel;
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");
        this.id = id;
        System.err.println(riddle.toString());
        this.subscribeToUpdates();
        this.subscribeToJoins();
        this.subscribeToAnnounce();
        this.nodeId = new Random().nextInt();
        System.out.println("Initialised node " + this.nodeId);
    }

    private void subscribeToUpdates() throws IOException {
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, ChannelNames.getBaseRoutingKey(this.id));
        this.channel.basicConsume(queueName, (consumerTag, x) -> {
            String message = new String(x.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
            GameUpdate u = new GameUpdate(message);
            this.handleGameUpdate(u);
        }, x -> {
        });
    }

    private void subscribeToClicks() throws IOException {
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, ChannelNames.getClicksRoutingKey(this.id));
        this.channel.basicConsume(queueName, (consumerTag, x) -> {
            String message = new String(x.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
            String[] coords = message.split(" ");
            try {
                this.clicks.put(new Pair<>(Integer.parseInt(coords[0]), Integer.parseInt(coords[1])));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, x -> {
        });
    }


    private void subscribeToJoins() throws IOException {
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, ChannelNames.getJoinsRoutingKey(this.id));
        this.channel.basicConsume(queueName, (consumerTag, x) -> {
            int requestedNode = Integer.parseInt(new String(x.getBody(), StandardCharsets.UTF_8));
            System.out.println(requestedNode);
            if (requestedNode == this.nodeId) {
                // I am responsible for answering his request
                System.out.println("Mando la griglia");
                this.sendStatus();
            }
        }, x -> {
        });
    }

    private void subscribeToAnnounce() throws IOException {
        final String queueName = channel.queueDeclare().getQueue();
        final String routingKey = ChannelNames.getAnnounceRoutingKey(this.id);
        channel.queueBind(queueName, EXCHANGE_NAME, routingKey);
        this.channel.basicConsume(queueName, (consumerTag, x) -> {
            String body = new String(x.getBody(), StandardCharsets.UTF_8);
            if (body.isEmpty()) {
                channel.basicPublish(EXCHANGE_NAME, routingKey, null, Integer.toString(this.nodeId).getBytes());
                System.out.println("");
            }
        }, x -> {
        });
    }

    private void sendStatus() throws IOException {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                ValueType status = this.riddle.getWritable(i, j) ? ValueType.USER : ValueType.GIVEN;
                this.sendRequest(new GameUpdate(i, j, this.riddle.get(i, j), status).serialize());
            }
        }
    }

    @Override
    public int getCell(int x, int y) {
        return riddle.get(x, y);
    }

    @Override
    public boolean handleGameUpdate(GameUpdate update) {
        if (this.riddle.canSet(update.getX(), update.getY(), (byte) update.getValue())) {
            if (update.getType() == ValueType.GIVEN) {
                this.riddle.set(update.getX(), update.getY(), (byte) update.getValue());
                this.riddle.setWritable(update.getX(), update.getY(), false);
            } else {
                this.riddle.setWritable(update.getX(), update.getY(), true);
                this.riddle.set(update.getX(), update.getY(), (byte) update.getValue());
            }
            this.updates.add(update);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setCell(int x, int y, int value, ValueType type) {
        if (!this.canSetCell(x, y, value)) {
            return;
        }
        GameUpdate update = new GameUpdate(x, y, value, type);
        try {
            this.sendRequest(update.serialize());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean canSetCell(int x, int y, int value) {
        return this.riddle.canSet(x, y, (byte) value);
    }

    @Override
    public boolean checkForVictory() {
        return this.riddle.isValid();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Stream<Optional<GameUpdate>> getUpdates() {
        assert (!this.streamGenerated);
        this.streamGenerated = true;
        return Stream.concat(
                IntStream.range(0, 81)
                        .mapToObj(x -> new GameUpdate(x / 9, x % 9, this.riddle.get(x / 9, x % 9), ValueType.GIVEN))
                        .filter(x -> x.getValue() != 0)
                        .map(Optional::of)
                , Stream.generate(() -> {
                    try {
                        return Optional.of(this.updates.take());
                    } catch (InterruptedException ie) {
                        return Optional.empty();
                    }
                }));
    }

    @Override
    public void notifyClick(int x, int y) {
        try {
            channel.basicPublish(EXCHANGE_NAME, ChannelNames.getClicksRoutingKey(this.id), null, (x + " " + y).getBytes("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendRequest(String request) throws IOException {
        channel.basicPublish(EXCHANGE_NAME, ChannelNames.getBaseRoutingKey(this.id), null, request.getBytes(StandardCharsets.UTF_8)); // Invio la richiesta al topic specificato
        System.out.println(" [x] Sent '" + request + "' to topic '" + ChannelNames.getBaseRoutingKey(this.id) + "'");
    }

    public int getNodeId() {
        return nodeId;
    }

    @Override
    public Stream<Optional<Pair<Integer, Integer>>> getClicks() {
        assert (!this.clickStreamGenerated);
        this.clickStreamGenerated = true;
        return Stream.generate(() -> {
            try {
                return Optional.of(this.clicks.take());
            } catch (InterruptedException ie) {
                return Optional.empty();
            }
        });
    }
}
