package Client;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.plaf.ColorUIResource;

/**
 *
 * @author Pedro Tanaka
 * @author Carolina Massae Kita
 * 
 */
public class Main {

    private static JPanel jpanel;
    private static Board board;
    private static Chat chat;

    private static String playerName;
    private static String serverIp;
    private static int serverPort;

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

        window.setSize(new Dimension(1200, 650));
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        Main reversiGame = new Main();
        reversiGame.jpanel.setOpaque(true);

        window.setContentPane(reversiGame.jpanel);

        window.setVisible(true);

    }

    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    public Main(){
        jpanel = new JPanel();

        serverIp = JOptionPane.showInputDialog(null, "Please type the server ip:");
        serverPort = Integer.parseInt(JOptionPane.showInputDialog(null, "Please type the server port:"));
        playerName = JOptionPane.showInputDialog(null, "Please type the player name:");

        board = new Board( serverIp, serverPort, playerName );

        Thread bd = new Thread(board);
        bd.start();

        chat = new  Chat(serverIp, ++serverPort, playerName);
        Thread gc = new Thread(chat);
        gc.start();

        jpanel.setLayout(null);

        jpanel.add(board);
        jpanel.add(chat);
        jpanel.setBackground(new Color(255, 0, 1));

        board.setBounds(0,40,550,550);
        board.setBackground(new Color(255, 0, 1));

        chat.setBackground(Color.red);
        chat.setBounds(560, 60, 520, 500);

    }
}
