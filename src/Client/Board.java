package Client;

import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.BufferOverflowException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * 
 * @author Pedro Tanaka
 * @author Carolina Massae Kita
 *
 */

public class Board extends JPanel implements Runnable {

//Atributes
    Cell[][] cells = new Cell[8][8];
    private int curPlayer;
    private int player1pieces;
    private int player2pieces;
    private static final int NUMCELLS = 8;
    private PrintStream output;
    private BufferedReader input;
    private Socket client;
    private String serverIp;
    private int serverPort;
    private JLabel connectionStatus;
    private String playerName;
    private String opponentName;

    public Board() {
        //initComponents();
        player1pieces = player2pieces = 0;
        drawBoard();
        MouseEvt mouseEvent = new MouseEvt();
        addMouseListener(mouseEvent);
        curPlayer = 1; // TESTE
    }

    private void drawBoard() {
        initCells();
        placeInitCells();
        createPolygons();
    }

    private void initCells() {
        for (int i = 0; i < NUMCELLS; i++) {
            for (int j = 0; j < NUMCELLS; j++) {
                this.cells[i][j] = new Cell();
                this.cells[i][j].setX(i);
                this.cells[i][j].setY(j);
                this.cells[i][j].setDraw(Boolean.TRUE);
            }
        }
    }

    private void placeInitCells() {
        cells[3][3].setPlayer(1);
        cells[3][4].setPlayer(2);
        cells[4][3].setPlayer(2);
        cells[4][4].setPlayer(1);
    }

    private void createPolygons() {
        int npoints = 4;    //Quantos pontos o poligono tem
        int width = 60;     //Largura
        int height = 60;    //Altura
        int initX = 20;     //Distância da Margem em X
        int initY = 20;     //Distância da Margem em Y
        Polygon p;          //Ponteiro de um poligono

        for (int i = 0; i < NUMCELLS; i++) {
            for (int j = 0; j < NUMCELLS; j++) {
                int xpoints[] = {i * width + initX, ((i * width) + (width / 2)) + initX, (i + 1) * width + initX, ((i * width) + (width / 2)) + initX};
                int ypoints[] = {((j * height) + (height / 2)) + initY, (j * height + initY), ((j * height) + (height / 2)) + initY, ((j + 1) * height + initY)};
                p = new Polygon(xpoints, ypoints, npoints);
                cells[i][j].setPolygon(p);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int i = 0; i < NUMCELLS; i++) {
            for (int j = 0; j < NUMCELLS; j++) {
                if (cells[i][j].getDraw() == true) {
                    {
                        g.setColor(cells[i][j].getColor());
                        g.fillPolygon(cells[i][j].getPolygon());
                        g.drawPolygon(cells[i][j].getPolygon());
                    }
                }
            }
        }
    }

    private void countPieces() {
        this.player1pieces = this.player2pieces = 0;
        for (int i = 0; i < NUMCELLS; i++) {
            for (int j = 0; j < NUMCELLS; j++) {
                if (cells[i][j].getPlayer() == 1) {
                    this.player1pieces++;
                } else {
                    this.player2pieces++;
                }
            }
        }
    }

    /**
     * @param x position X from the mouse event
     * @param y position Y from the mouse event
     */
    private void cellMovement(int x, int y) {
        for (int i = 0; i < NUMCELLS; i++) {
            for (int j = 0; j < NUMCELLS; j++) {
                if (cells[i][j].Contains(x, y) == true) {
                    if (cells[i][j].getPlayer() == 0) {
                        if (curPlayer == 1) {
                            verifyMovement(i, j);
                            cells[i][j].setPlayer(1);
                            curPlayer++;
                        } else {
                            verifyMovement(i, j);
                            cells[i][j].setPlayer(2);
                            curPlayer--;
                        }
                    } else {
                        System.out.println("Jogada Não Permitida");
                    }
                }
            }
        }
        countPieces();
    }

    private boolean verifyMovement(int posX, int posY) {
        boolean valueReturn = false;
        valueReturn = valueReturn || paintCapturedCells(posX, posY, 0, 1);
        System.out.println("1 - " + valueReturn);
        valueReturn = valueReturn || paintCapturedCells(posX, posY, -1, 1);
        System.out.println("2 - " + valueReturn);
        valueReturn = valueReturn || paintCapturedCells(posX, posY, -1, 0);
        System.out.println("3 - " + valueReturn);
        valueReturn = valueReturn || paintCapturedCells(posX, posY, -1, -1);
        System.out.println("4 - " + valueReturn);
        valueReturn = valueReturn || paintCapturedCells(posX, posY, 0, -1);
        System.out.println("5 - " + valueReturn);
        valueReturn = valueReturn || paintCapturedCells(posX, posY, 1, -1);
        System.out.println("6 - " + valueReturn);
        valueReturn = valueReturn || paintCapturedCells(posX, posY, 1, 0);
        System.out.println("7 - " + valueReturn);
        valueReturn = valueReturn || paintCapturedCells(posX, posY, 1, 1);
        if (valueReturn == false) {
            System.out.println("##### JOGADA INVALIDA ####");
        }
        return valueReturn;
    }

    private boolean paintCapturedCells(int positionX, int positionY, int dX, int dY) {
        int posX = positionX + dX;
        int posY = positionY + dY;
        boolean valueReturn = false;
        boolean keepGoing = true;
        if (curPlayer != cells[positionX][positionY].getPlayer()) {
            while (keepGoing) {
                if (dX == 1) {
                    if (posX == NUMCELLS) {
                        keepGoing = false;
                    }
                } else if (dX == -1) {
                    if (posX == 0) {
                        keepGoing = false;
                    }
                }
                if (dY == 1) {
                    if (posY == NUMCELLS) {
                        keepGoing = false;
                    }
                } else if (dY == -1) {
                    if (posY == 0) {
                        keepGoing = false;
                    }
                }

                if (keepGoing == true) {
                    posX += dX;
                    posY += dY;
                    if (curPlayer == cells[positionX][positionY].getPlayer()) {
                        valueReturn = true;
                        for (int i = positionX; i < posX; i = i + dX) {
                            for (int j = positionY; j < posY; j = j + dY) {
                                cells[i][j].setPlayer(curPlayer);
                            }
                        }
                    }
                }//Fim if
            }
        }
        return valueReturn;
    }

    public void run() {
        runClient();
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void runClient() {
        try {
            connectIntoServer();
            getInOutData();
            refreshScreenStatus();
        } catch (EOFException eofExcp) {
            showMessage("Connection Lost");
            JOptionPane.showMessageDialog(null, "Connection Error");
        } catch (IOException ioExcp) {
            ioExcp.printStackTrace();
        } finally {
            closeConnection();
        }

    }

    private void connectIntoServer() throws IOException {

        client = new Socket(InetAddress.getByName(this.serverIp), this.serverPort);
        showMessage("Succesfully connected!");
    }

    private void getInOutData() throws IOException {
        output = new PrintStream(client.getOutputStream());
        input = new BufferedReader(new InputStreamReader(client.getInputStream()));
    }

    private void refreshScreenStatus() throws IOException {
        boolean goingWell = true;
        do {
            try {
                String receivedData;
                receivedData = (String) input.readLine();
                String[] data = receivedData.split(":");

                this.opponentName = data[0];
                int x = Integer.parseInt(data[1]);
                int y = Integer.parseInt(data[2]);

                if (curPlayer == 2) {
                    cellMovement(x, y);
                    repaint();
                }
            } catch (IOException ioE) {
                goingWell = false;
                JOptionPane.showMessageDialog(null, "Unknown data type!");
            }
        } while (goingWell);
    }

    private void showMessage(final String messageToDisplay) {
        SwingUtilities.invokeLater(
                new Runnable() {

                    public void run()//atualiza a displayArea
                    {
                        connectionStatus.setText(messageToDisplay);
                    }
                });
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void closeConnection() {
        try {
            output.close();
            input.close();
            client.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public class MouseEvt extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (curPlayer == 1) {
            } else {
            }
            sendData(playerName + ":" + e.getX() + ":" + e.getY());
            cellMovement(e.getX(), e.getY());
            repaint();
        }
    }

    private void sendData(String message){
        try {
            output.println(message);
            output.flush();
        } catch (BufferOverflowException e) {
            JOptionPane.showMessageDialog(null, "An exception was found while sending data");
            
        }
    }
}
