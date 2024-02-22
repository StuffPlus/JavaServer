import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.*;

public class Server {
    private int port;
    private ServerSocket serverSocket;
    private ExecutorService pool;
    private Map<String, ClientHandler> clients;
    private String coordinatorId;

    public Server(int port) {
        this.port = port;
        this.pool = Executors.newCachedThreadPool();
        this.clients = new ConcurrentHashMap<>();
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(clientSocket, this);
            pool.execute(clientHandler);
        }
    }

    public synchronized void registerClient(String clientId, ClientHandler clientHandler) {
        if (clients.isEmpty()) {
            coordinatorId = clientId;
            clientHandler.setCoordinator(true);
        }
        clients.put(clientId, clientHandler);
        System.out.println("Client " + clientId + " registered. Total clients: " + clients.size());
    }

    public synchronized void unregisterClient(String clientId) {
        clients.remove(clientId);
        System.out.println("Client " + clientId + " unregistered. Total clients: " + clients.size());
        if (clientId.equals(coordinatorId)) {
            assignNewCoordinator();
        }
    }

    private void assignNewCoordinator() {
        if (!clients.isEmpty()) {
            String newCoordinatorId = clients.keySet().iterator().next();
            coordinatorId = newCoordinatorId;
            clients.get(newCoordinatorId).setCoordinator(true);
            System.out.println("New coordinator assigned: " + newCoordinatorId);
        }
    }

    public synchronized void forwardMessage(String message, String senderId) {
        for (Map.Entry<String, ClientHandler> clientEntry : clients.entrySet()) {
            if (!clientEntry.getKey().equals(senderId)) {
                clientEntry.getValue().sendMessage(message);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 12345;
        Server server = new Server(port);
        server.start();
    }
}

