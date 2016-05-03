import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    private static boolean connected = false;
    private static ServerSocket serverSocket = null;
    private static Socket fromClient = null;
    private static BufferedReader in;
    private static PrintWriter out;

    public static void main(String[] args) throws IOException, InterruptedException {
        if (!CreateServerSocket()) main(args);
        if (!WaitingForClient()) {
            Thread.sleep(5000);
            return;
        }

        InputStream sin = fromClient.getInputStream();
        OutputStream sout = fromClient.getOutputStream();

        DataInputStream in = new DataInputStream(sin);
        DataOutputStream out = new DataOutputStream(sout);

        String line = null;
        while (connected) {
            line = in.readUTF();
            System.out.println("The dumb client just sent me this line : " + line);
            System.out.println("I'm sending it back...");

            CheckCommand(line);
            
            out.writeUTF(line); // отсылаем клиенту обратно ту самую строку текста.
            out.flush(); // заставляем поток закончить передачу данных.
            System.out.println("Waiting for the next line...");
            System.out.println();
//            Thread.sleep(1000);
        }

        serverSocket.close();
    }

    private static Object CheckCommand(String line, Object ... args) {
        switch (line) {
            case "TEST":
                return "test done!";
                break;
            case "EXIT":
                System.exit(0);
                break;
            case "TREE": //Gets only one string argument (Path)
                GetFileTree(args);
                break;
        }
    }

    private static void GetFileTree(Object ... args) {
        String path = (String) args[0];

    }

    private static boolean WaitingForClient() {
        try {
            System.out.print("Waiting for a client...\n");
            fromClient = serverSocket.accept();
            System.out.print("Client connected\n");
        } catch (IOException e) {
            System.out.print("Can't accept\n");
            return false;
        }
        connected = true;
        return true;
    }

    private static boolean CreateServerSocket() {
        try {
            serverSocket = new ServerSocket(9999);
            System.out.print("Socket started at 9999 with address " + serverSocket.getInetAddress().getHostAddress() + "\n");
        } catch (IOException e) {
            System.out.print("Cannot bind to port 9999\n");
            return false;
        }
        return true;
    }
}
