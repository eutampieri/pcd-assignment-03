package pcd.ass03.sudoku;

import javax.swing.*;
import java.awt.*;

public abstract class BaseStartView extends JFrame {

    protected abstract void startGame();
    protected abstract void joinGame(String id);

    public BaseStartView() {
        setTitle("Start Game");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center window

        // Create label, text field and buttons
        JLabel label = new JLabel("Enter Game ID:");
        JTextField gameIdField = new JTextField(15);
        JButton startButton = new JButton("Start Game");
        JButton joinButton = new JButton("Join Game");

        // Start Game button event handler
        startButton.addActionListener(e -> {
            SwingUtilities.invokeLater(this::startGame);
        });

        // Join Game button event handler
        joinButton.addActionListener(e -> {
            String gameId = gameIdField.getText();
            SwingUtilities.invokeLater(() -> this.joinGame(gameId));
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
}
