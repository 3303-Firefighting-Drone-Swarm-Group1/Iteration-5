import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;

public class SchedulerTest {

    @Test
    public void testJoinAndLeave() throws Exception {
        int schedulerPort = 5001;
        Scheduler scheduler = new Scheduler(schedulerPort);

        // Allow the RPC server thread to start.
        try { Thread.sleep(100); } catch (InterruptedException e) { }

        // Send a join request in the format "join:port"
        scheduler.handleRequest("join:7000");

        Field field = Scheduler.class.getDeclaredField("availableDrones");
        field.setAccessible(true);
        ArrayList availableDrones = (ArrayList) field.get(scheduler);


        Field socketField = RPCClient.class.getDeclaredField("socket");
        socketField.setAccessible(true);
        for (Object client : availableDrones) {
            Socket dummySocket = new Socket() {
                @Override
                public int getLocalPort() {
                    return 7000;
                }
            };
            socketField.set(client, dummySocket);
        }

        // After join, availableDrones should have one entry.
        assertEquals(1, availableDrones.size());

        // Now send a leave request to remove the drone.
        scheduler.handleRequest("leave:7000");
        assertEquals(0, availableDrones.size());
    }
}
