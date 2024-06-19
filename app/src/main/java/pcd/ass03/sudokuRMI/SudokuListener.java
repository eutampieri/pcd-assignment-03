package pcd.ass03.sudokuRMI;

import pcd.ass03.sudoku.GameUpdate;
import pcd.ass03.sudoku.Pair;
import pcd.ass03.sudoku.ValueType;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SudokuListener implements GameEventsListener, Serializable {
    private final BlockingQueue<GameUpdate> updates = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<Pair<Integer, Integer>> clicks = new ArrayBlockingQueue<>(10);
    private boolean streamGenerated = false;
    private boolean clickStreamGenerated = false;

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

    public Stream<Optional<GameUpdate>> getUpdates() {
        assert (!this.streamGenerated);
        this.streamGenerated = true;
        return Stream.generate(() -> {
            try {
                return Optional.of(this.updates.take());
            } catch (InterruptedException ie) {
                return Optional.empty();
            }
        });
    }

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
