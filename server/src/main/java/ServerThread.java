import java.io.*;
import java.net.Socket;
import java.util.Objects;

class ServerThread extends Thread {

    private Socket socket;
    private String serverPath = "./server/src/main/resources";

//    private BufferedReader in;
//    private BufferedWriter out;
    private DataInputStream in;
    private DataOutputStream out;

    private boolean isAlive = true;

    public ServerThread(Socket socket, String serverPath) throws IOException {
        this.socket = socket;
        this.serverPath = serverPath;
//        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        start(); // вызываем run()
    }
    @Override
    public void run() {
        String msg;
        try {
            while (isAlive) {
                msg = in.readUTF();
                if(msg.equals("./list")){
                    try {
                        File dir = new File(serverPath);
                        if (!dir.exists()) {
                            //throw new RuntimeException("directory resource not exists on client");
                            out.writeUTF("ERROR! So sory...");
                            out.writeUTF("./list-end");
                            out.flush();
                        } else {
                            for (File file : Objects.requireNonNull(dir.listFiles())) {
                                out.writeUTF(file.getName() + " : " + file.length());
                            }
                            out.writeUTF("./list-end");
                            out.flush();
                        }
                    } catch (IOException ignores) {}
                } else if(msg.equals("./chdir")){
                    msg = in.readUTF();
                    System.out.println(serverPath+" : "+msg);
                    if(msg.equals("..")){
                        int ipos=0, inum=0;
                        for (int i = 0; i < serverPath.length(); i++) {
                            if(serverPath.toCharArray()[i] == '\\')
                            {
                                ipos=i;
                                inum++;
                            }
                        }
                        if(inum>1) {
                            serverPath = serverPath.substring(0, ipos);
                        } else if (inum==1){
                            serverPath = serverPath.substring(0, ipos);
                            serverPath += "\\";
                        }
                    } else{
                        serverPath = serverPath + "\\" + msg;
                    }
                    out.writeUTF("./chdir-OK");
                    out.flush();
                } else{
                    try {
                        out.writeUTF("ECHO: "+msg + "\n");
                        out.flush();
                    } catch (IOException ignored) {}
                }

//                  for (ServerSomthing vr : Server.serverList) {
//                    vr.send(word);
//                }
            }

        } catch (IOException e) {
        }
    }

    public void closeNow()
    {
        isAlive = false;
        try {
            socket.close();
        } catch (IOException ignored) {

        }
    }

/*
    private void send(String msg) {
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException ignored) {}
    }
 */

}