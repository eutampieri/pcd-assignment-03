package pcd.ass03.sudokuRMI;

import de.sfuhrm.sudoku.*;
import pcd.ass03.sudoku.Game;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public final class GameFactory {
    public static Game joinGame(String gameId) throws IOException, TimeoutException {
        GameManager manager = getGameManager();
        GameMatrix matrix = Creator.createFull(GameSchemas.SCHEMA_9X9);
        Riddle riddle = Creator.createRiddle(matrix);
        Sudoku sudoku = new Sudoku(riddle, manager, gameId);
        manager.joinGame(sudoku, gameId);
        return sudoku;
    }

    public static Game startGame() throws IOException, TimeoutException {
        String id = getGameManager().createGame();
        return joinGame(id);
    }

    private static GameManager getGameManager() {
        return null; //TODO lookup
    }
}