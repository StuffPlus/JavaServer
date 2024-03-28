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
    // Socket to communicate with the server
    private Socket socket;
    // Reader to read the messages from the server
    private BufferedReader bufferedReader;
    // Writer to send messages to the server
    private BufferedWriter bufferedWriter;
    // This is client's username
    private String username;
    // This is for the Graphical User Interface of the chat application
    private ClientGUI clientGUI;

    // Constructor: Sets up the client with a connection to the server and a username
    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
            // This sarts the GUI
            this.clientGUI = new ClientGUI(this, username);
            // To join the chat
            sendJoinMessage();
            // Listening for incoming messages from the server
            listenForMessage();
        } catch (IOException e) {
            // If any IO operations fail, closes all connections cleanly
            closeEverything();
        }
    }

    // Handles the initial JOIN message to the server
    private void sendJoinMessage() {
        try {
            bufferedWriter.write("JOIN " + username);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            // Reading server's response to the join request
            String serverResponse = bufferedReader.readLine();
            if ("Username Taken".equals(serverResponse)) {
                // Informs user if username is taken and close the application
                clientGUI.showErrorMessage("Username already taken. Please restart the application and choose a different unique username.");
                closeEverything();
                System.exit(1); // Exiting the application
            }
        } catch (IOException e) {
            closeEverything();
        }
    }

    // Sends a message to the server requesting to join the chat with the provided username
    public void sendMessage(String messageToSend) {
        try {
            if (messageToSend.equalsIgnoreCase("QUIT")) {
                // If user decides to quit
                bufferedWriter.write("QUIT");
                bufferedWriter.newLine();
                bufferedWriter.flush();
                closeEverything();
            } else if (messageToSend.equalsIgnoreCase("REQUEST_DETAILS")) {
                // Special command to request details from the server
                requestMemberDetails();
            } else if (messageToSend.startsWith("@dm")) {
                // Handling private messages
                sendPrivateMessage(messageToSend);
            } else {
                // Sending a public message with a timestamp
                String formattedMessage = "(" + clientGUI.getFormattedTime() + ") " + username + ": " + messageToSend;
                bufferedWriter.write("PUBLIC_MESSAGE " + formattedMessage);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything();
        }
    }

    // Handles sending private messages to specific users
    private void sendPrivateMessage(String messageToSend) {
        String[] parts = messageToSend.split(" ", 3);
        if (parts.length == 3) {
            String recipient = parts[1];
            String message = parts[2];
            try {
                // Formatting the private message before sending
                String formattedMessage = "(" + clientGUI.getFormattedTime() + ") " + username + " (Private): " + message;
                bufferedWriter.write("PRIVATE_MESSAGE " + recipient + " " + formattedMessage);
                bufferedWriter.newLine();
                bufferedWriter.flush();
                // Displaying the message in the GUI
                clientGUI.appendToMessages(formattedMessage + "\n", true);
            } catch (IOException e) {
                closeEverything();
            }
        } else {
            // Error handling for invalid private message command format
            clientGUI.appendToMessages("Invalid command format. Use @dm (user name) (message) to send a private message.\n", false);
        }
    }

    // Sends a request for member details to the server and coordinator
    private void requestMemberDetails() {
        try {
            bufferedWriter.write("REQUEST_DETAILS");
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything();
        }
    }

    //feature to send typing status to the server
    public void sendTypingStatus(boolean isTyping) {
        try {
// Notifying the server of the user's typing status
            bufferedWriter.write("TYPING " + username + " " + (isTyping ? "TRUE" : "FALSE"));
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything();
        }
    }

    // Listening for messages from the server and handling them in the GUI
    public void listenForMessage() {
        new Thread(() -> {
            String messageFromGroupChat;

            while (!socket.isClosed()) {
                try {
                    // Read messages from the server and handle them via the GUI
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

    // Closes the socket and IO streams cleanly, also closes the GUI window
    public void closeEverything() {
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null && !socket.isClosed()) socket.close();
            // Closing the GUI window upon disconnection
            clientGUI.closeWindow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Main method to use GUI dialogs for inputs
    public static void main(String[] args) {
        // Using GUI dialogs for getting connection details and username
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

