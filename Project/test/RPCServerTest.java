import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.net.*;

public class RPCServerTest {

    @Test
    public void testRPCServerEcho() throws Exception {
        int testPort = 7002;
        // Create a dummy Scheduler on testPort that echoes requests.
        Scheduler dummyScheduler = new Scheduler(testPort) {
            @Override
            public Object handleRequest(Object request) {
                return request;
            }
        };

        // Allow time for the RPC server to start.
        try { Thread.sleep(100); } catch (InterruptedException e) { }

        // Connect to the RPCServer running on testPort.
        RPCClient rpcClient = new RPCClient("localhost", testPort);

        String message = "Test RPCServer";

        String recieve = (String) rpcClient.sendRequest(message);
        //out.writeObject(message);
        // out.flush();
        //in.readObject();
        assertEquals(message, recieve);

    }
}
