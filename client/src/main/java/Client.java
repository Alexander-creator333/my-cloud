import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class Client implements Initializable {

    public Button copy;
    public Button delete;
    public Button connect;
    public Button mkDir;
    public ListView<String> listViewL;
    public ListView<String> listViewR;
    public TextField serverName;
    public TextField mkDirName;
    private List<File> clientFileList;
    private String clientPath = "c:\\333\\tmp";
    public static Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    private boolean connectStatus = false;

    public void sendCommand(ActionEvent actionEvent) {
        System.out.println(serverName.getText() );
    }

    private void ListLRefresh() throws IOException {
        if(connectStatus){
            try {
                os.writeUTF("./list");
                os.flush();
                String response;
                listViewL.getItems().clear();
                listViewL.getItems().add(".. : UP-TO-DIR");
                do {
                    response = is.readUTF();
                    if(response.equals("./list-end")) break;
                    listViewL.getItems().add(response);
                } while (true);//(!response.equals("./list-end"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            listViewL.getItems().clear();
            listViewL.getItems().add("not connected...");
        }
    }

    private void ListRRefresh() throws IOException {
        clientFileList = new ArrayList<>();
        File dir = new File(clientPath);
        listViewR.getItems().clear();
        listViewR.getItems().add(".. : UP-TO-DIR");
        if (!dir.exists()) {
            //throw new RuntimeException("directory resource not exists on client");
            listViewR.getItems().add("ERROR! So sory...");
        }
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            clientFileList.add(file);
            this.listViewR.getItems().add(file.getName() + " : " + file.length());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: 7/21/2020 init connect to server
        try{
            ListRRefresh();
            listViewL.setOnMouseClicked(a -> {
                if (a.getClickCount() == 2) {
                    String fileName = listViewL.getSelectionModel().getSelectedItem();
                    String[] subStr;
                    subStr = fileName.split(" : ");
                    fileName = subStr[0];
                    try{
                        os.writeUTF("./chdir");
                        os.writeUTF(fileName);
                        os.flush();
                        is.readUTF();
                        ListLRefresh();
                    } catch (IOException ignr) {}
                }
            });
            listViewR.setOnMouseClicked(a -> {
                if (a.getClickCount() == 2) {
                    String fileName = listViewR.getSelectionModel().getSelectedItem();
                    String[] subStr;
                    subStr = fileName.split(" : ");
                    fileName = subStr[0];
                    if(fileName.equals("..")){
                        int ipos=0, inum=0;
                        for (int i = 0; i < clientPath.length(); i++) {
                            if(clientPath.toCharArray()[i] == '\\')
                            {
                                ipos=i;
                                inum++;
                            }
                        }
                        if(inum>1) {
                            clientPath = clientPath.substring(0, ipos);
                        } else if (inum==1){
                            clientPath = clientPath.substring(0, ipos);
                            clientPath += "\\";
                        }

                    } else {
                        clientPath = clientPath + "\\" + fileName;
                    }
                    try{
                        ListRRefresh();
                    } catch (IOException ignr) {}
                }
            });
            copy.setDisable(true);
            mkDir.setDisable(true);
            delete.setDisable(true);
            copy.setOnAction(a -> {
                if(connectStatus){
                    try {
                        os.writeUTF("./copy");
                        os.flush();
                        String response = is.readUTF();
                        System.out.println(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    copy.setDisable(true);
                }
            });
            mkDir.setOnAction(a -> {
                if(connectStatus){
                    try {
                        os.writeUTF("./mkDir");
                        os.flush();
                        String response = is.readUTF();
                        System.out.println(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    mkDir.setDisable(true);
                }
            });
            delete.setOnAction(a -> {
                if(connectStatus){
                    try {
                        os.writeUTF("./delete");
                        os.flush();
                        String response = is.readUTF();
                        System.out.println(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    delete.setDisable(true);
                }
            });
            connect.setOnAction(a -> {
                if(!connectStatus){
                    try {
                        socket = new Socket(serverName.getText(), 8189);//порт
                        is = new DataInputStream(socket.getInputStream());
                        os = new DataOutputStream(socket.getOutputStream());
                        Thread.sleep(1000);
                        connectStatus = true;
                        connect.setText("Disconnect");
                        copy.setDisable(false);
                        mkDir.setDisable(false);
                        delete.setDisable(false);
                        ListLRefresh();
                    } catch (Exception e) {
                        e.printStackTrace();
                        connect.setText("ERROR! So Sory...");
                    }
                } else {
                    try{
                        is.close();
                        os.close();
                        socket.close();
                        connectStatus = false;
                        connect.setText("Connect");
                        copy.setDisable(true);
                        mkDir.setDisable(true);
                        delete.setDisable(true);
                        listViewL.getItems().clear();
                    } catch (IOException ignored) {}

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File findFileByName(String fileName) {
        for (File file : clientFileList) {
            if (file.getName().equals(fileName)){
                return file;
            }
        }
        return null;
    }
}
