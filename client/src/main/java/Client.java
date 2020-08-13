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
    private boolean leftORrightPanel = false;

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
                listViewL.getItems().add(".. : [DIR]");
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
        listViewR.getItems().add(".. : [DIR]");
        if (!dir.exists()) {
            //throw new RuntimeException("directory resource not exists on client");
            listViewR.getItems().add("ERROR! So sory...");
        }
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            clientFileList.add(file);
            if(file.isDirectory())
            {
                this.listViewR.getItems().add(file.getName() + " : " + "[DIR]");
            } else {
                this.listViewR.getItems().add(file.getName() + " : " + file.length());
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: 7/21/2020 init connect to server
        try{
            ListRRefresh();
            listViewL.setOnMouseClicked(a -> {
                leftORrightPanel = false;
                System.out.println("<");

                if (a.getClickCount() == 2) {
                    String fileName = listViewL.getSelectionModel().getSelectedItem();
                    String[] subStr;
                    subStr = fileName.split(" : ");
                    fileName = subStr[0];
                    if(subStr[1].equals("[DIR]")) {
                        try {
                            os.writeUTF("./chdir");
                            os.writeUTF(fileName);
                            os.flush();
                            is.readUTF();
                            ListLRefresh();
                        } catch (IOException ignr) {
                        }
                    }
                }
            });
            listViewR.setOnMouseClicked(a -> {
                leftORrightPanel = true;
                System.out.println(">");
                if (a.getClickCount() == 2) {
                    String fileName = listViewR.getSelectionModel().getSelectedItem();
                    String[] subStr;
                    subStr = fileName.split(" : ");
                    fileName = subStr[0];
                    if(subStr[1].equals("[DIR]")) {
                        if (fileName.equals("..")) {
                            int ipos = 0, inum = 0;
                            for (int i = 0; i < clientPath.length(); i++) {
                                if (clientPath.toCharArray()[i] == '\\') {
                                    ipos = i;
                                    inum++;
                                }
                            }
                            if (inum > 1) {
                                clientPath = clientPath.substring(0, ipos);
                            } else if (inum == 1) {
                                clientPath = clientPath.substring(0, ipos);
                                clientPath += "\\";
                            }

                        } else {
                            clientPath = clientPath + "\\" + fileName;
                        }
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
                if(leftORrightPanel == true){
                    String fileName = listViewR.getSelectionModel().getSelectedItem();
                    String[] subStr;
                    subStr = fileName.split(" : ");
                    fileName = subStr[0];
                    System.out.println("222 "+fileName.toString());
                    if(!subStr[1].equals("[DIR]")) {
                        File currentFile = findFileByName(fileName);
                        if (currentFile != null) {
                            try {
                                os.writeUTF("./upload");
                                os.writeUTF(fileName);
                                os.writeLong(currentFile.length());
                                FileInputStream fis = new FileInputStream(currentFile);
                                byte[] buffer = new byte[1024];
                                while (fis.available() > 0) {
                                    int bytesRead = fis.read(buffer);
                                    os.write(buffer, 0, bytesRead);
                                }
                                os.flush();
                                String response = is.readUTF();
                                System.out.println(response);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            System.out.println("222");
                        }
//                        try {
                            System.out.println("223");
//                            ListLRefresh();
                            System.out.println("224");
//                        } catch (IOException tmp) {}
                        System.out.println("225");
                    }
                    System.out.println("225.5");
                } else {
                    if (connectStatus) {
                        byte[] buffer = new byte[1024];
                        String fileName = listViewL.getSelectionModel().getSelectedItem();
                        String[] subStr = fileName.split(" : ");
                        fileName = subStr[0];
                        if (!subStr[1].equals("[DIR]")) {
                            try {
                                os.writeUTF("./download");
                                os.writeUTF(fileName.toString());
                                System.out.println("333"+fileName.toString());
                                fileName = is.readUTF();
                                if (fileName.equals("./upload")) {
                                    System.out.println("YES");
                                } else {
                                    System.out.println("NO");
                                }
                                fileName = is.readUTF();
                                System.out.println("334"+fileName.toString());
                            } catch (IOException ignored) {
                            }
                            long fileLength = 0;
                            try {
                                fileLength = is.readLong();
                            } catch (IOException tmp) {
                            }
                            File file = new File(clientPath + "\\" + fileName);
                            if (!file.exists()) {
                                try {
                                    file.createNewFile();
                                } catch (IOException tmp) {
                                }
                            System.out.println("335");
                            }
                            System.out.println("336");
                            try {
                                FileOutputStream fos = new FileOutputStream(file);
                                for (long i = 0; i < (fileLength / 1024 == 0 ? 1 : fileLength / 1024); i++) {
                                    int bytesRead = is.read(buffer);
                                    fos.write(buffer, 0, bytesRead);
                                }
                                System.out.println("337");
                            } catch (IOException tmp) {}
                            try {
                                os.writeUTF("./upload-OK");
                                os.flush();
                            } catch (IOException ignored) {
                            }
                            try {
                                ListRRefresh();
                            } catch (IOException tmp) {
                            }
                            System.out.println("Upload OK333");
                        }
                    } else {
                        copy.setDisable(true);
                    }
                }
                System.out.println("226");
            });
            mkDir.setOnAction(a -> {
                if(leftORrightPanel == true){
                    new File(clientPath.toString()+"\\"+mkDirName.getText()).mkdirs();
                    try {
                        ListRRefresh();
                    } catch (IOException tmp) {}
                } else {
                    if (connectStatus) {
                        try {
                            os.writeUTF("./mkDir");
                            os.writeUTF(mkDirName.getText());
                            os.flush();
                            String response = is.readUTF();
                            System.out.println(response.toString());
                            ListLRefresh();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        mkDir.setDisable(true);
                    }
                }
            });
            delete.setOnAction(a -> {
                if(leftORrightPanel == true){
                    String fileName = listViewR.getSelectionModel().getSelectedItem();
                    String[] subStr;
                    subStr = fileName.split(" : ");
                    new File(clientPath.toString()+"\\"+subStr[0].toString()).delete();
                    try {
                        ListRRefresh();
                    } catch (IOException tmp) {}
                } else {
                   if (connectStatus) {
                       try {
                           os.writeUTF("./delete");
                           String fileName = listViewL.getSelectionModel().getSelectedItem();
                           String[] subStr;
                           subStr = fileName.split(" : ");
                           os.writeUTF(subStr[0].toString());
                           os.flush();
                           String response = is.readUTF();
                           System.out.println(response.toString());
                           ListLRefresh();
                       } catch (Exception e) {
                           e.printStackTrace();
                       }
                   } else {
                       delete.setDisable(true);
                   }
                }
            });
            connect.setOnAction(a -> {
                if(!connectStatus){
                    try {
                        socket = new Socket(serverName.getText(), 31337);//порт
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
