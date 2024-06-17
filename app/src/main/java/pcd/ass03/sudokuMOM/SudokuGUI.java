package pcd.ass03.sudokuMOM;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import com.google.gson.JsonArray;

public class SudokuGUI extends JFrame {
    private final Game sudoku;
    private final JPanel mainPanel;
    private int numTopic = 0;
    private final JButton[][] gridButtons = new JButton[9][9];

    public SudokuGUI(Game sudoku) {
        super("Sudoku Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        this.sudoku = sudoku;

        /*JButton newGridButton = new JButton("New Grid");
        newGridButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        add(newGridButton, BorderLayout.SOUTH);*/

        setVisible(true);

        // TODO Use factory to create a game
        new Thread(new Runnable() {
            public void run() {
                sudoku.getUpdates().filter(Optional::isPresent).map(Optional::get).forEach(e -> updateGrid(e));
            }
        }).start();
        this.renderGrid();
    }

    /**
     * This function renders an empty grid
     */
    public void renderGrid() {
        JPanel gridPanel = new JPanel(new GridLayout(9, 9));

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                gridButtons[i][j] = new JButton("");
                int finalI = i;
                int finalJ = j;
                gridButtons[i][j].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        sudoku.notifyClick(finalI, finalJ);
                        try {
                            byte value = Byte.parseByte(JOptionPane.showInputDialog("Enter value:"));
                            sudoku.setCell(finalI, finalJ, value, ValueType.USER);
                        } catch (Exception ex) {
                            sudoku.setCell(finalI, finalJ, 0, ValueType.USER);
                        }
                    }
                });
                gridPanel.add(gridButtons[i][j]);
            }
        }

        JPanel panelWrapper = new JPanel(new BorderLayout());
        panelWrapper.setBorder(BorderFactory.createTitledBorder("Grid " + this.sudoku.getId()));
        panelWrapper.add(gridPanel, BorderLayout.CENTER);
        mainPanel.add(panelWrapper);
        mainPanel.revalidate();
    }

    public void updateGrid(GameUpdate update) {
        int r = update.getX();
        int c = update.getY();
        JButton button = this.gridButtons[r][c];
        button.setEnabled(update.getType() != ValueType.GIVEN);
        button.setText(update.getValue() == 0 ? "" : Integer.toString(update.getValue()));
        button.revalidate();
        button.repaint();
    }

}

