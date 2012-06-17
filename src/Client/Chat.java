/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.BufferOverflowException;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

/**
 *
 * @author Pedro Tanaka
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
    private String serverIp;
    private int serverPort;
    private Socket server;
    private String playerName;

    public Chat(String serverIp, int serverPort, String playerName) {
        setLayout(null);

        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.playerName = playerName;

        displayArea = new JTextArea();
        displayArea.setRows(10);
        displayArea.setColumns(80);
        displayArea.setLineWrap(true);

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

    private void sendData(String message){
        try
        {
            output.println(this.playerName + ": " + message);
            output.flush();
            showMessage("\n" + this.playerName + ": " + message);
        }catch(BufferOverflowException e) {
            displayArea.append("\nErro ao enviar dados");
        }
    }


    @SuppressWarnings("CallToThreadDumpStack")
    private void runClient() throws IOException{
        try {
            connectServer();
            getIOData();
            processData();
        } catch (EOFException eofe) {
            showMessage("Falha ao conectar...");
        } catch (IOException ioe){
            ioe.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
     private void closeConnection()
    {
        showMessage("\nFechando a conexão");
        setTextFieldEditable(false);

        try
        {
            output.close();
            input.close();
            server.close();
        }
        catch(IOException ioException)
        {
            ioException.printStackTrace();
        }
    }

    private void connectServer() throws IOException
    {
        showMessage("Tentando conectar...\n");

        server = new Socket(InetAddress.getByName(serverIp), this.serverPort);

        showMessage("Conexão estabelecida com: " + server.getInetAddress().getHostName());
    }

    private void showMessage(final String messageToDisplay)
    {
        SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run()
                    {
                        displayArea.append(messageToDisplay);
                    }
                }
        );
    }

    private void getIOData() throws IOException
    {
        output = new PrintStream(server.getOutputStream());
        input = new BufferedReader(new InputStreamReader(server.getInputStream()));
    }

    private void processData() throws IOException
    {
        boolean tudoOk = true;
        setTextFieldEditable(true);

        do
        {
            try
            {
                message = (String)input.readLine();
                showMessage("\n" + message);
            }
            catch(IOException e)
            {
                tudoOk = false;
                showMessage("\nO tipo de mensagem recebida é desconhecido");
            }

        }while(tudoOk);
    }

    private void setTextFieldEditable(final boolean editable)
    {
       SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run()
                    {
                        enterField.setEditable(editable);
                    }
                }
        );
    }

    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /*
     * TODO
     */

}

