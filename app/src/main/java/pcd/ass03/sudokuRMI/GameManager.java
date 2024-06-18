package pcd.ass03.sudokuRMI;

import pcd.ass03.sudoku.GameUpdate;
import pcd.ass03.sudoku.Pair;

public interface GameManager {
    /**
     * Initialises a new game (server side)
     * @return the game ID
     */
    String createGame() throws java.rmi.RemoteException;
    /**
     * Register a listener to game events.
     * On join, the listener is given the current grid status
     */
    void joinGame(GameEventsListener listener, String gameId) throws java.rmi.RemoteException;
    void notifyClick(Pair<Integer, Integer> position) throws java.rmi.RemoteException;
    void notifyUpdate(GameUpdate update) throws java.rmi.RemoteException;
}
