package Client;

import java.awt.Color;
import java.awt.Dimension;
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

    private static String playerName;
    private static String serverIp;
    private static String serverPort;

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

        window.setIconImage(new ImageIcon("rsc/logo.png").getImage());

        window.setSize(new Dimension(540, 540));
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        Main reversiGame = new Main();
        reversiGame.jpanel.setOpaque(true);


        window.setContentPane(reversiGame.jpanel);

        window.setVisible(true);

    }

    public Main(){
        jpanel = new JPanel();
        board = new Board();

        /* //Network Issue
        playerName = JOptionPane.showInputDialog(null, "Please type the player name:");
        serverIp = JOptionPane.showInputDialog(null, "Please type the server ip:");
        serverPort = JOptionPane.showInputDialog(null, "Please type the server port:");
        */

        Thread bd = new Thread(board);
        bd.start();

        jpanel.setLayout(null);
        jpanel.add(board);
        jpanel.setBackground(Color.blue);
        
        board.setBounds(0,0,500,500);
        board.setBackground(Color.blue);


    }
}
