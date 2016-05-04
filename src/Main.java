import java.io.*;
import java.net.*;

public class Main {

    private static boolean connected = false;
    private static boolean internet = false;
    private static boolean running = true;

    private static ServerSocket serverSocket = null;
    private static Socket fromClient = null;
    private static String externalIP;

    private static String domainName = "remoteserver.anondns.net";
    private static String key = "f8ebf7d46abe08f46e9022acd4b96422";

    public static void main(String[] args) throws InterruptedException, IOException {

        while (running) {
            do
            {
                internet = CheckInternetConnection();
                if (!internet) {
                    System.out.print("\tRetrying after 10 seconds...\n");
                    Thread.sleep(10000);
                }
            } while (!internet);

            while (serverSocket == null) {
                CreateServerSocket();
                Thread.sleep(1000);
            }

            Thread IPPostingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        SendIPToServer();
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            System.out.print("Thread sleep error\n");
                        }
                    }
                }
            });
            IPPostingThread.start();

            WaitingForClient();

            while (connected) {
                try {
                    Exchange();
                } catch (IOException e) {
                    connected = false;
                    System.out.print("Client disconnected\n");
                }
                Thread.sleep(500);
            }
        }
    }

    private static boolean CheckInternetConnection() throws IOException {
        try {
            URL url = new URL("http://www.google.com");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.connect();
            if (con.getResponseCode() == 200){
                System.out.print("Internet connection available\n");
                externalIP = ExternalIPCatcher.GetIP();
                return true;
            }
        } catch (Exception exception) {
            System.out.print("Internet connection not available\n");
            return false;
        }
        System.out.print("Internet connection not available\n");
        return false;
    }

    private static void SendIPToServer() {

        String endUrl = "http://anondns.net/api/set/" + domainName + "/" + key;
        HttpURLConnection connection;
        try {
            URL url = new URL(endUrl);
            connection = (HttpURLConnection)url.openConnection();
            connection.connect();
            int response = connection.getResponseCode();
            if (response == 200)
                System.out.print("\t-IP was sent onto server\n");
            else
                System.out.print("There were some trouble with IP sending");
        } catch (MalformedURLException e) {
            System.out.print("Cannot create URL\n");
        } catch (IOException e) {
            System.out.print("Open connection issue\n");
        }
    }

    private static void Exchange() throws IOException, InterruptedException {
        InputStream sin = fromClient.getInputStream();
        OutputStream sout = fromClient.getOutputStream();

        ObjectOutputStream out = new ObjectOutputStream(sout);
        DataInputStream in = new DataInputStream(sin);

        String line;
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
                return "Test done!";
            case "EXIT":
                System.exit(0);
                break;
            case "TREE": //Gets only one string argument (Path)
                return GetFileTree(args);
            case "CMD": //Gets command lines separated by |
                return ExecuteCMD(args);

        }
        return null;
    }

    private static String[] ExecuteCMD(String ... command) {
        String[] args = new String[command.length-1];
        System.arraycopy(command, 1, args, 0, args.length);
        //CMD function
        return args;
    }

    private static String[] GetFileTree(String[] args) {
        String path = args[1];
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
        int localPort = 0;
        try {
            serverSocket = new ServerSocket(localPort);
            localPort = serverSocket.getLocalPort();
            System.out.print("Socket started at " + localPort +  " with local address " + serverSocket.getInetAddress().toString() + " and remote " + externalIP + ") \n");
        } catch (IOException e) {
            System.out.print("Cannot bind to port " + localPort + "\n");
            return false;
        }
        return true;
    }
}
