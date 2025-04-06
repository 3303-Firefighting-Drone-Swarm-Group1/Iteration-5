import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/***
 * Displays Active positions of Fire Incidents and Drones in service by updating based on a Timer.
 */
public class EventUI extends JFrame implements ActionListener {

    guiDrawing map;
    ViewController viewController;
    HashMap<Integer, Zone> zones;
    JPanel panel;

    public EventUI(ViewController viewController) {
        super("Event UI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.viewController = viewController;
        panel = new JPanel(new GridLayout(6, 0));
        zones = viewController.getZoneData();

        setSize(1200, 900);
        this.map =  new guiDrawing(zones, viewController.fireList, viewController.droneList, viewController.extinguishedFireList);
        map.setPreferredSize(new Dimension(900, 900));



        panel.add(new JLabel("Legend"));
        writeLegend();

        //Logs location
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setBorder(BorderFactory.createLineBorder(Color.black));
        textArea.setWrapStyleWord(true);
        panel.getLayout().minimumLayoutSize(textArea);
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        this.add(map, BorderLayout.CENTER);
        this.add(panel, BorderLayout.EAST);

        setVisible(true);
        //new Timer(100, System.out.println("fffff"));



        Timer timer = new Timer(150, this);
        timer.setInitialDelay(200);
        timer.start();

    }

    public void updateMap() {
        map.updateData(viewController.fireList, viewController.droneList, viewController.extinguishedFireList);
        map.repaint();

        //        int farthest_point = 600;
//        int numberZones = 5;
//        guiDrawing drawing = new guiDrawing(zones);
//        map = drawing;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateMap();
    }

    public void writeLegend() {
        JTextArea b1 = new JTextArea("Active Fire");
        b1.setBackground(Color.ORANGE);
        JTextArea b2 = new JTextArea("Extinguished Fire");
        b2.setBackground(Color.GREEN);
        JTextArea b3 = new JTextArea("Drone outbound");
        b3.setBackground(Color.YELLOW);
        JTextArea b4 = new JTextArea("Drone extinguishing");
        b4.setBackground(Color.BLUE);
        JTextArea b5 = new JTextArea("Drone returning");
        b5.setBackground(Color.magenta);
        panel.add(b1);
        panel.add(b2);
        panel.add(b3);
        panel.add(b4);
        panel.add(b5);
    }
}

class guiDrawing extends JPanel {
    public HashMap<Integer, Zone> zones = new HashMap<>();
    private ArrayList<Fire> fires;
    private ArrayList<Fire> extinguishedFires;
    private ArrayList<Drone> drones;

    public guiDrawing(HashMap<Integer, Zone> zones, ArrayList<Fire> fires, ArrayList<Drone> drones, ArrayList<Fire> extinguishedFires) {
        this.zones = zones;
        this.fires = fires;
        this.drones = drones;
        this.extinguishedFires = extinguishedFires;
    }

    public void updateData(ArrayList<Fire> fires, ArrayList<Drone> drones, ArrayList<Fire> extinguishedFires) {
        this.fires = fires;
        this.drones = drones;
        this.extinguishedFires = extinguishedFires;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.black);

        //Graphics2D g2d = (Graphics2D) g;
        int[] coordinates;
        int x = 0;
        for (Zone zone: zones.values()) {
            g.setColor(Color.black);
            coordinates = zone.getCoordinatesAsInt();
            //System.out.println("Rectangle coord:" + coordinates[0] + " " +  coordinates[1]+  " " + coordinates[2]+ " " + coordinates[3]);
            g.fillRect(coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
            //System.out.println("zone " + (int) zone.getStart().getY() + "Dimensions" + zone.getEnd().getY());
            g.setColor(Color.WHITE);
            g.fillRect(coordinates[0]+2, coordinates[1] +2, coordinates[2] -2, coordinates[3] -2);
            String temp = String.valueOf(x++);
            g.setColor(Color.BLACK);
            g.drawString(temp, coordinates[0] + 20, coordinates[1] + 20);
        }
        for (Fire fire: fires) {
            if (extinguishedFires.contains(fire)) {
                g.setColor(Color.GREEN);
            } else {
                g.setColor(Color.ORANGE);
            }
            g.fillRect((int)(fire.getX() - 5), (int)(fire.getY() - 5), 10, 10);
            System.out.println("Fire: " + fire.getDistance());
        }

        for (Drone drone: drones) {
            if (drone.getState().equals(DroneSubsystem.DroneState.RETURNING_TO_BASE)) {
                g.setColor(Color.MAGENTA);
            } else if (drone.getState().equals(DroneSubsystem.DroneState.EN_ROUTE)) {
                g.setColor(Color.YELLOW);
            } else if (drone.getState().equals(DroneSubsystem.DroneState.DROPPING_AGENT)) {
                g.setColor(Color.BLUE);
            }
            g.fillRect((int)(drone.getX() - 5), (int)(drone.getY() - 5), 10, 10);
            String temp = drone.getState().toString();
            g.drawString(temp, (int)(drone.getX() + 10), (int)(drone.getX() + 10));
        }
        //System.out.println("zone size: " + zones.size());
        //g2d.dispose();
    }
}
