// COMP1549 - Advanced Programming

// GROUP 67
// Group members:
// - Omith Chowdhury - 001236697
// - Daim Ahmed - 001223454
// - Mohammed Amiin Mohammed - 001223569
// - Tuong-Luan X Bach - 001232844
// - Zafer Ahmed - 001225733

// CODE FOR THE SERVER

package src;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    // This if for the server's listening port
    private int port;
    // The server socket that listens for incoming client connections
    private ServerSocket serverSocket;
    // A pool of threads for handling multiple client connections simultaneously
    private ExecutorService pool;
    // A thread-safe map to keep track of all connected clients and their handlers
    private Map<String, ClientHandler> clients;
    // The ID of the client who has been assigned as the coordinator
    private String coordinatorId;

    // Constructor: Sets up the server on a specific port
    public Server(int port) {
        this.port = port;
        // Creates a cached thread pool for handling client connections
        this.pool = Executors.newCachedThreadPool();
        // Initializes the clients map for keeping track of connected clients
        this.clients = new ConcurrentHashMap<>();
    }

    // Starts the server, making it listen for client connections
    public void start() throws IOException {
        // Creates a server socket bound to the specified port
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        // Continuously listens for new client connections
        while (true) {
            // Accepts an incoming connection from a client
            Socket clientSocket = serverSocket.accept();
            // Creates a handler for the connected client
            ClientHandler clientHandler = new ClientHandler(clientSocket, this);
            // Executes the client handler in a separate thread
            pool.execute(clientHandler);
        }
    }

    // Registers a new client with the server
    public synchronized boolean registerClient(String clientId, ClientHandler clientHandler) {
        // Checks if the clientId is already in use
        if (clients.containsKey(clientId)) {
            System.out.println("Username already taken. Please choose a different username.");
            return false; // Username is taken
        } else {
            // If this is the first client, assign them as the coordinator
            if (clients.isEmpty()) {
                coordinatorId = clientId;
                clientHandler.setCoordinator(true);
            }
            // Adds the client and their handler to the map
            clients.put(clientId, clientHandler);
            System.out.println("Client " + clientId + " registered. Total clients: " + clients.size());
            return true; // Successfully registered
        }
    }

    // Unregisters a client from the server
    public synchronized void unregisterClient(String clientId) {
        // Removes the client from the map
        clients.remove(clientId);
        System.out.println("Client " + clientId + " unregistered. Total clients: " + clients.size());

        // Notify other clients that this client has left
        String leaveMessage = "Client " + clientId + " has left the chat.";
        forwardMessage(leaveMessage, "Server");

        // If the leaving client was the coordinator, assign a new one
        if (clientId.equals(coordinatorId)) {
            assignNewCoordinator();
        }
    }

    // Assigns a new coordinator from the list of connected clients
    private void assignNewCoordinator() {
        if (!clients.isEmpty()) {
            // Picks the first client in the list as the new coordinator
            String newCoordinatorId = clients.keySet().iterator().next();
            coordinatorId = newCoordinatorId;
            clients.get(newCoordinatorId).setCoordinator(true);
            System.out.println("Client " + newCoordinatorId + " has become the new coordinator.");

            // Notify all clients about the new coordinator
            String newCoordinatorMessage = "Client " + newCoordinatorId + " has become the new coordinator.";
            forwardMessage(newCoordinatorMessage, "Server");
        }
    }

    // Forwards a message from one client to all others, or to a specific client for private messages
    public synchronized void forwardMessage(String message, String senderId) {
        // Checks if the message is a direct message (DM)
        if (message.contains("@DM")) {
            String[] str = message.split(" ");
            String msgNew = "";
            for (int i = 3; i < str.length; i++) {
                msgNew += str[i] + " ";
            }
            // Sends the private message
            privateMessage(msgNew, str[2], senderId);
        } else {
            // Broadcasts the message to all clients except the sender
            for (Map.Entry<String, ClientHandler> clientEntry : clients.entrySet()) {
                if (!clientEntry.getKey().equals(senderId)) {
                    clientEntry.getValue().sendMessage(message);
                }
            }
        }
    }

    void privateMessage(String msg, String nickName, String senderID) {
        for (Map.Entry<String, ClientHandler> m : this.clients.entrySet()) {
            if (m.getKey().equals(nickName)) {
                m.getValue().sendMessage(senderID + "(Private): " + msg);
            }
        }
    }

    // Getters for clients map and coordinator ID
    public Map<String, ClientHandler> getClients() {
        return clients;
    }

    public String getCoordinatorId() {
        return coordinatorId;
    }

    // Stops the server, shutting down the thread pool and closing the server socket
    public void stop() throws IOException {
        pool.shutdown();
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    // Main method to run the server
    public static void main(String[] args) throws IOException {
        int port = 12345; // Sets the server port
        Server server = new Server(port);
        server.start(); // Starts the server
    }
}
