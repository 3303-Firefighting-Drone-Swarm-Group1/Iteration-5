import org.junit.Test;
import static org.junit.Assert.*;

public class RPCClientTest {

    @Test
    public void testSendRequest() {
        int testPort = 7001;
        // Create a dummy Scheduler on testPort that echoes the request.
        Scheduler dummyScheduler = new Scheduler(testPort) {
            @Override
            public Object handleRequest(Object request) {
                return request;
            }
        };

        // Allow time for the RPC server to start.
        try { Thread.sleep(100); } catch (InterruptedException e) { }

        // Create an RPCClient connecting to testPort.
        RPCClient client = new RPCClient("localhost", testPort);
        String testMessage = "Hello, RPC!";
        Object response = client.sendRequest(testMessage);
        assertEquals(testMessage, response);
    }
}
