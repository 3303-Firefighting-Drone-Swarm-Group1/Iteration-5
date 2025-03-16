import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class SchedulerTest {

    @Test
    public void testJoinAndLeave() throws Exception {
        int schedulerPort = 5001;
        Scheduler scheduler = new Scheduler(schedulerPort);

        // Allow the RPC server thread to start
        try { Thread.sleep(100); } catch (InterruptedException e) { }

        // Send a join request (simulated message format "join:port")
        scheduler.handleRequest("join:7000");

        // Use reflection to access the private 'availableDrones' field
        Field field = Scheduler.class.getDeclaredField("availableDrones");
        field.setAccessible(true);
        ArrayList availableDrones = (ArrayList) field.get(scheduler);

        // After join, availableDrones should have one entry.
        assertEquals(1, availableDrones.size());

        // Now send a leave request to remove the drone.
        scheduler.handleRequest("leave:7000");
        assertEquals(0, availableDrones.size());
    }
}
