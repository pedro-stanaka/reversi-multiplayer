package com.br.pedrotanaka;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.*;

/**
 *
 * @author Pedro Tanaka
 * @author Carolina Massae Kita
 * 
 */
public class Main {

    private static JPanel jpanel;

    public static void main(String args[]) {
        javax.swing.SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        show();
                    }
                });
    }

    private static void show() {
        JFrame window = new JFrame("Black X White (Client Side)");

        window.setIconImage(new ImageIcon(Main.class.getResource("/logo.png")).getImage());

        window.setSize(new Dimension(1200, 650));
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setResizable(false);

        Main game = new Main();
        jpanel.setOpaque(true);

        window.setContentPane(jpanel);

        window.setVisible(true);

    }

    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    public Main(){
        jpanel = new JPanel();

        String serverIp = JOptionPane.showInputDialog(null, "Please type the server ip:");
        int serverPort = 12345;
        String playerName = JOptionPane.showInputDialog(null, "Please type the player name:");

        Board board = new Board(serverIp, serverPort, playerName);

        Thread bd = new Thread(board);
        bd.start();

        Chat chat = new Chat(serverIp, ++serverPort, playerName);
        Thread gc = new Thread(chat);
        gc.start();

        jpanel.setLayout(null);

        jpanel.add(board);
        jpanel.add(chat);
        jpanel.setBackground(new Color(255, 0, 1));

        board.setBounds(0, 40, 550, 550);
        board.setBackground(new Color(255, 0, 1));

        chat.setBackground(Color.red);
        chat.setBounds(560, 60, 520, 500);

    }
}
