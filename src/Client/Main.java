/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Pedro Tanaka
 */
public class Main {

    private static JPanel jpanel;
    private static Board board;

    public static void main(String args[]) {
        javax.swing.SwingUtilities.invokeLater(
                new Runnable() {

                    @Override
                    public void run() {
                        exibir();
                    }
                });


    }

    private static void exibir() {
        JFrame window = new JFrame("Jogo Pebinha");
        window.setSize(new Dimension(800, 800));
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        Main reversiGame = new Main();
        Main.jpanel.setOpaque(true);

        window.setContentPane(Main.jpanel);

        window.setVisible(true);


    }

    public Main(){
        jpanel = new JPanel();
        board = new Board();

        Thread bd = new Thread(board);
        bd.start();

        jpanel.add(board);

        board.setBounds(0,0,700,600);

    }
}
