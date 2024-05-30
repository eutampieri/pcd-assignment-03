package pcd.ass03.sudokuMOM;

import com.rabbitmq.client.Channel;
import de.sfuhrm.sudoku.Riddle;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

public final class Sudoku implements Game {
    private static final String EXCHANGE_NAME = "sudoku_exchange";

    private final Riddle riddle;
    private final Channel channel;
    private final String id;
    private final BlockingQueue<GameUpdate> updates = new ArrayBlockingQueue<>(10);
    private boolean streamGenerated = false;
    private int numberOfNodes = 1;
    private int nodeId = 0;

    public Sudoku(Riddle riddle, Channel channel, String id) throws IOException {
        this.riddle = riddle;
        this.channel = channel;
        this.id = id;
        this.subscribeToUpdates();
        this.subscribeToJoins();
    }

    private void subscribeToUpdates() throws IOException {
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, this.id);
        this.channel.basicConsume(queueName, (consumerTag, x) -> {
            GameUpdate u = new GameUpdate(new String(x.getBody(), "UTF-8"));
            this.handleGameUpdate(u);
        }, x -> {
        });
    }

    private void subscribeToJoins() throws IOException {
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, this.id + "/joins");
        this.channel.basicConsume(queueName, (consumerTag, x) -> {
            int randomValue = Integer.parseInt(new String(x.getBody(), "UTF-8"));
            if (randomValue % this.numberOfNodes == this.nodeId) {
                // I am responsible for answering his request
                this.sendStatus();
                this.channel.basicPublish(
                        EXCHANGE_NAME,
                        this.id + "/" + randomValue,
                        null,
                        Integer.toString(this.numberOfNodes).getBytes("UTF-8")
                );
            }
            this.numberOfNodes += 1;
        }, x -> {
        });
    }

    private void sendStatus() {
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                // TODO get the cell value and if it's fillable. If some number is present, send it to the channel
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
    public boolean setCell(int x, int y, int value, ValueType type) {
        GameUpdate update = new GameUpdate(x, y, value, type);
        // TODO don't call handleGameUpdate. Rely on message queue
        if (this.handleGameUpdate(update)) {
            try {
                this.sendRequest(update.serialize());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        } else {
            return false;
        }
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
        return Stream.concat(null, Stream.generate(() -> {
            try {
                return Optional.of(this.updates.take());
            } catch (InterruptedException ie) {
                return Optional.empty();
            }
        }));
    }

    private void sendRequest(String request) throws IOException {
        channel.basicPublish(EXCHANGE_NAME, this.id, null, request.getBytes("UTF-8")); // Invio la richiesta al topic specificato
        System.out.println(" [x] Sent '" + request + "' to topic '" + this.id + "'");
    }

}
