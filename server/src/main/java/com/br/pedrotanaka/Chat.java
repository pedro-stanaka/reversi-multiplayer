/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.br.pedrotanaka;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.BufferOverflowException;

/**
 * @author Pedro Tanaka
 * @Carolina Massae Kita
 */
public class Chat extends JPanel implements Runnable {

    /*
     * IO and UI elements
     */
    public JTextField enterField;
    private JTextArea displayArea;
    private PrintStream output;
    private BufferedReader input;
    private String message = "";
    private JScrollPane scroll;

    /*
     * Network Elements
     */
    private int serverPort;
    private ServerSocket server;
    private String playerName;
    private Socket connection;

    public Chat(int serverPort, String playerName) {
        setLayout(null);

        this.serverPort = serverPort;
        this.playerName = playerName;

        displayArea = new JTextArea();
        displayArea.setRows(10);
        displayArea.setColumns(80);
        displayArea.setLineWrap(true);
        displayArea.setEditable(false);

        scroll = new JScrollPane(displayArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBounds(0, 0, 500, 160);
        add(scroll);

        enterField = new JTextField();
        enterField.requestFocus();
        enterField.setEditable(false);
        enterField.setBounds(0, 170, 300, 25);

        enterField.addActionListener(
                new ActionListener() {

                    public void actionPerformed(ActionEvent event) {
                        sendData(event.getActionCommand());
                        enterField.setText("");
                    }
                });
        add(enterField);
    }

    private void showMessage(final String messageToDisplay) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        displayArea.append(messageToDisplay);
                    }
                }
        );
    }

    private void sendData(String message) {
        try {
            output.println(this.playerName + ": " + message);
            output.flush();
            showMessage("\n" + this.playerName + ": " + message);
        } catch (BufferOverflowException e) {
            displayArea.append("\nErro ao enviar dados");
        }
    }


    private void waitConnection() throws IOException {
        showMessage("Waiting for connections...\nListenning to the port " + this.serverPort + "\n");
        connection = server.accept();
        showMessage("Connection stabilished with: " + connection.getInetAddress().getHostName() + "\n");
    }

    private void getIOdata() throws IOException {
        output = new PrintStream(connection.getOutputStream());
        input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    }

    private void setTextFieldEditable(final boolean editable) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        enterField.setEditable(editable);
                    }
                }
        );
    }

    private void manipulaDadosNaJanela() throws IOException {
        boolean tudoOk = true;

        String mensagem = " Connetion successfully... ";
        showMessage(mensagem);
        setTextFieldEditable(true);

        do {
            try {
                mensagem = (String) input.readLine();
                showMessage("\n" + mensagem); //exibe a mensagem
            } catch (IOException e) {
                tudoOk = false;
                showMessage("\n The message is in wrong format.");

            }

        } while (tudoOk);
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void closeConnection() {
        showMessage("\n Closing connection! \n");
        setTextFieldEditable(false);

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
            while (true) {
                try {
                    waitConnection();
                    getIOdata();
                    manipulaDadosNaJanela();
                } catch (EOFException eofexc) {
                    showMessage("\n Server has closed the connection");
                    eofexc.printStackTrace();
                } finally {
                    closeConnection();
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    public void run() {
        runServer();
    }

}
