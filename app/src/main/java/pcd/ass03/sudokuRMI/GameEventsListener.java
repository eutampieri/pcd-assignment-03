package pcd.ass03.sudokuRMI;

import pcd.ass03.sudoku.GameUpdate;
import pcd.ass03.sudoku.Pair;

public interface GameEventsListener extends java.rmi.Remote{
    void notifyGameUpdate(GameUpdate update) throws java.rmi.RemoteException;
    void notifyClick(Pair<Integer, Integer> cell) throws java.rmi.RemoteException;
}
