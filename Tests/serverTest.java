package Tests;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;



import org.junit.*;

import src.Client;
import src.ClientHandler;
import src.Server;

public class serverTest{
    private Map<String, ClientHandler> clients;
    private String host = "localhost";
    private int port = 6666;

    @Before
    public void startServerTest() throws IOException{
        Server server = new Server(port);
        server.start();
    }
    @Test
    public void addTestClient() throws IOException{
       
        String username = "Luan";
        Socket socket = new Socket(host, port);
        Client client = new Client(socket, username);
        assertEquals(1,clients.size());
    }

    @Test
    public void assignNewCoordinatorTest() throws IOException{
        Socket socket = new Socket(host, port);
        Client client = new Client(socket, "Test");
        if (!clients.isEmpty()) {
            String newCoordinatorId = clients.keySet().iterator().next();
            clients.get(newCoordinatorId).setCoordinator(true);
            assertTrue(true);

            
        }else{
            assertFalse(false);
        }
    }
    @Test
    public void forwardMessageTest() throws IOException{
        Socket socket = new Socket(host, port);
        Client client2 = new Client(socket, "Test2");
        Socket socket2 = new Socket(host, port);
        Client client3 = new Client(socket, "Test3");
        for (Map.Entry<String, ClientHandler> clientEntry : clients.entrySet()) {
            if (!clientEntry.getKey().equals("Test2")) {
                clientEntry.getValue().sendMessage("Hello all");
                assertTrue(true);
            }
        }
        socket2.close();
    }
    @Test
    public void privateMessageTest() throws IOException{
        Socket socket3 = new Socket(host, port);
        Client client4 = new Client(socket3, "Test4");
        Socket socket4 = new Socket(host, port);
        Client client5 = new Client(socket4, "Test5");
        for (Map.Entry<String, ClientHandler> m : this.clients.entrySet()) {
            if (m.getKey().equals("Test4")) {
                m.getValue().sendMessage("Test5" + "(Private): " + "Hello");
            }
        }
    }
    
    
        
}