package Client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Pedro Tanaka
 * @author Carolina Massae Kita
 * 
 */
public class Main {

    private static JPanel jpanel;
    private static Board board;

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
        JFrame window = new JFrame("Jogo Pebinha");
        
        window.setIconImage(new ImageIcon("rsc/logo.png").getImage());
        window.setSize(new Dimension(800, 600));
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        Main reversiGame = new Main();
        reversiGame.jpanel.setOpaque(true);
        //reversiGame.jpanel.setBackground(Color.blue);


        window.setContentPane(reversiGame.jpanel);

        window.setVisible(true);

    }

    public Main(){
        jpanel = new JPanel();
        board = new Board();

        Thread bd = new Thread(board);
        bd.start();

        jpanel.setLayout(null);
        jpanel.add(board);
        jpanel.setBackground(Color.blue);
        
        board.setBounds(0,0,700,500);
        board.setBackground(Color.blue);


    }
}
