import java.util.ArrayList;
import java.net.Socket;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;


public class ClientHandler implements Runnable{
    //Defines a ClientHandler class that implements the Runnable interface, indicating it can be run in a separate thread.
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>(); //Defines a static ArrayList called clientHandlers to keep track of all client handlers.

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    //Declares instance variables for the Socket, BufferedReader, BufferedWriter, and clientUsername.
    public ClientHandler(Socket socket){ //Defines a constructor that initializes the Socket, BufferedReader, BufferedWriter, and clientUsername based on the client's input.
        try{
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();


            clientHandlers.add(this);
            broadcastMessage("Server: " + clientUsername + " has entered the chat"); //Adds the current instance of ClientHandler to the clientHandlers list and broadcasts a message to all clients that a new client has entered the chat.

        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    @Override
    public void run(){ //Implements the run() method required by the Runnable interface. This method continuously reads messages from the client and broadcasts them to all other clients.
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient); //Defines a broadcastMessage() method to send a message to all clients except the sender.
            }catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend){
        for(ClientHandler clientHandler : clientHandlers){
            try{
                if (!clientHandler.clientUsername.equals(clientUsername)){
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();

                } 
            }catch(IOException e){
                closeEverything(socket,bufferedReader,bufferedWriter);
            }
        }
    }
    
    public void removeClientHandler() { //Implements a removeClientHandler() method to remove the current client handler from the list and broadcast a message to inform other clients that this client has left the chat.
        clientHandlers.remove(this);
        broadcastMessage("Server: " + clientUsername + " has left the chat");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){ //Defines a closeEverything() method to close the socket, buffered reader, and buffered writer associated with this client handler when it's done.
        removeClientHandler();
        try{
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null){
                socket.close();     
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}

