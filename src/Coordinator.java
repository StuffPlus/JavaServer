package src;
public class Coordinator {
    private static ClientHandler currentCoordinator;

    public static synchronized void setCoordinator(ClientHandler clientHandler) {
        currentCoordinator = clientHandler;
    }

    public static synchronized ClientHandler getCurrentCoordinator() {
        return currentCoordinator;
    }
}
