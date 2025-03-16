import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.net.*;

public class RPCServerTest {

    @Test
    public void testRPCServerEcho() throws Exception {
        int testPort = 7002;
        // Create a dummy DroneSubsystem that echoes requests.
        DroneSubsystem dummyDrone = new DroneSubsystem("localhost", testPort, 8001) {
            @Override
            public Object handleRequest(Object request) {
                return request;
            }
        };

        // Start the RPCServer with the dummyDrone handler.
        Thread serverThread = new Thread(new RPCServer(testPort, dummyDrone));
        serverThread.start();

        // Allow the server to start
        try { Thread.sleep(100); } catch (InterruptedException e) { }

        // Manually connect to the RPCServer via a Socket.
        Socket socket = new Socket("localhost", testPort);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        String message = "Test RPCServer";
        out.writeObject(message);
        out.flush();
        Object response = in.readObject();

        assertEquals(message, response);

        socket.close();
        serverThread.interrupt();
    }
}
