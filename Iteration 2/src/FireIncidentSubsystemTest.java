import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.util.Scanner;

/***
 * @author Sam Touma
 * Iteration 1 Feb 1, 2025
 */

class FireIncidentSubsystemTest {

    private static final String ZONE_FILE = "input/sample_zone_file.csv";
    private static final String EVENT_FILE = "input/Sample_event_file.csv";;

    @Test
    void testFileReading() {
        // Check if files exist
        File zoneFile = new File(ZONE_FILE);
        File eventFile = new File(EVENT_FILE);

        assertTrue("Zone input file is missing!", zoneFile.exists());
        assertTrue("Event input file is missing!", eventFile.exists());

        // Check if files can be read
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

    @Test
    void testIncidentPassing() throws InterruptedException {
        // Create components
        Box fireToSchedulerBox = new Box();
        Box schedulerToFireBox = new Box();
        Box schedulerToDroneBox = new Box();
        Box droneToSchedulerBox = new Box();

        Scheduler scheduler = new Scheduler(schedulerToFireBox, schedulerToDroneBox, fireToSchedulerBox, droneToSchedulerBox);
        FireIncidentSubsystem fireSystem = new FireIncidentSubsystem(scheduler, ZONE_FILE, EVENT_FILE, fireToSchedulerBox, schedulerToFireBox);
        DroneSubsystem drone = new DroneSubsystem(scheduler, droneToSchedulerBox, schedulerToDroneBox);

        // Start threads
        Thread fireThread = new Thread(fireSystem);
        Thread droneThread = new Thread(drone);
        Thread schedulerThread = new Thread(scheduler);
        fireThread.start();
        droneThread.start();
        schedulerThread.start();

        // thread delay
        Thread.sleep(5000);

        // scheduler processed at least one incident
        assertTrue("No incidents were fully processed.", fireSystem.getNumCompleted() > 0);

        // terminate threads
        fireThread.interrupt();
        droneThread.interrupt();
        schedulerThread.interrupt();
        fireThread.join();
        droneThread.join();
        schedulerThread.join();
    }
}
