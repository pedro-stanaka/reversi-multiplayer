/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.awt.Polygon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;

/**
 *
 * @author Pedro Tanaka
 */
public class Board extends JPanel implements Runnable {

//Atributes
    Cell[][] cells = new Cell[8][8];
    private int curPlayer;
    private int player1pieces;
    private int player2pieces;

    public Board() {
        //initComponents();
        player1pieces = player2pieces = 0;
        drawBoad();
        MouseEvt mouseEvent = new MouseEvt();
        addMouseListener(mouseEvent);
    }

    private void drawBoad() {
        initCells();
        placeInitCells();
        createPolygons();
    }

    private void initCells() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                this.cells[i][j] = new Cell();
                this.cells[i][j].setX(i);
                this.cells[i][j].setY(j);
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
        int npoints = 4;
        int width = 40;
        int height = 40;
        int initX = 20;
        int initY = 20;
        Polygon p;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int xpoints[] = {i * width + initX, ((i + 1) * (width / 2)) + initX, ((i + 1) * (width / 2)) + initX, (i + 1) * width + initX};
                int ypoints[] = {(j + 1) * (height / 2) + initY, (j * height + initY), ((j + 1) * height + initY), (j + 1) * (height / 2) + initY};
                p = new Polygon(xpoints, ypoints, npoints);
                cells[i][j].setPolygon(p);
                cells[i][j].setDraw(Boolean.TRUE);
            }
        }

    }

    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public class MouseEvt extends MouseAdapter {

        public void mouseCliked(MouseEvent e) {

            if (curPlayer == 1) {
            } else {
            }
            repaint();
        }
    }
}
