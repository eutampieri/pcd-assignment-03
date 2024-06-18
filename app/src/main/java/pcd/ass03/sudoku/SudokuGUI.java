package pcd.ass03.sudoku;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Optional;

public class SudokuGUI extends JFrame {
    private final Game sudoku;
    private final JPanel mainPanel;
    private final JButton[][] gridButtons = new JButton[9][9];

    public SudokuGUI(Game sudoku) {
        super("Sudoku Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        this.sudoku = sudoku;

        setVisible(true);

        new Thread(() -> sudoku.getUpdates()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(e -> updateGrid(e))).start();
        new Thread(() -> sudoku.getClicks()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(e -> this.gridButtons[e.getLeft()][e.getRight()].setBackground(Color.YELLOW))).start();
        this.renderGrid();
    }

    /**
     * This function renders an empty grid
     */
    public synchronized void renderGrid() {
        JPanel mainGridPanel = new JPanel(new GridLayout(3, 3));

        for(int a = 0; a < 3; a++) {
            for(int b = 0; b < 3; b++) {
                JPanel subGridPanel = new JPanel(new GridLayout(3, 3));
                subGridPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        gridButtons[3 * a + i][3 * b + j] = new JButton("");
                        gridButtons[3 * a + i][3 * b + j].setToolTipText(i + ", " + j);
                        int finalI = 3 * a + i;
                        int finalJ = 3 * b + j;
                        gridButtons[3 * a + i][3 * b + j].addActionListener(new ActionListener() {
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
                        subGridPanel.add(gridButtons[3 * a + i][3 * b + j]);
                    }
                    mainGridPanel.add(subGridPanel);
                }
            }
        }

        JPanel panelWrapper = new JPanel(new BorderLayout());
        panelWrapper.setBorder(BorderFactory.createTitledBorder("Grid " + this.sudoku.getId()));
        panelWrapper.add(mainGridPanel, BorderLayout.CENTER);
        mainPanel.add(panelWrapper);
        mainPanel.revalidate();
    }

    public synchronized void updateGrid(GameUpdate update) {
        int r = update.getX();
        int c = update.getY();
        JButton button = this.gridButtons[r][c];
        button.setEnabled(update.getType() != ValueType.GIVEN);
        button.setText(update.getValue() == 0 ? "" : Integer.toString(update.getValue()));
        button.setBackground(Color.WHITE);
        button.revalidate();
        button.repaint();
        if(sudoku.checkForVictory()) {
            JOptionPane.showMessageDialog(null, "You won!");
        }
    }

}