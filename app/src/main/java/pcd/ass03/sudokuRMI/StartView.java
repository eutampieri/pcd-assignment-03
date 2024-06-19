package pcd.ass03.sudokuRMI;

import pcd.ass03.sudoku.BaseStartView;
import pcd.ass03.sudoku.Game;
import pcd.ass03.sudoku.SudokuGUI;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.concurrent.TimeoutException;

public class StartView extends BaseStartView {

    @Override
    protected void startGame() {
        try {
            Game sudoku = GameFactory.startGame();
            System.out.println(sudoku.getId());
            new SudokuGUI(sudoku);
        } catch (IOException | TimeoutException | NotBoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void joinGame(String id) {
        try {
            new SudokuGUI(GameFactory.joinGame(id));
        } catch (IOException | TimeoutException ex) {
            throw new RuntimeException(ex);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new StartView().setVisible(true);
    }
}