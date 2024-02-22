import java.io.*;
import java.net.*;

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
                    out.println("Welcome to the chat, " + clientId);
                    printClientInfo();
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

    public void printClientInfo() {
        System.out.println("Client connected: " + clientId);
        System.out.println("IP Address: " + clientSocket.getInetAddress().getHostAddress());
        System.out.println("Port: " + clientSocket.getPort());
        System.out.println("Is Coordinator: " + isCoordinator);
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void setCoordinator(boolean isCoordinator) {
        this.isCoordinator = isCoordinator;
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

