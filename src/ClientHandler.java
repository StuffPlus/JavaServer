// COMP1549 - Advanced Programming

// GROUP 67
// Group members:
// - Omith Chowdhury - 001236697
// - Daim Ahmed - 001223454
// - Mohammed Amiin Mohammed - 001223569
// - Toung-Luan X Bach - 001232844
// - Zafer Ahmed - 001225733

// CODE FOR HANDLING THE CLIENT

package src;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

// Class that handles individual client connections to the server
public class ClientHandler implements Runnable {
    private Socket clientSocket; // Socket connection to the client
    private Server server; // Reference to the main server
    private PrintWriter out; // For sending messages to the client
    private BufferedReader in; // For receiving messages from the client
    private String clientId; // Stores the client's username
    private boolean isCoordinator; // Flag to indicate if this client is the coordinator

    // Constructor: Initializes the client handler with a client socket and server reference
    public ClientHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.isCoordinator = false; // By default, a client is not the coordinator
    }

    // The main logic of handling client interactions runs in this method
    @Override
    public void run() {
        try {
            // Setup output and input streams for communicating with the client
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String line;
            // Continuously listen for messages from the client
            while ((line = in.readLine()) != null) {
                if (line.startsWith("JOIN")) { // Client sends JOIN <username> to register
                    clientId = line.substring(5); // Extract username
                    // Attempt to register the client with the server
                    boolean registered = server.registerClient(clientId, this);
                    if (!registered) {
                        out.println("Username Taken"); // Inform the client if the username is taken
                        break; // Exit the loop if registration failed
                    } else {
                        out.println("Welcome to the chat, " + clientId); // Welcome the newly registered client
                        sendCoordinatorInfo(); // Send info about the chat coordinator
                        printClientInfo(); // Print client connection details to the server console
                    }
                } else if (line.equalsIgnoreCase("REQUEST_DETAILS")) { // Client requests member details
                    sendMemberDetails(); // Send the list of members to the client
                } else {
                    // Forward any other message to all clients
                    server.forwardMessage(line, clientId);
                }
            }
        } catch (IOException e) {
            System.out.println("Exception in ClientHandler for client " + clientId + ": " + e.getMessage());
        } finally {
            // Clean up resources and unregister client when connection is lost or closed
            server.unregisterClient(clientId);
            closeResources();
        }
    }

    // Sends information about the current coordinator to the newly connected client
    private void sendCoordinatorInfo() {
        if (server.getCoordinatorId() != null) { // Check if there is a coordinator
            out.println("Coordinator " + server.getCoordinatorId()); // Send coordinator info
        }
    }

    // Logs basic information about the client connection to the server console
    public void printClientInfo() {
        System.out.println("Client connected: " + clientId);
        System.out.println("IP Address: " + clientSocket.getInetAddress().getHostAddress());
        System.out.println("Port: " + clientSocket.getPort());
        System.out.println("Is Coordinator: " + isCoordinator);
    }

    // Method to send a message to this client
    public void sendMessage(String message) {
        out.println(message);
    }

    // Sends details of all members in the chat to the requesting client
    private void sendMemberDetails() {
        StringBuilder details = new StringBuilder();
        details.append("MEMBER_DETAILS\n");
        // Loop through all clients and append their details
        for (Map.Entry<String, ClientHandler> entry : server.getClients().entrySet()) {
            details.append(entry.getKey()).append(": ")
                    .append(entry.getValue().getClientIpAddress()).append(", ")
                    .append(entry.getValue().getClientPort()).append("\n");
        }
        out.println(details.toString()); // Send the compiled details to the client
    }

    // Setter method to update the coordinator status of this client
    public void setCoordinator(boolean isCoordinator) {
        this.isCoordinator = isCoordinator;
    }

    // Utility method to get this client's IP address
    public String getClientIpAddress() {
        return clientSocket.getInetAddress().getHostAddress();
    }

    // Utility method to get this client's port number
    public int getClientPort() {
        return clientSocket.getPort();
    }

    // Cleans up resources by closing I/O streams and the socket when the client disconnects or when an error occurs
    public void closeResources() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) client
