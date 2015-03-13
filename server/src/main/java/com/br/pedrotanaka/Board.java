/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.br.pedrotanaka;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.BufferOverflowException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Pedro Tanaka
 */
public class Board extends JPanel implements Runnable {

    private static final int NUM_CELLS = 8;
    Cell[][] cells = new Cell[8][8];
    private int curPlayer;
    private int player1pieces;
    private int player2pieces;
    private String playerName;
    private String opponentName;
    private JLabel score;
    private PrintStream output;
    private BufferedReader input;
    private Socket connection;
    private ServerSocket server;
    private int serverPort;
    private JLabel connectionStatus;
    private JButton playAgainButton;

    public Board(int serverPort, String playerName) {
        this.serverPort = 12345;
        this.playerName = playerName;

        curPlayer = 1;

        connectionStatus = new JLabel("<html><p>Ready to receive connections...<br/>Listening to the port</p></html>" + serverPort);
        connectionStatus.setBounds(560, 10, 180, 60);
        add(connectionStatus);

        score = new JLabel();
        score.setBounds(0, 200, 100, 100);
        score.setForeground(Color.WHITE);
        score.setBorder(BorderFactory.createBevelBorder(0));
        add(score);

        ActListener csActionListener = new ActListener();
        playAgainButton = new JButton(new ImageIcon("rsc/logo.png"));
        playAgainButton.setText("Play Again!");
        playAgainButton.setVerticalTextPosition(AbstractButton.CENTER);
        playAgainButton.setHorizontalTextPosition(AbstractButton.CENTER);
        playAgainButton.addActionListener(csActionListener);
        playAgainButton.setMnemonic(KeyEvent.VK_P);
        playAgainButton.setActionCommand("playAgain");
        playAgainButton.setBounds(0, 0, 100, 100);
        playAgainButton.setForeground(Color.red);

        playAgainButton.setVisible(false);
        playAgainButton.setEnabled(false);
        add(playAgainButton);

        drawBoard();

        MouseEvt mouseEvent = new MouseEvt();
        addMouseListener(mouseEvent);

    }

    private void drawBoard() {
        initCells();
        placeInitCells();
        createPolygons();
    }

    private void initCells() {
        for (int i = 0; i < NUM_CELLS; i++) {
            for (int j = 0; j < NUM_CELLS; j++) {
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
        int npoints = 4;    // Number of points that the polygon has
        int width = 60;     // Width of the cell
        int height = 60;    // Height of the cell
        int initX = 30;     //Distância da Margem em X
        int initY = 50;     //Distância da Margem em Y
        Polygon p;          //Ponteiro de um poligono


        for (int i = 0; i < NUM_CELLS; i++) {
            for (int j = 0; j < NUM_CELLS; j++) {
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

        for (int i = 0; i < NUM_CELLS; i++) {
            for (int j = 0; j < NUM_CELLS; j++) {
                if (cells[i][j].getDraw()) {
                    {
                        g.setColor(cells[i][j].getColor());
                        g.fillPolygon(cells[i][j].getPolygon());
                        g.drawPolygon(cells[i][j].getPolygon());
                    }
                }
            }
        }
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

    /**
     * @param x position X from the mouse event
     * @param y position Y from the mouse event
     */
    private void cellMovement(int x, int y) {
        for (int i = 0; i < NUM_CELLS; i++) {
            for (int j = 0; j < NUM_CELLS; j++) {
                if (cells[i][j].Contains(x, y)) {
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
                        showMessage(" ----- Forbidden movement. ------ ");
                    }
                }
            }
        }
        countPieces();
        showScore();
        repaint();
    }

    private void countPieces() {
        this.player1pieces = this.player2pieces = 0;
        for (int i = 0; i < NUM_CELLS; i++) {
            for (int j = 0; j < NUM_CELLS; j++) {
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

    private void showWinner() {
        String winner;

        if (player1pieces > player2pieces) {
            winner = this.playerName;
        } else {
            winner = this.opponentName;
        }
        playAgainButton.setVisible(true);
        playAgainButton.setEnabled(true);

        @SuppressWarnings("RedundantStringConstructorCall")
        String msg = new String("The winner of this match was " + winner);
        showMessage(msg);


        repaint();

    }

    private void playAgain() throws IOException {
        sendData("playAgain" + ":" + this.playerName);
        for (int i = 0; i < NUM_CELLS; i++) {
            for (int j = 0; j < NUM_CELLS; j++) {
                this.cells[i][j].setPlayer(0);
            }
        }
        placeInitCells();
        playAgainButton.setEnabled(false);
        playAgainButton.setVisible(false);
        repaint();
    }

    private void showScore() {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        String gameScore;
                        gameScore = "<html>You have: " + player1pieces + " pieces<br/>";
                        gameScore += opponentName + " has: " + player2pieces + " pieces</html>";
                        score.setText(gameScore);
                    }
                });
    }

    private boolean verifyHasMovements() {
        boolean valueReturn = false;
        int num = 2;

        for (int posX = 0; posX < NUM_CELLS; posX++) {
            for (int posY = 0; posY < NUM_CELLS; posY++) {
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
        if (!valueReturn) {
            showMessage("##### FORBIDDEN MOVEMENT ####");
        }
        return valueReturn;
    }

    private boolean paintCapturedCellsPlus0Plus1(int positionX, int positionY, int num) {
        int dY = 1;
        int posY = positionY + dY;
        boolean valueReturn = false;
        if (posY < NUM_CELLS && curPlayer != cells[positionX][posY].getPlayer()) {
            while (!valueReturn && posY < (NUM_CELLS - 1) && cells[positionX][posY].getPlayer() != 0) {
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
        if (posX >= 0 && posY < NUM_CELLS && curPlayer != cells[posX][posY].getPlayer()) {
            while (!valueReturn && (posX > 0) && (posY < (NUM_CELLS - 1)) && (cells[posX][posY].getPlayer() != 0)) {
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
            while (!valueReturn && (posX > 0) && (cells[posX][positionY].getPlayer() != 0)) {
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
            while (!valueReturn && (posX > 0) && (posY > 0) && (cells[posX][posY].getPlayer() != 0)) {
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
            while (!valueReturn && (posY > 0) && (cells[positionX][posY].getPlayer() != 0)) {
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
        if (posX < NUM_CELLS && posY >= 0 && curPlayer != cells[posX][posY].getPlayer()) {
            while (!valueReturn && (posX < (NUM_CELLS - 1)) && (posY > 0) && (cells[posX][posY].getPlayer() != 0)) {
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
        if (posX < NUM_CELLS && curPlayer != cells[posX][positionY].getPlayer()) {
            while (!valueReturn && (posX < (NUM_CELLS - 1)) && (cells[posX][positionY].getPlayer() != 0)) {
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
        if (posX < NUM_CELLS && posY < NUM_CELLS && curPlayer != cells[posX][posY].getPlayer()) {
            while (!valueReturn && (posX < (NUM_CELLS - 1)) && (posY < (NUM_CELLS - 1)) && (cells[posX][posY].getPlayer() != 0)) {
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

    private void waitConnection() throws IOException {
        connection = server.accept();
        connectionStatus("Conectado");
    }

    private void sendData(String message) throws IOException {
        try {
            output = new PrintStream(connection.getOutputStream());
            output.println(message);
            output.flush();
        } catch (BufferOverflowException boe) {
            JOptionPane.showMessageDialog(null, "Error while sending data!");
        }
    }

    private void getIOData() throws IOException {
        output = new PrintStream(connection.getOutputStream());
        input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        //System.out.println("(ins getIO)input.readLine= "+input.readLine());//DEBUG
    }

    private void refreshScreenStatus() throws IOException {
        boolean goingWell = true;
        do {
            try {
                String receivedData;
                receivedData = input.readLine();
                String[] data = receivedData.split(":");

                this.opponentName = data[0];
                int x = Integer.parseInt(data[1]);
                int y = Integer.parseInt(data[2]);
                showMessage(receivedData);

                if (curPlayer == 2) {
                    System.out.println("Testing Transmission");//Debug
                    cellMovement(x, y);
                    repaint();
                }
            } catch (IOException ioE) {
                goingWell = false;
                JOptionPane.showMessageDialog(null, "Unknown data type!");
            }
        } while (goingWell);
    }

    private void connectionStatus(final String status) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        connectionStatus.setText(status);
                    }
                });
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void closeConnection() {
        try {
            output.close();
            input.close();
            connection.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void runServer() {
        try {
            server = new ServerSocket(this.serverPort, 1);
            if (server.getLocalPort() != 12345) {
                System.out.println("Not conn!");
            } //debug
            try {
                waitConnection();
                getIOData();
                System.out.println("Before refresh");
                refreshScreenStatus();
            } catch (EOFException eofException) {
                connectionStatus("Disconnected");
                JOptionPane.showMessageDialog(null, "Connection Failure");
            } finally {
                closeConnection();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public void run() {
        runServer();
    }

    public class MouseEvt extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (curPlayer == 1) {
                try {
                    cellMovement(e.getX(), e.getY());
                    sendData(playerName + ":" + e.getX() + ":" + e.getY()); //NetWork Problem
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
                try {
                    playAgain();
                } catch (IOException ex) {
                    Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
