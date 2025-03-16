import org.junit.Test;
import static org.junit.Assert.*;

public class RPCClientTest {

    @Test
    public void testSendRequest() {
        int testPort = 7001;
        // Create a dummy DroneSubsystem that simply echoes the request
        DroneSubsystem dummyDrone = new DroneSubsystem("localhost", testPort, 8000) {
            @Override
            public Object handleRequest(Object request) {
                return request;
            }
        };

        // Start an RPCServer on the test port with dummyDrone as handler.
        Thread serverThread = new Thread(new RPCServer(testPort, dummyDrone));
        serverThread.start();

        // Allow the server to start
        try { Thread.sleep(100); } catch (InterruptedException e) { }

        // Create an RPCClient and send a test message.
        RPCClient client = new RPCClient("localhost", testPort);
        String testMessage = "Hello, RPC!";
        Object response = client.sendRequest(testMessage);

        assertEquals(testMessage, response);

        // Optionally interrupt the server thread (in a real test you might add a shutdown mechanism)
        serverThread.interrupt();
    }
}
