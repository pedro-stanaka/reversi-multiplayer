/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.br.pedrotanaka;

import javax.swing.*;
import java.awt.Color;

/**
 *
 * @author Pedro Tanaka
 */
public class Main {
    private JPanel panel;


    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(
            new Runnable() {

                @Override
                public void run() {
                    show();
                }
            }
        );
    }

    private static void show()
    {
        JFrame window = new JFrame("Black X White (Server Side)");
        window.setIconImage(new ImageIcon(Main.class.getResource("/logo.png")).getImage());
        window.setSize(1200, 650);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setResizable(false);

        Main game = new Main();
        game.panel.setOpaque(true);

        window.setContentPane(game.panel);

        window.setVisible(true);

    }

    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    public Main() {
        panel = new JPanel();

        //portaDoServidor =  Integer.parseInt(JOptionPane.showInputDialog("Informe a porta do servidor"));
        String nomeDoJogador = JOptionPane.showInputDialog("Informe o nome do jogador");

        Board boardMain = new Board(12345, nomeDoJogador);
        Thread tb = new Thread(boardMain);
        tb.start();

        Chat chat = new Chat(12346, nomeDoJogador);
        Thread bp = new Thread(chat);
        bp.start();

        panel.setLayout(null);
        panel.setBackground(new Color(0, 215, 215));
        panel.add(boardMain);
        panel.add(chat);
        

        boardMain.setBounds(0, 40, 550, 550);
        boardMain.setBackground(new Color(0, 215, 215));

        chat.setBackground(new Color(0, 215, 215));
        chat.setBounds(560, 60, 520, 500);

    }
}
