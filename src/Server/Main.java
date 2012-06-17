/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Server;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author Pedro Tanaka
 */
public class Main {
    private JPanel panel;
    private Board board;
    private Chat chat;

    private int portaDoServidor;
    private final String nomeDoJogador;



    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(
            new Runnable() {

                @Override
                public void run() {
                    exibir();
                }
            }
        );
    }

    private static void exibir()
    {
        JFrame janela = new JFrame("Servidor");
        janela.setSize(650, 650);
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setResizable(false);



        Main hexagono = new Main();
        hexagono.panel.setOpaque(true);

        janela.setContentPane(hexagono.panel);

        janela.setVisible(true);

    }

    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    public Main() {
        panel = new JPanel();

        portaDoServidor =  Integer.parseInt(JOptionPane.showInputDialog("Informe a porta do servidor"));
        nomeDoJogador = JOptionPane.showInputDialog("Informe o nome do jogador");

//        boardMain = new Board(portaDoServidor, nomeDoJogador);
//        Thread tb = new Thread(boardboard);
//        tb.start();

        chat = new Chat(++portaDoServidor, nomeDoJogador);
        Thread bp = new Thread(chat);
        bp.start();

        panel.setLayout(null);
//        panel.add(boardboard);
        panel.add(chat);

//        boardboard.setBounds(0, 0, 700, 400);
        chat.setBounds(50, 401, 700, 500);

    }
}
