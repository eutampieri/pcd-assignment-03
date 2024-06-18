package pcd.ass03.sudokuRMI;

import de.sfuhrm.sudoku.*;
import pcd.ass03.sudoku.Game;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public final class GameFactory {
    public static Game joinGame(String gameId) throws IOException, TimeoutException {
        Riddle riddle = new GameMatrixFactory().newRiddle(GameSchemas.SCHEMA_9X9);

        return null;
    }

    public static Game startGame() throws IOException, TimeoutException {
        GameMatrix matrix = Creator.createFull(GameSchemas.SCHEMA_9X9);
        Riddle riddle = Creator.createRiddle(matrix);

        String gameId = UUID.randomUUID().toString();

        return null;
    }
}