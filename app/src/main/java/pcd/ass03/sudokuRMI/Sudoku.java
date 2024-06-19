package pcd.ass03.sudokuRMI;

import de.sfuhrm.sudoku.Riddle;
import pcd.ass03.sudoku.Game;
import pcd.ass03.sudoku.GameUpdate;
import pcd.ass03.sudoku.Pair;
import pcd.ass03.sudoku.ValueType;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class Sudoku implements Game {

    private final Riddle riddle;
    private final String id;
    private final GameManager manager;
    private final SudokuListener listener = new SudokuListener();

    public Sudoku(Riddle riddle, GameManager manager, String id) throws IOException {
        this.riddle = riddle;
        this.id = id;
        System.err.println(riddle.toString());
        this.manager = manager;
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
            this.manager.notifyUpdate(this.id, update);
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
        boolean allCellsFilled = true;
        for (int i = 0; i < 9 && allCellsFilled; i++) {
            for (int j = 0; j < 9 && allCellsFilled; j++) {
                allCellsFilled = this.riddle.get(i, j) != 0;
            }
        }
        return this.riddle.isValid() && allCellsFilled;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Stream<Optional<GameUpdate>> getUpdates() {
        return Stream.concat(
                IntStream.range(0, 81)
                        .mapToObj(x -> new GameUpdate(x / 9, x % 9, this.riddle.get(x / 9, x % 9), ValueType.GIVEN))
                        .filter(x -> x.getValue() != 0)
                        .map(Optional::of)
                , listener.getUpdates());
    }

    @Override
    public void notifyClick(int x, int y) {
        try {
            this.manager.notifyClick(this.id, new Pair<>(x, y));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<Optional<Pair<Integer, Integer>>> getClicks() {
        return listener.getClicks();
    }

    public GameEventsListener getListener() {
        return this.listener;
    }
}
