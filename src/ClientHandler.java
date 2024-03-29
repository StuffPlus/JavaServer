// COMP1549 - Advanced Programming

// GROUP 67
// Group members:
// - Omith Chowdhury - 001236697
// - Daim Ahmed - 001223454
// - Mohammed Amiin Mohammed - 001223569
// - Tuong-Luan X Bach - 001232844
// - Zafer Ahmed - 001225733

// CODE FOR HANDLING THE CLIENT
package src;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private Server server;
    private PrintWriter out;
    private BufferedReader in;
    private String clientId;
    private boolean isCoordinator;

    // Constructor for the ClientHandler class
    // Initializes the client socket and server references
    public ClientHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.isCoordinator = false;
    }

    // Main method that runs when the thread is started
    // Handles client communication and message forwarding
    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String line;
            while ((line = in.readLine()) != null) {
                if (line.equalsIgnoreCase("QUIT")) {
                    server.unregisterClient(clientId);
                    closeResources();
                    break; // Exit the loop
                } else if (line.startsWith("JOIN")) {
                    clientId = line.substring(5);
                    boolean registered = server.registerClient(clientId, this);
                    if (!registered) {
                        out.println("Username Taken"); // Inform the client
                        break; // Exit the loop or handle as needed
                    } else {
                        out.println("Welcome to the chat, " + clientId); // Existing welcome message
                        sendCoordinatorInfo();// Notify the client about the coordinator
                        printClientInfo();
                    }
                    
                } else if (line.equalsIgnoreCase("REQUEST_DETAILS")) {
                    // Handle request for member details
                    sendMemberDetails();
                } else {
                    server.forwardMessage(line, clientId);
                }
            }
        } catch (IOException e) {
            System.out.println("Exception in ClientHandler for client " + clientId + ": " + e.getMessage());
        } finally {
            server.unregisterClient(clientId);
            closeResources();
        }
    }

    // Sends coordinator information to new clients
    // Informs the client about the current coordinator, if any
    private void sendCoordinatorInfo() {
        if (server.getCoordinatorId() != null) {
            out.println("Coordinator " + server.getCoordinatorId());
        }
    }

    // Prints client information to the console
    // Displays the client ID, IP address, port, and coordinator status
    public void printClientInfo() {
        System.out.println("Client connected: " + clientId);
        System.out.println("IP Address: " + clientSocket.getInetAddress().getHostAddress());
        System.out.println("Port: " + clientSocket.getPort());
        System.out.println("Is Coordinator: " + isCoordinator);
    }

    // Sends a message to the client
    // Writes the message to the client's output stream
    public void sendMessage(String message) {
        out.println(message);
    }

    // Sends member details to the client
    // Retrieves the member information from the server and sends it to the client
    private void sendMemberDetails() {
        StringBuilder details = new StringBuilder();
        details.append("MEMBER_DETAILS\n");
        for (Map.Entry<String, ClientHandler> entry : server.getClients().entrySet()) {
            details.append(entry.getKey()).append(": ")
                   .append(entry.getValue().getClientIpAddress()).append(", ")
                   .append(entry.getValue().getClientPort()).append("\n");
        }
        out.println(details.toString());
    }

    // Sets the coordinator status of the client
    // Indicates whether the client is the coordinator or not
    public void setCoordinator(boolean isCoordinator) {
        this.isCoordinator = isCoordinator;
    }

    // Retrieves the IP address of the client
    // Returns the IP address as a string
    public String getClientIpAddress() {
        return clientSocket.getInetAddress().getHostAddress();
    }

    // Retrieves the port number of the client
    // Returns the port number as an integer
    public int getClientPort() {
        return clientSocket.getPort();
    }

    // Closes the resources associated with the client
    // Closes the input stream, output stream, and client socket
    public void closeResources() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing resources for client " + clientId);
        }
    }
}