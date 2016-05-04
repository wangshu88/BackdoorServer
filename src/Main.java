import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    private static boolean connected = false;
    private static ServerSocket serverSocket = null;
    private static Socket fromClient = null;
    private static BufferedReader in;
    private static PrintWriter out;

    public static void main(String[] args) throws InterruptedException {
        while (serverSocket == null) {
            CreateServerSocket();
            Thread.sleep(2000);
        }
//        if (!WaitingForClient()) {
//            Thread.sleep(2000);
//            main(args);
//        }
        while (true) {
            if (connected)
                try {
                    Exchange();
                } catch (IOException e) {
                    connected = false;
                    System.out.print("Client disconnected\n");
                }
            else
                WaitingForClient();
            Thread.sleep(500);
        }
    }

    private static void Exchange() throws IOException, InterruptedException {
        InputStream sin = fromClient.getInputStream();
        OutputStream sout = fromClient.getOutputStream();

//        ObjectInputStream in = new ObjectInputStream(sin);
        ObjectOutputStream out = new ObjectOutputStream(sout);

        DataInputStream in = new DataInputStream(sin);
//        DataOutputStream out = new DataOutputStream(sout);

        String line = null;
        while (connected) {
            line = in.readUTF();
            System.out.println("Client message: " + line);

            Object object = CheckCommand(line);

            out.writeObject(object); // отсылаем клиенту обратно ту самую строку текста.
            out.flush(); // заставляем поток закончить передачу данных.
            System.out.println("Waiting for the next line...");
            System.out.println();
        }
    }

    private static Object CheckCommand(String line) {
        String[] args = line.split("\\|");
        switch (args[0]) {
            case "TEST":
                return "test done!";
            case "EXIT":
                System.exit(0);
                break;
            case "TREE": //Gets only one string argument (Path)
                return GetFileTree(args);
        }
        return null;
    }

    private static String[] GetFileTree(String[] args) {
        String path = (String) args[1];
        return Filewalker.walk(path);
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
