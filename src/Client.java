package src;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {   
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;

            // Send JOIN message to server
            sendJoinMessage();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void sendJoinMessage() {
        try {
            bufferedWriter.write("JOIN " + username);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            // Wait for server response on username availability
            String serverResponse = bufferedReader.readLine();
            if ("Username Taken".equals(serverResponse)) {
                System.out.println("Username already taken. Please restart the application and choose a different unique username.");
                // Close resources and exit or prompt for new username again
                closeEverything(socket, bufferedReader, bufferedWriter);
                System.exit(1); // For simplicity, exiting. Implement retry logic as needed.
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage() {
        try {
            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                if (messageToSend.equalsIgnoreCase("exit")) {
                    break;
                } else if (messageToSend.equalsIgnoreCase("REQUEST_DETAILS")) {
                    requestMemberDetails();
                } else {
                    bufferedWriter.write(username + ": " + messageToSend);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            }
            scanner.close();
            closeEverything(socket, bufferedReader, bufferedWriter);
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void requestMemberDetails() {
        try {
            bufferedWriter.write("REQUEST_DETAILS");
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMessage() {
        new Thread(() -> {
            String messageFromGroupChat;

            while (socket.isConnected()) {
                try {
                    messageFromGroupChat = bufferedReader.readLine();
                    System.out.println(messageFromGroupChat);
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter ip address");
        String host = scanner.nextLine();
        System.out.println("Enter port number");
        int port = scanner.nextInt();
        System.out.println("Enter your username");
        String username = scanner.nextLine();
        username = scanner.nextLine();
        Socket socket = new Socket(host, port);
        Client client = new Client(socket, username);
        System.out.println("You have connected to the server");
        client.listenForMessage();
        client.sendMessage();
        scanner.close();
    }
}