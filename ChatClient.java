import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private String username;

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.start();
    }

    public void start() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {
            
            // Read server messages in a separate thread
            new Thread(new ServerMessageHandler(in)).start();
            
            // Handle user input
            while (true) {
                String input = scanner.nextLine();
                out.println(input);
                
                if (input.equalsIgnoreCase("/quit")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error in client: " + e.getMessage());
        }
    }

    private class ServerMessageHandler implements Runnable {
        private BufferedReader in;

        public ServerMessageHandler(BufferedReader in) {
            this.in = in;
        }

        @Override
        public void run() {
            try {
                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    System.out.println(serverMessage);
                }
            } catch (IOException e) {
                System.err.println("Error reading from server: " + e.getMessage());
            }
        }
    }
}