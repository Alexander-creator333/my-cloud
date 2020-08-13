import javafx.scene.control.ListView;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ServerGUI extends JFrame {
    private boolean serverStatusB = false;
    private JLabel serverStatus;
    private JButton serverStartStop;
    private JTextField serverPort;
    private ListView<String> listLogView;

    Server  serverServer;

    public ServerGUI(){
        super("ServerGUI");

//        tf1.setFont(bigFont);
        serverStatus = new JLabel("Server stoped...");
        Font bigFont = serverStatus.getFont().deriveFont(Font.PLAIN, 30f);
        serverStatus.setFont(bigFont);
        serverStartStop = new JButton("Start server");
        serverStartStop.setFont(bigFont);
        serverPort = new JTextField("31337");
        serverPort.setFont(bigFont);
        //listLogView = new ListView<String>();

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(serverStatus, BorderLayout.CENTER);
        JPanel centerPanel = new JPanel(new FlowLayout());
        centerPanel.add(serverPort, BorderLayout.WEST);
        centerPanel.add(serverStartStop, BorderLayout.EAST);
        JPanel bottomPanel = new JPanel(new FlowLayout());
       // bottomPanel.add(<String>listLogView, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        initListeners();
        serverServer = new Server(8181, "C:\\");

        setBounds(300, 300, 400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //это нужно для того чтобы при


    }
    private void initListeners() {
        serverStartStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(serverStatusB){
                    if(serverServer.Stop()){
                        serverStatusB = false;
                        serverStatus.setText("Server stoped");
                        serverStartStop.setText("Start server");
                    } else {
                        serverStatus.setText("Any ERROR! So sorry....");
                    }

                } else {
                    int sP = Integer.parseInt(serverPort.getText());
                    if((sP < 65535 ) && (sP > 0)){
                        serverServer = new Server(sP);
                        serverServer.start();
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        if(serverServer.isAlive()) {
                            serverStatusB = true;
                            serverStatus.setText("Server started at port: "+sP);
                            serverStartStop.setText("Stop server");
                        } else {
                            serverStatus.setText("Any ERROR! So sorry....");
                        }
                    }
                    else
                    {
                        serverStatus.setText("ERROR - Bad port number!");
                    }

                }
            }
        });
    }

    public static void main(String[] args) {
        ServerGUI app = new ServerGUI();
        app.setVisible(true);
    }
}