package pcd.ass03.sudokuMOM;

import java.util.Optional;
import java.util.stream.Stream;

public interface Game {
    int getCell(int x, int y);
    boolean handleGameUpdate(GameUpdate update);
    void setCell(int x, int y, int value, ValueType type);
    boolean canSetCell(int x, int y, int value);
    boolean checkForVictory();
    String getId();
    Stream<Optional<GameUpdate>> getUpdates();
    void notifyClick(int x, int y);
    Stream<Optional<Pair<Integer, Integer>>> getClicks();
}