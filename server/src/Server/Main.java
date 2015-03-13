/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Server;

import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author Pedro Tanaka
 */
public class Main {
    private JPanel panel;
    private Board boardMain;
    private Chat chat;

    private int portaDoServidor;
    private final String nomeDoJogador;



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
        window.setIconImage(new ImageIcon("rsc/logo.png").getImage());
        window.setSize(1200, 650);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        nomeDoJogador = JOptionPane.showInputDialog("Informe o nome do jogador");

        boardMain = new Board(12345, nomeDoJogador);
        Thread tb = new Thread(boardMain);
        tb.start();

        chat = new Chat(12346, nomeDoJogador);
        Thread bp = new Thread(chat);
        bp.start();

        panel.setLayout(null);
        panel.setBackground(new Color(0, 215, 215));
        panel.add(boardMain);
        panel.add(chat);
        

        boardMain.setBounds(0,40,550,550);
        boardMain.setBackground(new Color(0, 215, 215));

        chat.setBackground(new Color(0, 215, 215));
        chat.setBounds(560, 60, 520, 500);

    }
}
