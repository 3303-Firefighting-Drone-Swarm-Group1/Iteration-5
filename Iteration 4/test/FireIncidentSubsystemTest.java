import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;
import java.lang.reflect.Field;

public class FireIncidentSubsystemTest {

    private static final String ZONE_FILE = "input/sample_zone_file.csv";
    private static final String EVENT_FILE = "input/Sample_event_file.csv";

    @Test
    public void testFileReading() {
        File zoneFile = new File(ZONE_FILE);
        File eventFile = new File(EVENT_FILE);

        assertTrue("Zone input file is missing!", zoneFile.exists());
        assertTrue("Event input file is missing!", eventFile.exists());

        try (Scanner zoneScanner = new Scanner(zoneFile)) {
            assertTrue("Zone file is empty!", zoneScanner.hasNextLine());
        } catch (Exception e) {
            fail("Failed to read zone file: " + e.getMessage());
        }

        try (Scanner eventScanner = new Scanner(eventFile)) {
            assertTrue("Event file is empty!", eventScanner.hasNextLine());
        } catch (Exception e) {
            fail("Failed to read event file: " + e.getMessage());
        }
    }

    private class DummyRPCClient extends RPCClient {
        private Object lastRequest;
        public DummyRPCClient(String host, int port) {
            super(host, port);
        }
        @Override
        public Object sendRequest(Object request) {
            lastRequest = request;
            return null;
        }
        public Object getLastRequest() {
            return lastRequest;
        }
    }

    @Test
    public void testIncidentPassing() throws Exception {
        // We pass our updated file paths to the constructor.
        FireIncidentSubsystem fireSystem = new FireIncidentSubsystem(
                ZONE_FILE,
                EVENT_FILE,
                "localhost",
                12345
        );

        // Replace the internal schedulerClient with a dummy so we can see what was sent.
        DummyRPCClient dummyClient = new DummyRPCClient("localhost", 12345);
        Field clientField = FireIncidentSubsystem.class.getDeclaredField("schedulerClient");
        clientField.setAccessible(true);
        clientField.set(fireSystem, dummyClient);

        // Run the subsystem, which should read the files and send incidents to the scheduler.
        fireSystem.run();

        // Verify that a non-empty list of IncidentMessages was sent.
        Object request = dummyClient.getLastRequest();
        assertNotNull("No request was sent to the scheduler.", request);
        assertTrue("The request should be an ArrayList of IncidentMessages", request instanceof ArrayList);
        ArrayList<?> messages = (ArrayList<?>) request;
        assertFalse("The incident list should not be empty.", messages.isEmpty());
    }
}
