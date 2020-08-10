import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ServerGUI extends JFrame {
    private boolean serverStatusB = false;
    private JLabel serverStatus;
    private JButton serverStartStop;
    private JTextField serverPort;
    Server  serverServer;

    public ServerGUI(){
        super("ServerGUI");

        serverStatus = new JLabel("Server stoped...");
        serverStartStop = new JButton("Start server");
        serverPort = new JTextField("8189");

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(serverStatus, BorderLayout.NORTH);
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(serverPort, BorderLayout.NORTH);
        bottomPanel.add(serverStartStop, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);

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