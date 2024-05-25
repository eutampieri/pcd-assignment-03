package pcd.ass03.sudokuMOM;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.google.gson.JsonArray;

public class SudokuGUI extends JFrame {
    private SudokuPlayer player;
    private JPanel mainPanel;
    private int numTopic=0;

    public SudokuGUI() {
        super("Sudoku Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        add(new JScrollPane(mainPanel), BorderLayout.CENTER);

        JButton newGridButton = new JButton("New Grid");
        newGridButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String topic = String.valueOf(numTopic);
                    player.newGrid(topic);
                    numTopic++;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        add(newGridButton, BorderLayout.SOUTH);

        try {
            // SAREBBERO PIU DI 1
            player = new SudokuPlayer(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setVisible(true);
    }

    public void updateGrid(String topic, JsonArray board) {
        JPanel gridPanel = new JPanel(new GridLayout(9, 9));
        JButton[][] gridButtons = new JButton[9][9];

        for (int i = 0; i < 9; i++) {
            JsonArray row = board.get(i).getAsJsonArray();
            for (int j = 0; j < 9; j++) {
                final int r = i;
                final int c = j;
                gridButtons[i][j] = new JButton(String.valueOf(row.get(j).getAsByte()));
                gridButtons[i][j].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JButton btn = (JButton) e.getSource();
                        byte value = Byte.parseByte(JOptionPane.showInputDialog("Enter value:"));
                        try {
                            player.setGrid(r, c, value, topic);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                gridPanel.add(gridButtons[i][j]);
            }
        }

        JPanel panelWrapper = new JPanel(new BorderLayout());
        panelWrapper.setBorder(BorderFactory.createTitledBorder("Grid " + topic));
        panelWrapper.add(gridPanel, BorderLayout.CENTER);
        mainPanel.add(panelWrapper);
        mainPanel.revalidate();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new SudokuGUI();
            }
        });
    }
}

