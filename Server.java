import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server{

    private ServerSocket serverSocket;
    private boolean isCoordinatorAssigned = false;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer(){ //Inside startServer(), it enters a loop to continuously accept client connections.
        try{
            while(!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("A new client has joined"); //When a client connects, it prints a message and creates a ClientHandler instance to handle the client's requests.
                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler); //Creates a new thread for each client and starts it.
                thread.start();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    } 

    public void closeServerSocket(){ //Defines a method closeServerSocke() to close the server socket.
        try{ //Inside closeServerSocke(), it checks if the server socket is not null and closes it if it's open
            if (serverSocket != null){
                serverSocket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException { //In the main method, it creates a ServerSocket instance listening on port 1234, creates a Server instance, and starts the server
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}

    