import org.junit.Assert;
import org.junit.Test;

public class TCPClientTest {

    TCPClient tcpClient = new TCPClient("www.example.com", 80);

    @Test
    public void testGetHTTPType() throws Exception {
        String type = tcpClient.getHTTPType("GET / HTTP/1.1");
        Assert.assertEquals("1.1", type);
        type = tcpClient.getHTTPType("Get / HTTP/1.0");
        Assert.assertEquals("1.0", type);
    }

    @Test
    public void testGetHTTPCommand() throws Exception {
        String type = tcpClient.getHTTPCommand("GET / HTTP/1.1");
        Assert.assertEquals("GET", type);

        type = tcpClient.getHTTPCommand("POST / HTTP/1.1");
        Assert.assertEquals("POST", type);
    }
}