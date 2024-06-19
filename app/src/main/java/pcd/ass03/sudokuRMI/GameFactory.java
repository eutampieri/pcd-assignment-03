package pcd.ass03.sudokuRMI;

import de.sfuhrm.sudoku.*;
import pcd.ass03.sudoku.Game;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeoutException;

public final class GameFactory {
    public static Game joinGame(String gameId) throws IOException, TimeoutException, NotBoundException {
        GameManager manager = getGameManager();
        Riddle riddle = new GameMatrixFactory().newRiddle(GameSchemas.SCHEMA_9X9);
        Sudoku sudoku = new Sudoku(riddle, manager, gameId);
        manager.joinGame(sudoku.getListener(), gameId);
        return sudoku;
    }

    public static Game startGame() throws IOException, TimeoutException, NotBoundException {
        String id = getGameManager().createGame();
        return joinGame(id);
    }

    private static GameManager getGameManager() throws RemoteException, NotBoundException {

        Registry registry = LocateRegistry.getRegistry("localhost");
        GameManager gameManager =  (GameManager) registry.lookup("gameManager");
        return gameManager;
    }
}