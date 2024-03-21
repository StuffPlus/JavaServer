import java.io.IOException;
import java.net.Socket;

import org.junit.*;

public class ClientTest{
    @Test 
    public void test() throws IOException{ 
        
        Socket socket = new Socket("localhost", 1234);
        Client client = new Client(socket, "Luan");
        
        
    }
    
}
    