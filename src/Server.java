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
    private int port;
    private ServerSocket serverSocket;
    private ExecutorService pool;
    private Map<String, ClientHandler> clients;
    private String coordinatorId;

    // Constructor for the Server class
    // Initializes the port, thread pool, and client map
    public Server(int port) {
        this.port = port;
        this.pool = Executors.newCachedThreadPool();
        this.clients = new ConcurrentHashMap<>();
    }

    // Starts the server and listens for client connections
    // Creates a ClientHandler for each connected client and submits it to the thread pool
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(clientSocket, this);
            pool.execute(clientHandler);
        }
    }

    // Registers a client with the server
    // Assigns the first registered client as the coordinator
    public synchronized boolean registerClient(String clientId, ClientHandler clientHandler) {
        if (clients.containsKey(clientId)) {
            System.out.println("Username already taken. Please choose a different username.");
            return false; // Username is taken
        } else {
            if (clients.isEmpty()) {
                coordinatorId = clientId;
                clientHandler.setCoordinator(true);
            }
            clients.put(clientId, clientHandler);
            System.out.println("Client " + clientId + " registered. Total clients: " + clients.size());
            return true; // Successfully registered
        }
    }

    // Unregisters a client from the server
    // Notifies other clients about the client's departure and assigns a new coordinator if necessary
    public synchronized void unregisterClient(String clientId) {
        if (clients.containsKey(clientId)) {
            clients.remove(clientId);
            System.out.println("Client " + clientId + " unregistered. Total clients: " + clients.size());

            // Notify other clients that this client has left
            String leaveMessage = "Client " + clientId + " has left the chat.";
            forwardMessage(leaveMessage, "Server");

            if (clientId.equals(coordinatorId)) {
                assignNewCoordinator();
            }
        }
    }

    // Assigns a new coordinator when the current coordinator leaves
    // Selects the next available client as the new coordinator and notifies all clients
    private void assignNewCoordinator() {
        if (!clients.isEmpty()) {
            String newCoordinatorId = clients.keySet().iterator().next();
            coordinatorId = newCoordinatorId;
            clients.get(newCoordinatorId).setCoordinator(true);
            System.out.println("Client " + newCoordinatorId + " has become the new coordinator.");

            // Notify all clients about the new coordinator
            String newCoordinatorMessage = "Client " + newCoordinatorId + " has become the new coordinator.";
            forwardMessage(newCoordinatorMessage, "Server");
        }
    }

    // Forwards a message to all connected clients, except the sender
    // Checks if the message is a direct message and handles it accordingly
    public synchronized void forwardMessage(String message, String senderId) {
        // Assuming "@dm" is the indicator for a direct message
        if (message.startsWith("@dm")) {
            String[] parts = message.split(" ", 3);
            if (parts.length >= 3) {
                String recipient = parts[1];
                String msg = parts[2];
                privateMessage(msg, recipient, senderId);
            }
        } else {
            for (Map.Entry<String, ClientHandler> clientEntry : clients.entrySet()) {
                if (!clientEntry.getKey().equals(senderId)) {
                    clientEntry.getValue().sendMessage(message);
                }
            }
        }
    }

    // Sends a private message to a specific recipient
    // If the recipient is not found, sends an error message to the sender
    void privateMessage(String msg, String nickName, String senderID) {
        ClientHandler senderHandler = clients.get(senderID);
        for (Map.Entry<String, ClientHandler> m : this.clients.entrySet()) {
            if (m.getKey().equals(nickName)) {
                String msgForRecipient = String.format("PRIVATE_MESSAGE %s %s", nickName, msg);
                m.getValue().sendMessage(msgForRecipient);
                
                if (senderHandler != null) {
                    String msgForSender = String.format("PRIVATE_MESSAGE %s %s", senderID, msg);
                    senderHandler.sendMessage(msgForSender);
                }
                return;
            }
        }
        if (senderHandler != null) {
            senderHandler.sendMessage("User " + nickName + " not found. Please check the username and try again.");
        }
    }

    // Retrieves the map of connected clients
    // Returns the clients map
    public Map<String, ClientHandler> getClients() {
        return clients;
    }

    // Retrieves the ID of the current coordinator
    // Returns the coordinatorId
    public String getCoordinatorId() {
        return coordinatorId;
    }

    // Stops the server and closes the server socket
    // Used for testing purposes to gracefully shut down the server
    public void stop() throws IOException {
        pool.shutdown();
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    // Main method to start the server
    // Creates a Server instance and starts it on the specified port
    public static void main(String[] args) throws IOException {
        int port = 12345;
        Server server = new Server(port);
        server.start();
    }
}
