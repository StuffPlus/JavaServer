package Tests;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Random;
import src.*;
import org.junit.*;


public class serverTest{

    private String host = "localhost";
    Random rn = new Random();
    int n = rn.nextInt(10000);
    private int port = n;
    private Map<String, ClientHandler> clients;
    @Before
    public void startServerTest() throws IOException{
        Server server = new Server(port);
        server.start(); // create a server

    }

    @Test
    public void addTestClient() throws IOException{
       
        String username = "testito";
        Socket socket = new Socket(host, port);
        Client client = new Client(socket, username);
        assertEquals(1,clients.size()); // check the size of the clients
    }

    @Test
    public void assignNewCoordinatorTest() throws IOException{
        Socket socket = new Socket(host, port);
        Client client = new Client(socket, "Test");
        if (!clients.isEmpty()) {
            String newCoordinatorId = clients.keySet().iterator().next(); // get the next user in the map
            clients.get(newCoordinatorId).setCoordinator(true); // set the user to the coordinator
            assertEquals(true, clients.get(newCoordinatorId)); // check if the flag to set the coordinator is true

            
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
            }else{
                assertFalse(false);
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
                assertTrue(true);
            }else{
                assertFalse(false);
            }
        }
    }
    
    @Test
    public void unregisterClientTest() throws IOException{
        Socket socket5 = new Socket(host, port);
        Client client6 = new Client(socket5, "Test6");
        clients.remove("Test6");
        assertEquals(0,clients.size()); // check the size of the map
        
    }
    @Test
    public void getMemberDetailsTest() throws IOException{
        Server server2 = new Server(1234);
        server2.start();

        StringBuilder details = new StringBuilder();
        details.append("MEMBER_DETAILS\n");
        for (Map.Entry<String, ClientHandler> entry : server2.getClients().entrySet()) {
            details.append(entry.getKey()).append(": ")
                   .append(entry.getValue().getClientIpAddress()).append(", ")
                   .append(entry.getValue().getClientPort()).append("\n"); 
        }
        assertEquals(false, details.isEmpty()); // check if the clients have been added
    }
        
}