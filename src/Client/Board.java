package Client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.JLabel;
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

    private void countPieces(){
        this.player1pieces = this.player2pieces = 0;
        for (int i = 0; i < NUMCELLS; i++) {
            for (int j = 0; j < NUMCELLS; j++) {
                if( cells[i][j].getPlayer() == 1)
                    this.player1pieces++;
                else
                    this.player2pieces++;
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
                            cells[i][j].setPlayer(1);
                            curPlayer++;
                        } else {
                            cells[i][j].setPlayer(2);
                            curPlayer--;
                        }
                    }
                    else
                    {
                        System.out.println("Jogada Não Permitida");
                    }
                }
            }
        }
        countPieces();
    }
    
    private boolean verifyMovement(int posX, int posY){
        boolean valueReturn = false;
        valueReturn = valueReturn || paintCapturedCells(posX, posY, 0, 1);
        valueReturn = valueReturn || paintCapturedCells(posX, posY, -1, 1);
        valueReturn = valueReturn || paintCapturedCells(posX, posY, -1, 0);
        valueReturn = valueReturn || paintCapturedCells(posX, posY, -1, -1);
        valueReturn = valueReturn || paintCapturedCells(posX, posY, 0, -1);
        valueReturn = valueReturn || paintCapturedCells(posX, posY, 1, -1);
        valueReturn = valueReturn || paintCapturedCells(posX, posY, 1, 0);
        valueReturn = valueReturn || paintCapturedCells(posX, posY, 1, 1);
        
        return valueReturn;
    }
    
    private boolean paintCapturedCells(int positionX, int positionY, int dX, int dY){
        int posX = positionX + dX;
        int posY = positionY + dY;
        boolean valueReturn = false;
        if(curPlayer != cells[posX][posY].getPlayer()){
            while(1 == 1){
                if(dX == 1){
                    if(posX == NUMCELLS)
                        break;
                }
                else if(dX == -1){
                    if(posX == 0)
                        break;
                }
                if(dY == 1){
                    if(posY == NUMCELLS)
                        break;
                }
                else if(dY == -1){
                    if(posY == 0)
                        break;
                }
                posX += dX;
                posY += dY;
                if(curPlayer == cells[posX][posY].getPlayer())
                    valueReturn = true;
            }
        }
        return valueReturn;
    }

    public void run() {
        for (int i = 0; i < NUMCELLS; i++) {
            for (int j = 0; j < NUMCELLS; j++) {
                this.cells[i][j].setDraw(Boolean.TRUE);
            }
        }
    }

    private void runClient() {
        try {

        } catch (EOFException eofExcp) {
        }
    }

    private void connectIntoServer() throws IOException{

        client = new Socket(InetAddress.getByName(this.serverIp), this.serverPort);
        showMessage("Succesfully connected!");
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

    public class MouseEvt extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (curPlayer == 1) {
            } else {
            }
            String movements = "Nome:" + e.getX() + ":" + e.getY();
            cellMovement(e.getX(), e.getY());
            repaint();
        }
    }
}
