package pcd.ass03.sudokuRMI;

import de.sfuhrm.sudoku.Riddle;
import pcd.ass03.sudoku.Game;
import pcd.ass03.sudoku.GameUpdate;
import pcd.ass03.sudoku.Pair;
import pcd.ass03.sudoku.ValueType;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class Sudoku implements Game, GameEventsListener {

    private final Riddle riddle;
    private final String id;
    private final BlockingQueue<GameUpdate> updates = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<Pair<Integer, Integer>> clicks = new ArrayBlockingQueue<>(10);
    private boolean streamGenerated = false;
    private boolean clickStreamGenerated = false;
    private final GameManager manager;

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
            this.manager.notifyUpdate(update);
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
        for(int i = 0; i < 9 && allCellsFilled; i++) {
            for(int j = 0; j < 9 && allCellsFilled; j++) {
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
            this.manager.notifyClick(new Pair<>(x, y));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    @Override
    public void notifyGameUpdate(GameUpdate update) throws RemoteException {
        try {
            this.updates.put(update);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notifyClick(Pair<Integer, Integer> cell) throws RemoteException {
        try {
            this.clicks.put(cell);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
