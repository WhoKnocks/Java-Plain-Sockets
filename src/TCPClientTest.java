import junit.framework.TestCase;
import org.junit.Test;

public class TCPClientTest extends TestCase {

    TCPClient tcpClient = new TCPClient("www.example.com", 80);

    @Test
    public void testGetHTTPType() throws Exception {
        String type = tcpClient.getHTTPType("GET / HTTP/1.1");
        assertEquals("1.1", type);
        type = tcpClient.getHTTPType("Get / HTTP/1.0");
        assertEquals("1.0", type);
    }

    @Test
    public void testGetHTTPCommand() throws Exception {
        String type = tcpClient.getHTTPCommand("GET / HTTP/1.1");
        assertEquals("GET", type);

        type = tcpClient.getHTTPCommand("POST / HTTP/1.1");
        assertEquals("POST", type);
    }
}