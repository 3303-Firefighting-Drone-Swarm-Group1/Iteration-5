import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.util.Scanner;

/***
 * @author Sam Touma
 * Iteration 1 Feb 1, 2025
 */

class SystemTest {

    private static final String ZONE_FILE = "input/sample_zone_file.csv";
    private static final String EVENT_FILE = "input/Sample_event_file.csv";;

    @Test
    void testFileReading() {
        // Check if files exist
        File zoneFile = new File(ZONE_FILE);
        File eventFile = new File(EVENT_FILE);

        assertTrue(zoneFile.exists(), "Zone input file is missing!");
        assertTrue(eventFile.exists(), "Event input file is missing!");

        // Check if files can be read
        try (Scanner zoneScanner = new Scanner(zoneFile)) {
            assertTrue(zoneScanner.hasNextLine(), "Zone file is empty!");
        } catch (Exception e) {
            fail("Failed to read zone file: " + e.getMessage());
        }

        try (Scanner eventScanner = new Scanner(eventFile)) {
            assertTrue(eventScanner.hasNextLine(), "Event file is empty!");
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
        assertTrue(fireSystem.getNumCompleted() > 0, "No incidents were fully processed.");

        // terminate threads
        fireThread.interrupt();
        droneThread.interrupt();
        schedulerThread.interrupt();
        fireThread.join();
        droneThread.join();
        schedulerThread.join();
    }
}
