import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * This class runs the server
 *
 * @author Adhi Ramkumar & Divy Sunderraj
 * @version April 25 2020
 */
final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    private static Object object = new Object();
    private final String wordsToFilterFileName;


    private ChatServer(int port, String wordsToFilterFileName) {
        this.port = port;
        this.wordsToFilterFileName = wordsToFilterFileName;
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        int portNumber;
        String fileName;
        if (args.length == 2) {
            portNumber = Integer.parseInt(args[0]);
            fileName = args[1];
        } else if (args.length == 1) {
            portNumber = Integer.parseInt(args[0]);
            fileName = "badwords.txt";
        } else {
            portNumber = 1500;
            fileName = "badwords.txt";
        }
        ChatServer server = new ChatServer(portNumber, fileName);
        server.start();
    }

    private synchronized void broadcast(String message) {
        // need to implement concurrency
        SimpleDateFormat timeStamp = new SimpleDateFormat("HH:mm:ss");
        Date today = new Date();
        String time = timeStamp.format(today);

        ChatFilter cf = new ChatFilter(wordsToFilterFileName);
        message = cf.filter(message);

        message = time + " " + message;

        System.out.println(message);


        for (ClientThread client : clients)
            client.writeMessage(message);
    } //broadcast

    private synchronized void directMessage(String message, String recipient) {
        SimpleDateFormat timeStamp = new SimpleDateFormat("HH:mm:ss");
        Date today = new Date();
        String time = timeStamp.format(today);

        ChatFilter cf = new ChatFilter(wordsToFilterFileName);
        message = cf.filter(message);

        message = time + " " + message;

        System.out.println(message);

        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).username.equals(recipient)) {
                clients.get(i).writeMessage(message);
                break;
            }
        }
    }

    private void remove(int id) {
        synchronized (object) {
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).id == id) {
                    clients.remove(i);
                }
            }
        }
    } //remove


    /**
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     *
     * @author your name and section
     * @version date
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private boolean writeMessage(String message) {
            try {
                sOutput.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return socket.isConnected();
        } //writeMessage

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            username = checkDuplicate(username);
            if (writeMessage(username + ": Ping")) {
                String welcome = "Welcome to the server, " + username + "\nIn order to communicate, please enter" +
                        " your message and then push enter." +
                        "\nIf you wish to direct message someone, enter '/msg' " +
                        "and follow the directions.\nIf you wish for a list of all the connected clients, enter" +
                        " '/list.'";
                broadcast(welcome);
                while (true) { // Loop while able to connect
                    try {
                        cm = (ChatMessage) sInput.readObject();
                        if (cm.getMessage().equals("/logout") && cm.getType() == 1) { //logout request
                            String logoutMsg = username + " has logged off.";
                            broadcast(logoutMsg);
                            close();
                            break;
                        } else if (cm.getType() == 2 && cm.getRecipient() != null) { //direct message
                            String recipient = cm.getRecipient();
                            if (username.equals(recipient)) {
                                writeMessage("You cannot message yourself");
                            } else {
                                writeMessage("Your message was sent to " + recipient);
                                directMessage(username + ": " + cm.getMessage(), recipient);
                            }
                        } else if (cm.getMessage().equals("/list") && cm.getType() == 2) {
                            writeClientList(username);
                            //list all active users
                        } else if (cm.getType() == 0) {
                            broadcast(username + ": " + cm.getMessage());
                        } //regular message
                    } catch (IOException e) {
                        String lostConnection = username + " has lost connection";
                        broadcast(lostConnection);
                        break;
                    } catch (ClassNotFoundException e) {
                        break;
                    }
                }

            }

            remove(id);
            broadcast(username + " has been removed");
            close();
        }

        private synchronized void writeClientList(String username) {
            if (clients.size() == 1) {
                writeMessage("You are the only client");
            } else {
                for (int i = 0; i < clients.size(); i++) {
                    if (!clients.get(i).username.equals(username)) {
                        writeMessage(clients.get(i).username);
                    }
                }
            }
        }

        private synchronized String checkDuplicate(String username) {
            while (true) {
                int nameCounter = 0;
                for (int i = 0; i < clients.size(); i++) {
                    if (clients.get(i).username.equals(username)) {
                        nameCounter++;
                        if (nameCounter == 2) {
                            username += "1";
                            clients.get(clients.size() - 1).username = username;
                        }
                    }
                }
                if (nameCounter < 2) {
                    break;
                }
            }
            return username;
        }

        private void close() {
            try {
                sInput.close();
                sOutput.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } //DOES THE EXACT SAME AS LOGGING OUT IN THE CHATCLIENT CLASS
    }
}
