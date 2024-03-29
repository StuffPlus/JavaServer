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
import java.io.*;
import java.net.*;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private ClientGUI clientGUI;

    // Constructor for the Client class
    // Initializes the socket, I/O streams, username, and GUI
    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
            this.clientGUI = new ClientGUI(this, username);
            sendJoinMessage();
            listenForMessage();
        } catch (IOException e) {
            closeEverything();
        }
    }

    // Sends a join message to the server with the client's username
    // Handles the server's response and closes the connection if the username is taken
    private void sendJoinMessage() {
        try {
            bufferedWriter.write("JOIN " + username);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            String serverResponse = bufferedReader.readLine();
            if ("Username Taken".equals(serverResponse)) {
                clientGUI.showErrorMessage("Username already taken. Please restart the application and choose a different unique username.");
                closeEverything();
                System.exit(1);
            }
        } catch (IOException e) {
            closeEverything();
        }
    }

    // Sends a message to the server based on the message type
    // Handles quit, request details, private messages, and public messages
    public void sendMessage(String messageToSend) {
        try {
            if (messageToSend.equalsIgnoreCase("QUIT")) {
                bufferedWriter.write("QUIT");
                bufferedWriter.newLine();
                bufferedWriter.flush();
                closeEverything();
            } else if (messageToSend.equalsIgnoreCase("REQUEST_DETAILS")) {
                requestMemberDetails();
            } else if (messageToSend.startsWith("@dm")) {
                sendPrivateMessage(messageToSend);
            } else {
                String formattedMessage = "(" + clientGUI.getFormattedTime() + ") " + username + ": " + messageToSend;
                bufferedWriter.write("PUBLIC_MESSAGE " + formattedMessage);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything();
        }
    }

    // Sends a private message to a specific recipient
    // Formats the message and sends it to the server
    private void sendPrivateMessage(String messageToSend) {
        String[] parts = messageToSend.split(" ", 3);
        if (parts.length == 3) {
            String recipient = parts[1];
            String message = parts[2];
            try {
                String formattedMessage = username + ": " + message;
                bufferedWriter.write("PRIVATE_MESSAGE " + recipient + " " + formattedMessage);
                bufferedWriter.newLine();
                bufferedWriter.flush();
                clientGUI.appendToMessages("(" + clientGUI.getFormattedTime() + ") (Private to " + recipient + "): " + message + "\n", true);
            } catch (IOException e) {
                closeEverything();
            }
        } else {
            clientGUI.appendToMessages("Invalid private message format. Use @dm (username) (message)\n", false);
        }
    }

    // Requests member details from the server
    // Sends a request to the server to retrieve member information
    private void requestMemberDetails() {
        try {
            bufferedWriter.write("REQUEST_DETAILS");
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything();
        }
    }

    // Sends the typing status of the client to the server
    // Indicates whether the client is currently typing or not
    public void sendTypingStatus(boolean isTyping) {
        try {
            bufferedWriter.write("TYPING " + username + " " + (isTyping ? "TRUE" : "FALSE"));
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything();
        }
    }

    // Listens for incoming messages from the server
    // Handles the received messages and updates the GUI accordingly
    public void listenForMessage() {
        new Thread(() -> {
            String messageFromGroupChat;

            while (!socket.isClosed()) {
                try {
                    messageFromGroupChat = bufferedReader.readLine();
                    if (messageFromGroupChat != null) {
                        clientGUI.handleMessage(messageFromGroupChat);
                    }
                } catch (IOException e) {
                    closeEverything();
                    break;
                }
            }
        }).start();
    }

    // Closes all the resources (streams and socket) and the GUI window
    // Ensures proper cleanup when the client is shutting down
    public void closeEverything() {
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null && !socket.isClosed()) socket.close();
            clientGUI.closeWindow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Main method to start the client application
    // Prompts the user for the server's IP address, port number, and username
    public static void main(String[] args) {
        String host = ClientGUI.showInputDialog("Enter IP address:");
        String portString = ClientGUI.showInputDialog("Enter port number:");
        String username = ClientGUI.showInputDialog("Enter your username:");

        if (host != null && portString != null && username != null && !username.trim().isEmpty()) {
            try {
                int port = Integer.parseInt(portString);
                Socket socket = new Socket(host, port);
                new Client(socket, username);
            } catch (NumberFormatException e) {
                ClientGUI.showErrorMessage("Invalid port number.");
            } catch (IOException e) {
                e.printStackTrace();
                ClientGUI.showErrorMessage("Cannot connect to the server, try again later.");
            }
        } else {
            ClientGUI.showErrorMessage("IP address, port number, and username are required to join the chat.");
        }
    }
}