import java.io.*;
import java.net.*;
import java.util.*;



public class ChatServer {
    private static final int PORT = 12345;
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Chat Server is running...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket);
                
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Error in the server: " + e.getMessage());
        }
    }
    
    public static void broadcast(String message, ClientHandler excludeClient) {
        for (ClientHandler client : clientHandlers) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }
    
    public static void removeClient(ClientHandler client) {
        clientHandlers.remove(client);
        System.out.println("Client disconnected. Total clients: " + clientHandlers.size());
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Get username
            out.println("Enter your username:");
            username = in.readLine();
            System.out.println(username + " has joined the chat.");
            ChatServer.broadcast(username + " has joined the chat.", this);
            
            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                if (clientMessage.equalsIgnoreCase("/quit")) {
                    break;
                }
                System.out.println(username + ": " + clientMessage);
                ChatServer.broadcast(username + ": " + clientMessage, this);
            }
        } catch (IOException e) {
            System.err.println("Error with client " + username + ": " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
            ChatServer.removeClient(this);
            ChatServer.broadcast(username + " has left the chat.", this);
            System.out.println(username + " has disconnected.");
        }
    }
    
    public void sendMessage(String message) {
        out.println(message);
    }
}