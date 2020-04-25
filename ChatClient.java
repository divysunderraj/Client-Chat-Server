import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * This class controls the Client side of the Chat-Server
 *
 * @author Adhi Ramkumar & Divy Sunderraj
 * @version April 25 2020
 */
final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;

    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }


    /*
     * This starts the Chat Client
     */
    private boolean start() {
        // Create a socket
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            return false;
        }

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }


        // This thread will listen from the server for incoming messages
        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        // After starting, send the clients username to the server.
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) {
        // Get proper arguments and override defaults
        Scanner scanner = new Scanner(System.in);
        int portNumber;
        String serverName;
        String username;
        if (args.length == 1) {
            //only username given
            username = args[0];
            portNumber = 1500;
            serverName = "localhost";
        } else if (args.length == 2) {
            //username and port number given
            username = args[0];
            serverName = "localhost";
            portNumber = Integer.parseInt(args[1]);
        } else if (args.length == 3) {
            //username, port number, and server given
            username = args[0];
            portNumber = Integer.parseInt(args[1]);
            serverName = args[2];
        } else {
            //nothing given
            username = "Anonymous";
            portNumber = 1500;
            serverName = "localhost";
        }

        ChatClient client = new ChatClient(serverName, portNumber, username);

        // Create your client and start it
        if (client.start()) {
            while (true) {
                String message = scanner.nextLine();
                if (message.equals("/msg")) {
                    System.out.println("Please enter the name of the recipient");
                    String recipient = scanner.nextLine();
                    System.out.println("Please enter the message you want to say to them: ");
                    String directMessage = scanner.nextLine();
                    client.sendMessage(new ChatMessage(directMessage, 2, recipient));
                } else if (message.equals("/list")) {
                    client.sendMessage(new ChatMessage("/list", 2, null));
                } else if (message.equals("/logout")) {
                    client.sendMessage(new ChatMessage("/logout", 1, null));
                    break;
                } else {
                    client.sendMessage(new ChatMessage(message, 0, null));
                }
            }
            client.close();
        } else {
            System.out.println("The server is not running");
        }

        //client.sendMessage(new ChatMessage("Hello there", 0));
        //client.sendMessage(new ChatMessage("I go to iu", 0));

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

    /**
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     *
     * @author your name and section
     * @version date
     */
    private final class ListenFromServer implements Runnable {
        public void run() {
            while (true) {
                try {
                    String msg = (String) sInput.readObject();
                    System.out.println(msg);
                } catch (IOException | ClassNotFoundException e) {
                    close();
                    break;
                }
            } //end while
        }
    }
}
