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

    public synchronized boolean registerClient(String clientId, ClientHandler clientHandler) {
        if (clients.containsKey(clientId)) {
            System.out.println("Username already taken. Please choose a different username.");
            return false; // Username is taken
        }else { 
        
            if (clients.isEmpty()) {
                coordinatorId = clientId;
                clientHandler.setCoordinator(true);
            }
            clients.put(clientId, clientHandler);
            System.out.println("Client " + clientId + " registered. Total clients: " + clients.size());
            return true; // Successfully registered
        }
    }

    public synchronized void unregisterClient(String clientId) {
        clients.remove(clientId);
        System.out.println("Client " + clientId + " unregistered. Total clients: " + clients.size());

        // Notify other clients that this client has left
        String leaveMessage = "Client " + clientId + " has left the chat.";
        forwardMessage(leaveMessage, "Server");

        if (clientId.equals(coordinatorId)) {
            assignNewCoordinator();
        }
    }

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

    public synchronized void forwardMessage(String message, String senderId) {
        if (message.contains("@DM")) {
            String[] str = message.split(" ");
            String msgNew = "";
            for (int i = 3; i < str.length; i++) {
                msgNew += str[i];
                msgNew += " ";
            }
            System.out.println(str[2]);
            privateMessage(msgNew, str[2], senderId);
        } else {
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

    public Map<String, ClientHandler> getClients() {
        return clients;
    }

    public String getCoordinatorId() {
        return coordinatorId;
    }

    public static void main(String[] args) throws IOException {
        int port = 12345;
        Server server = new Server(port);
        server.start();
    }
}
