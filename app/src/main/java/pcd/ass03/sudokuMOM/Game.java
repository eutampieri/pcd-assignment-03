package pcd.ass03.sudokuMOM;

import java.util.Optional;
import java.util.stream.Stream;

public interface Game {
    int getCell(int x, int y);
    boolean handleGameUpdate(GameUpdate update);
    boolean setCell(int x, int y, int value, ValueType type);
    boolean checkForVictory();
    String getId();
    Stream<Optional<GameUpdate>> getUpdats();
}