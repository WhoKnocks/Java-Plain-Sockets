import org.junit.Assert;
import org.junit.Test;

public class TCPClientTest {



    @Test
    public void testGetHTTPType() throws Exception {
        String type = HTTPUtilities.getHTTPType("GET / HTTP/1.1");
        Assert.assertEquals("1.1", type);
        type = HTTPUtilities.getHTTPType("Get / HTTP/1.0");
        Assert.assertEquals("1.0", type);
    }

    @Test
    public void testGetHTTPCommand() throws Exception {
        String type = HTTPUtilities.getHTTPCommand("GET / HTTP/1.1");
        Assert.assertEquals("GET", type);

        type = HTTPUtilities.getHTTPCommand("POST / HTTP/1.1");
        Assert.assertEquals("POST", type);
    }
}