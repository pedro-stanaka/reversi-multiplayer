package com.br.pedrotanaka;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.BufferOverflowException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author Pedro Tanaka
 * @author Carolina Massae Kita
 * @version 0.2a
 */
public class Board extends JPanel implements Runnable {

//Atributes
    Cell[][] cells = new Cell[8][8];
    private int curPlayer;
    private int player1pieces;
    private int player2pieces;
    private static final int NUMCELLS = 8;
    private String playerName;
    private String opponentName;
    private JLabel score;
//Network Atributes
    private PrintStream output;
    private BufferedReader input;
    private Socket client;
    private String serverIp;
    private int serverPort;
    private JLabel connectionStatus;

    /**
     *
     * @param serverIp IP adress of the server
     * @param serverPort Port number of the server
     * @param playerName Name of the player playing as server.
     */
    public Board(String serverIp, int serverPort, String playerName) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.playerName = playerName;

        player1pieces = player2pieces = 0;
        drawBoard();
        MouseEvt mouseEvent = new MouseEvt();
        addMouseListener(mouseEvent);
        curPlayer = 2;

        connectionStatus = new JLabel();
        connectionStatus.setBounds(0, 0, 100, 50);
        connectionStatus.setForeground(Color.green);
        connectionStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createBevelBorder(0), BorderFactory.createEmptyBorder(1, 2, 2, 1)));
        add(connectionStatus);

        score = new JLabel();
        score.setBounds(0, 200, 100, 100);
        score.setForeground(Color.WHITE);
        score.setBorder(BorderFactory.createBevelBorder(0));
        add(score);


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
        cells[3][3].setPlayer(2);
        cells[3][4].setPlayer(1);
        cells[4][3].setPlayer(1);
        cells[4][4].setPlayer(2);
    }

    private void createPolygons() {
        int npoints = 4;    // Number of points that the polygon has
        int width = 60;     // Width of the cell
        int height = 60;    // Height of the cell
        int initX = 30;     //Distância da Margem em X
        int initY = 50;     //Distância da Margem em Y
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

        g.setColor(Color.red);
        g.drawLine(0, 35, 1200, 35);

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
                } else if (cells[i][j].getPlayer() == 2) {
                    this.player2pieces++;
                }
            }
        }
        if (player1pieces + player2pieces == 64) {
            showWinner();
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
                        showMessage("                              ");

                        if (curPlayer == 1) {
                            if (verifyHasMovements()) {
                                if (verifyMovement(i, j)) {
                                    curPlayer++;
                                    showMessage("It's " + opponentName + " turn!");
                                }
                            } else {
                                curPlayer++;
                                showMessage("You don't have a valid movement! It's " + opponentName + " turn!");
                            }

                        } else if (curPlayer == 2) {
                            if (verifyHasMovements()) {
                                if (verifyMovement(i, j)) {
                                    curPlayer--;
                                    showMessage("<html>It's your turn!<html/>");
                                }
                            } else {
                                curPlayer--;
                                showMessage("<html>It's your turn!<html/>");
                            }

                        }
                    } else {
                        showMessage("  ----- Forbidden movement. ------  ");
                    }
                }
            }
        }
        countPieces();
        showScore();
        repaint();
    }

    private void showScore() {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        String gameScore;
                        gameScore = "<html>You have: " + player1pieces + " pieces<br/>";
                        gameScore += opponentName +" has: " + player2pieces + " pieces</html>";
                        score.setText(gameScore);
                    }
                });
    }

    /**
     * Check if the current player can move or not from a special localization
     * in the matrix of cells. The origin position is [posX][posY].
     *
     * @param posX the position X in the board
     * @param posY the position Y in the board
     * @param dX the horizontal direction that is suposed to check
     * @param dY the vertical direction that is suposed to check
     *
     * @return false if the curPlayer can move now, true otherwise.
     *
     */
    private boolean verifyHasMovements() {
        boolean valueReturn = false;
        int num = 2;
        for (int posX = 0; posX < NUMCELLS; posX++) {
            for (int posY = 0; posY < NUMCELLS; posY++) {
                if (cells[posX][posY].getPlayer() == 0) {
                    valueReturn = paintCapturedCellsPlus0Plus1(posX, posY, num) || valueReturn;
                    valueReturn = paintCapturedCellsMinus1Plus1(posX, posY, num) || valueReturn;
                    valueReturn = paintCapturedCellsMinus1Plus0(posX, posY, num) || valueReturn;
                    valueReturn = paintCapturedCellsMinus1Minus1(posX, posY, num) || valueReturn;
                    valueReturn = paintCapturedCellsPlus0Minus1(posX, posY, num) || valueReturn;
                    valueReturn = paintCapturedCellsPlus1Minus1(posX, posY, num) || valueReturn;
                    valueReturn = paintCapturedCellsPlus1Plus0(posX, posY, num) || valueReturn;
                    valueReturn = paintCapturedCellsPlus1Plus1(posX, posY, num) || valueReturn;
                }
            }
        }
        return valueReturn;
    }

    private boolean verifyMovement(int posX, int posY) {
        boolean valueReturn = false;
        int num = 1; //Fala que a função é verifyMovement

        valueReturn = paintCapturedCellsPlus0Plus1(posX, posY, num) || valueReturn;
        valueReturn = paintCapturedCellsMinus1Plus1(posX, posY, num) || valueReturn;
        valueReturn = paintCapturedCellsMinus1Plus0(posX, posY, num) || valueReturn;
        valueReturn = paintCapturedCellsMinus1Minus1(posX, posY, num) || valueReturn;
        valueReturn = paintCapturedCellsPlus0Minus1(posX, posY, num) || valueReturn;
        valueReturn = paintCapturedCellsPlus1Minus1(posX, posY, num) || valueReturn;
        valueReturn = paintCapturedCellsPlus1Plus0(posX, posY, num) || valueReturn;
        valueReturn = paintCapturedCellsPlus1Plus1(posX, posY, num) || valueReturn;
        if (curPlayer == 2 && !valueReturn) {
            System.out.println("Client Movement Invalid");
        } // Debug
        if (valueReturn == false) {
            showMessage("##### INVALID MOVEMENT ####");
        }
        return valueReturn;
    }

    private boolean paintCapturedCellsPlus0Plus1(int positionX, int positionY, int num) {
        int dY = 1;
        int posY = positionY + dY;
        boolean valueReturn = false;
        if (posY < NUMCELLS && curPlayer != cells[positionX][posY].getPlayer()) {
            while (valueReturn == false && posY < (NUMCELLS - 1) && cells[positionX][posY].getPlayer() != 0) {
                posY += dY;
                if (curPlayer == cells[positionX][posY].getPlayer()) {
                    valueReturn = true;
                    if (num == 1) {
                        for (int j = positionY; j < posY; j = j + dY) {
                            cells[positionX][j].setPlayer(curPlayer);
                        }
                    }
                }//enf if
            }
        }//enf if
        return valueReturn;
    }

    private boolean paintCapturedCellsMinus1Plus1(int positionX, int positionY, int num) {
        int dX = -1;
        int dY = 1;
        int posX = positionX + dX;
        int posY = positionY + dY;
        boolean valueReturn = false;
        if (posX >= 0 && posY < NUMCELLS && curPlayer != cells[posX][posY].getPlayer()) {
            while (valueReturn == false && posX > 0 && posY < (NUMCELLS - 1) && cells[posX][posY].getPlayer() != 0) {
                posX += dX;
                posY += dY;
                if (curPlayer == cells[posX][posY].getPlayer()) {
                    valueReturn = true;
                    if (num == 1) {
                        for (int i = 0; i <= mod(positionX, posX); i++) {
                            cells[positionX + dX * i][positionY + dY * i].setPlayer(curPlayer);
                        }
                    }
                }//enf if
            }
        }//enf if
        return valueReturn;
    }

    private boolean paintCapturedCellsMinus1Plus0(int positionX, int positionY, int num) {
        int dX = -1;
        int posX = positionX + dX;
        boolean valueReturn = false;
        if (posX >= 0 && curPlayer != cells[posX][positionY].getPlayer()) {
            while (valueReturn == false && posX > 0 && cells[posX][positionY].getPlayer() != 0) {
                posX += dX;
                if (curPlayer == cells[posX][positionY].getPlayer()) {
                    valueReturn = true;
                    if (num == 1) {
                        for (int i = positionX; i > posX; i = i + dX) {
                            cells[i][positionY].setPlayer(curPlayer);
                        }
                    }
                }//enf if
            }
        }//enf if
        return valueReturn;
    }

    private boolean paintCapturedCellsMinus1Minus1(int positionX, int positionY, int num) {
        int dX = -1;
        int dY = -1;
        int posX = positionX + dX;
        int posY = positionY + dY;
        boolean valueReturn = false;
        if (posX >= 0 && posY >= 0 && curPlayer != cells[posX][posY].getPlayer()) {
            while (valueReturn == false && posX > 0 && posY > 0 && cells[posX][posY].getPlayer() != 0) {
                posX += dX;
                posY += dY;
                if (curPlayer == cells[posX][posY].getPlayer()) {
                    valueReturn = true;
                    if (num == 1) {
                        for (int i = 0; i <= mod(positionX, posX); i++) {
                            cells[positionX + dX * i][positionY + dY * i].setPlayer(curPlayer);
                        }
                    }
                }//enf if
            }
        }//enf if
        return valueReturn;
    }

    private boolean paintCapturedCellsPlus0Minus1(int positionX, int positionY, int num) {
        int dY = -1;
        int posY = positionY + dY;
        boolean valueReturn = false;
        if (posY >= 0 && curPlayer != cells[positionX][posY].getPlayer()) {
            while (valueReturn == false && posY > 0 && cells[positionX][posY].getPlayer() != 0) {
                posY += dY;
                if (curPlayer == cells[positionX][posY].getPlayer()) {
                    valueReturn = true;
                    if (num == 1) {
                        for (int j = positionY; j > posY; j = j + dY) {
                            cells[positionX][j].setPlayer(curPlayer);
                        }
                    }
                }//enf if
            }
        }//enf if
        return valueReturn;
    }

    private boolean paintCapturedCellsPlus1Minus1(int positionX, int positionY, int num) {
        int dX = 1;
        int dY = -1;
        int posX = positionX + dX;
        int posY = positionY + dY;
        boolean valueReturn = false;
        if (posX < NUMCELLS && posY >= 0 && curPlayer != cells[posX][posY].getPlayer()) {
            while (valueReturn == false && posX < (NUMCELLS - 1) && posY > 0 && cells[posX][posY].getPlayer() != 0) {
                posX += dX;
                posY += dY;
                if (curPlayer == cells[posX][posY].getPlayer()) {
                    valueReturn = true;
                    if (num == 1) {
                        for (int i = 0; i <= mod(positionX, posX); i++) {
                            cells[positionX + dX * i][positionY + dY * i].setPlayer(curPlayer);
                        }
                    }
                }//enf if
            }
        }//enf if
        return valueReturn;
    }

    private boolean paintCapturedCellsPlus1Plus0(int positionX, int positionY, int num) {
        int dX = 1;
        int posX = positionX + dX;
        boolean valueReturn = false;
        if (posX < NUMCELLS && curPlayer != cells[posX][positionY].getPlayer()) {
            while (valueReturn == false && posX < (NUMCELLS - 1) && cells[posX][positionY].getPlayer() != 0) {
                posX += dX;
                if (curPlayer == cells[posX][positionY].getPlayer()) {
                    valueReturn = true;
                    if (num == 1) {
                        for (int i = positionX; i < posX; i = i + dX) {
                            cells[i][positionY].setPlayer(curPlayer);
                        }
                    }
                }//enf if
            }
        }//enf if
        return valueReturn;
    }

    private boolean paintCapturedCellsPlus1Plus1(int positionX, int positionY, int num) {
        int dX = 1;
        int dY = 1;
        int posX = positionX + dX;
        int posY = positionY + dY;
        boolean valueReturn = false;
        if (posX < NUMCELLS && posY < NUMCELLS && curPlayer != cells[posX][posY].getPlayer()) {
            while (valueReturn == false && posX < (NUMCELLS - 1) && posY < (NUMCELLS - 1) && cells[posX][posY].getPlayer() != 0) {
                posX += dX;
                posY += dY;
                if (curPlayer == cells[posX][posY].getPlayer()) {
                    valueReturn = true;
                    if (num == 1) {
                        for (int i = 0; i <= mod(positionX, posX); i++) {
                            cells[positionX + dX * i][positionY + dY * i].setPlayer(curPlayer);
                        }
                    }
                }//enf if
            }
        }//enf if
        return valueReturn;
    }

    private int mod(int num1, int num2) {
        if (num1 > num2) {
            return (num1 - num2);
        } else {
            return (num2 - num1);
        }
    }

    @Override
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
                System.out.println("Inside refresh:Client" + receivedData);//Debug
                String[] data = receivedData.split(":");
                if (!data[0].equals("playAgain")) {
                    this.opponentName = data[0];
                    int x = Integer.parseInt(data[1]);
                    int y = Integer.parseInt(data[2]);
                    showMessage(receivedData);
                    if (curPlayer == 2) {
                        System.out.println("Before Server Play" + x + " " + y + " " + curPlayer + "\n");
                        cellMovement(x, y);
                        repaint();
                    }
                } else {
                    playAgain();
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
                    @Override
                    public void run()//atualiza a displayArea
                    {
                        connectionStatus.setText(messageToDisplay);
                    }
                });
    }

    private void showWinner() {
        String winner;

        if (player1pieces > player2pieces) {
            winner = this.playerName;
        } else {
            winner = this.opponentName;
        }

        @SuppressWarnings("RedundantStringConstructorCall")
        String msg = new String("The winner of this match was " + winner);
        showMessage(msg);
        repaint();

    }

    private void playAgain() {

        for (int i = 0; i < NUMCELLS; i++) {
            for (int j = 0; j < NUMCELLS; j++) {
                this.cells[i][j].setPlayer(0);
            }
        }
        player1pieces = player2pieces = 0;
        showScore();
        placeInitCells();
        repaint();
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
                try {
                    cellMovement(e.getX(), e.getY());
                    sendData(playerName + ":" + e.getX() + ":" + e.getY());
                    repaint();
                } catch (IOException ex) {
                    Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                showMessage("It is the other player turn!");
            }
        }
    }

    public class ActListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().equals("playAgain")) {
                playAgain();
            }
        }
    }

    private void sendData(String message) throws IOException {
        try {
            output = new PrintStream(client.getOutputStream());
            output.println(message);
            output.flush();
            System.out.println("Sending data from client...\n" + message + "\n"); //DEBUG
        } catch (BufferOverflowException e) {
            JOptionPane.showMessageDialog(null, "An exception was found while sending data");
        }
    }
}
