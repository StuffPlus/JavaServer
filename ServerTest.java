import java.io.IOException;

import org.junit.Test;

public class ServerTest {
    @Test
    public void serverTest() throws IOException{
        int port = 1234;
        Server server = new Server(port);
        server.start();
    }

}
