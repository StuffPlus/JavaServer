// COMP1549 - Advanced Programming

// GROUP 67
// Group members:
// - Omith Chowdhury - 001236697
// - Daim Ahmed - 001223454
// - Mohammed Amiin Mohammed - 001223569
// - Tuong-Luan X Bach - 001232844
// - Zafer Ahmed - 001225733

// CODE FOR THE CLIENTS OF THE APPLICATION

package src;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    // Socket to communicate with the server
    private Socket socket;
    // Reader to read the messages from the server
    private BufferedReader bufferedReader;
    // Writer to send messages to the server
    private BufferedWriter bufferedWriter;
    // This client's username
    private String username;

    // This is for the Graphical User Interface of the chat application
    private ClientGUI clientGUI;

    // Constructor: Sets up the client with a connection to the server and a username
    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            // Set up the writer and reader for the socket streams
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;

            // This starts the GUI
            this.clientGUI = new ClientGUI(this,username);

            // This is to join the chat with the chosen username
            sendJoinMessage();
        } catch (IOException e) {
            // If any IO operations fail, close all connections cleanly
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    // Sends a message to the server requesting to join the chat with the provided username
    private void sendJoinMessage() {
        try {
            bufferedWriter.write("JOIN " + username);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            // Read the server's response to the join request
            String serverResponse = bufferedReader.readLine();
            if ("Username Taken".equals(serverResponse)) {
                System.out.println("Username already taken. Please restart the application and choose a different unique username.");
                // If the username is taken, close connections and exit
                closeEverything(socket, bufferedReader, bufferedWriter);
                System.exit(1); // Exiting, but a retry logic could be implemented here
            }
        } catch (IOException e) {
            // If sending the message or reading the response fails, close all connections
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    // Allows the user to send messages to the chat until "exit" is entered
    public void sendMessage() {
        try {
            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                if (messageToSend.equalsIgnoreCase("exit")) {
                    break; // Exit the loop if "exit" is entered
                } else if (messageToSend.equalsIgnoreCase("REQUEST_DETAILS")) {
                    // Special command to request details from the server
                    requestMemberDetails();
                } else {
                    // Send any other message to the chat
                    bufferedWriter.write(username + ": " + messageToSend);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            }
            scanner.close();
            // Close connections after finishing sending messages
            closeEverything(socket, bufferedReader, bufferedWriter);
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    // Sends a special request to the server for member details
    private void requestMemberDetails() {
        try {
            bufferedWriter.write("REQUEST_DETAILS");
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            // If the request fails, close all connections
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    // Starts a new thread that listens for messages from the server and prints them
    public void listenForMessage() {
        new Thread(() -> {
            String messageFromGroupChat;

            while (socket.isConnected()) {
                try {
                    // Read messages from the server and print them
                    messageFromGroupChat = bufferedReader.readLine();
                    System.out.println(messageFromGroupChat);
                } catch (IOException e) {
                    // If reading messages fails, close all connections
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }).start();
    }

    // Closes the socket and IO streams cleanly
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Main method to run the client
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter IP address:");
        String host = scanner.nextLine();
        System.out.println("Enter port number:");
        int port = Integer.parseInt(scanner.nextLine());
        System.out.println("Enter your username:");
        String username = scanner.nextLine();
        scanner.close();
        try {
            Socket socket = new Socket(host, port);
            Client client = new Client(socket, username);
        } catch (IOException e) {
            System.out.println("Unable to connect to server. Please check the IP address and port number.");
            e.printStackTrace();
        }
    }
}