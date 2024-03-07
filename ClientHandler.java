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

    public ClientHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.isCoordinator = false;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("JOIN")) {
                    clientId = line.substring(5);
                    server.registerClient(clientId, this);

                    // Notify the client about the coordinator
                    sendCoordinatorInfo();

                    out.println("Welcome to the chat, " + clientId);
                    printClientInfo();
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

    // Add this method to send coordinator information to new clients
    private void sendCoordinatorInfo() {
        if (server.getCoordinatorId() != null) {
            out.println("Coordinator " + server.getCoordinatorId());
        }
    }

    public void printClientInfo() {
        System.out.println("Client connected: " + clientId);
        System.out.println("IP Address: " + clientSocket.getInetAddress().getHostAddress());
        System.out.println("Port: " + clientSocket.getPort());
        System.out.println("Is Coordinator: " + isCoordinator);
    }

    public void sendMessage(String message) {
        out.println(message);
    }

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

    public void setCoordinator(boolean isCoordinator) {
        this.isCoordinator = isCoordinator;
    }

    public String getClientIpAddress() {
        return clientSocket.getInetAddress().getHostAddress();
    }

    public int getClientPort() {
        return clientSocket.getPort();
    }

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
