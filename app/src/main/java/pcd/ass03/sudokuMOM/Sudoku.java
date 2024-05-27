package pcd.ass03.sudokuMOM;

import com.rabbitmq.client.Channel;
import de.sfuhrm.sudoku.Riddle;

import java.io.IOException;

public class Sudoku implements Game {
    private static final String EXCHANGE_NAME = "sudoku_exchange";

    private final Riddle riddle;
    private final Channel channel;
    private final String id;

    public Sudoku(Riddle riddle, Channel channel, String id) {
        this.riddle = riddle;
        this.channel = channel;
        this.id = id;
    }

    @Override
    public int getCell(int x, int y) {
        return riddle.get(x, y);
    }

    @Override
    public boolean handleGameUpdate(GameUpdate update) {
        if(this.riddle.canSet(update.getX(), update.getY(), (byte) update.getValue())) {
            this.riddle.set(update.getX(), update.getY(), (byte) update.getValue());
            if(update.getType() == ValueType.GIVEN) {
                this.riddle.setWritable(update.getX(), update.getY(), false);
            }
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean setCell(int x, int y, int value, ValueType type) {
        GameUpdate update = new GameUpdate(x, y, value, type);
        if(this.handleGameUpdate(update)) {
            try {
                this.sendRequest(update.toString());
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

    private void sendRequest(String request) throws IOException {
        channel.basicPublish(EXCHANGE_NAME, this.id, null, request.getBytes("UTF-8")); // Invio la richiesta al topic specificato
        System.out.println(" [x] Sent '" + request + "' to topic '" + this.id + "'");
    }

}
