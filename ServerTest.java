import java.io.IOException;
import java.net.Socket;

import org.junit.Test;

public class serverTest{
    
    @Test
    public void startServerTest() throws IOException{
        int port = 1234;
        Server server = new Server(port);
        server.start();
        addTestClient("Luan");    
        addTestClient("Omith");
        addTestClient("Mo");
    }

    public void addTestClient(String username) throws IOException{
        String host = "localhost";
        int port = 1234;

        Socket socket = new Socket(host, port);
        Client client = new Client(socket, username);
    }
        
}