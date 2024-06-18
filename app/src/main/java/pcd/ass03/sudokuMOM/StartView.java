package pcd.ass03.sudokuMOM;

import pcd.ass03.sudoku.Game;
import pcd.ass03.sudoku.SudokuGUI;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class StartView extends JFrame {


    public StartView() {
        setTitle("Start Game");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centra la finestra

        // Crea etichetta, campo di testo e pulsanti
        JLabel label = new JLabel("Enter Game ID:");
        JTextField gameIdField = new JTextField(15);
        JButton startButton = new JButton("Start Game");
        JButton joinButton = new JButton("Join Game");

        // Event handler per il pulsante Start Game
        startButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    Game sudoku = GameFactory.startGame();
                    System.out.println(sudoku.getId());
                    new SudokuGUI(sudoku);
                } catch (IOException | TimeoutException ex) {
                    throw new RuntimeException(ex);
                }
            });
        });

        // Event handler per il pulsante Join Game
        joinButton.addActionListener(e -> { //serve a associare un listener all'oggetto joinButton per gestire l'evento di azione
            String gameId = gameIdField.getText();
            SwingUtilities.invokeLater(() -> {
                try {
                    new SudokuGUI(GameFactory.joinGame(gameId));
                } catch (IOException | TimeoutException ex) {
                    throw new RuntimeException(ex);
                }
            });
        });

        this.setLayout(new BorderLayout());

        JPanel joinPanel = new JPanel();
        joinPanel.setLayout(new BorderLayout());
        joinPanel.add(label, BorderLayout.NORTH);
        joinPanel.add(gameIdField, BorderLayout.CENTER);
        joinPanel.add(joinButton, BorderLayout.SOUTH);

        this.add(joinPanel, BorderLayout.NORTH);
        this.add(startButton, BorderLayout.SOUTH);

    }


    public static void main(String[] args) {
        new StartView().setVisible(true);
    }

}
