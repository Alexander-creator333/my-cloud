import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server extends Thread {

    private static String serverPath = "c:\\"; //""./server/src/main/resources";
    public static int serverPort = 8081;
    public static LinkedList<ServerThread> serverList = new LinkedList<>();
    private ServerSocket serverSK;
    private boolean isAlive = true;

    public Server(){    }

    public Server(int serverPort){
        this.serverPort = serverPort;
    }

    public Server(String serverPath){
        this.serverPath = serverPath;
    }

    public Server(int serverPort, String serverPath){
        this.serverPort = serverPort;
        this.serverPath = serverPath;
    }

    @Override
    public void run() {
        try {
            this.Start();
        } catch (IOException ignored){

        }
    }
    public void Start() throws IOException {
        try {
            serverSK = new ServerSocket(serverPort);
        } catch (IOException ignored){
            isAlive = false;
        }
        try {
            while (isAlive) {
                Socket socket = serverSK.accept();
                try {
                    serverList.add(new ServerThread(socket, serverPath));
                } catch (IOException e) {
                    socket.close();
                }
            }
        } finally {
            serverSK.close();
            isAlive = false;
        }
    }

    public boolean Stop(){
         for (ServerThread vr : Server.serverList) {
             vr.closeNow() ;
         }
         isAlive = false;
         try {
             serverSK.close();
         } catch (IOException ignor) {}
         return true;
    }

}
