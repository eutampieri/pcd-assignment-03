package pcd.ass03.sudokuMOM;

public interface Game {
    int getCell(int x, int y);
    boolean handleGameUpdate(GameUpdate update);
    boolean setCell(int x, int y, int value, ValueType type);
    boolean checkForVictory();
    String getId();
}