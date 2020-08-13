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
        byte [] buffer = new byte[1024];
        try {
            while (isAlive) {
                msg = in.readUTF();
                System.out.println(">> "+msg);
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
                                if(file.isDirectory()){
                                    out.writeUTF(file.getName() + " : [DIR]");
                                } else {
                                    out.writeUTF(file.getName() + " : " + file.length());
                                }
                            }
                            out.writeUTF("./list-end");
                            out.flush();
                        }
                    } catch (IOException ignores) {}
                } else if(msg.equals("./download")){
                    String fileName = in.readUTF();
                    File currentFile = findFileByName(fileName);
                    if (currentFile != null) {
                        try {
                            out.writeUTF("./upload");
                            out.writeUTF(fileName);
                            out.writeLong(currentFile.length());
                            FileInputStream fis = new FileInputStream(currentFile);
                            while (fis.available() > 0) {
                                int bytesRead = fis.read(buffer);
                                out.write(buffer, 0, bytesRead);
                            }
                            out.flush();
                            String response = in.readUTF();
                            System.out.println(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if(msg.equals("./upload")){
                    String fileName = in.readUTF();
                    long fileLength = in.readLong();
                    File file = new File(serverPath + "\\" + fileName);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    try(FileOutputStream fos = new FileOutputStream(file)) {
                        for (long i = 0; i < (fileLength / 1024 == 0 ? 1 : fileLength / 1024); i++) {
                            int bytesRead = in.read(buffer);
                            fos.write(buffer, 0, bytesRead);
                        }
                        fos.close();
                    }
                    try{
                        out.writeUTF("./upload-OK");
                        out.flush();
                    } catch (IOException ignored) {}
                    msg = "tmp";
                    System.out.println("Upload OK333");
                } else if(msg.equals("./delete")){
                    msg = in.readUTF();
                    System.out.println("delete "+serverPath.toString()+"\\"+msg.toString());
                    new File(serverPath.toString()+"\\"+msg.toString()).delete();
                    try{
                        out.writeUTF("./delete-OK");
                        out.flush();
                    } catch (IOException ignored) {}
                } else if(msg.equals("./mkDir")){
                    msg = in.readUTF();
                    System.out.println("mkDir "+serverPath.toString()+"\\"+msg.toString());
                    new File(serverPath.toString()+"\\"+msg.toString()).mkdirs();
                    try{
                        out.writeUTF("./mkDir-OK");
                        out.flush();
                    } catch (IOException ignored) {}
                } else if(msg.equals("./chdir")){
                    msg = in.readUTF();
                    System.out.println(serverPath.toString()+" : "+msg);
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
    private File findFileByName(String fileName) {
        File dir = new File(serverPath);
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if(file.getName().equals(fileName)){
                return file;
            }
        }
        return null;
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