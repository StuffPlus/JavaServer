import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServerClientTest {
    private Server server;
    private Socket clientSocket;

    @Before
    public void setUp() throws IOException {
        // Start the server
        server = new Server(5555);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Connect a client to the server
        clientSocket = new Socket("localhost", 5555);
    }

    @After
    public void tearDown() throws IOException {
        // Close the client socket
        clientSocket.close();
        // Stop the server
        server.stop();
    }

    @Test
    public void testClientRegistration() throws IOException {
        // Mock client registration
        InputStream inputStream = new ByteArrayInputStream("JOIN Alice".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        Socket mockClientSocket = new Socket("localhost", 5555);
        ClientHandler clientHandler = new ClientHandler(mockClientSocket, server);
        clientHandler.run();
        assertEquals(1, server.getClients().size());
    }

    @Test
    public void testMessageExchange() throws IOException {
        // Mock message exchange between client and server
        InputStream inputStream = new ByteArrayInputStream("JOIN Alice".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        Socket mockClientSocket = new Socket("localhost", 5555);
        ClientHandler clientHandler = new ClientHandler(mockClientSocket, server);
        clientHandler.run();
    
        String testMessage = "Hello, World!";
        clientHandler.sendMessage(testMessage);
    }
    

    @Test
    public void testClientDisconnect() throws IOException {
        // Mock client disconnection
        InputStream inputStream = new ByteArrayInputStream("JOIN Alice".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        Socket mockClientSocket = new Socket("localhost", 5555);
        ClientHandler clientHandler = new ClientHandler(mockClientSocket, server);
        clientHandler.run();
        clientHandler.closeResources();
        assertEquals(0, server.getClients().size());
    }
}
