package pcd.ass03.sudokuRMI;

import pcd.ass03.sudoku.GameUpdate;
import pcd.ass03.sudoku.Pair;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface GameManager {
    /**
     * Initialises a new game (server side)
     * @return the game ID
     */
    String createGame() throws IOException, TimeoutException;
    /**
     * Register a listener to game events.
     * On join, the listener is given the current grid status
     */
    void joinGame(GameEventsListener listener, String gameId) throws java.rmi.RemoteException;
    void notifyClick(String gameId, Pair<Integer, Integer> position) throws java.rmi.RemoteException;
    void notifyUpdate(String gameId, GameUpdate update) throws java.rmi.RemoteException;
}
