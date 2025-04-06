import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/***
 * Displays Active positions of Fire Incidents and Drones in service.
 */
public class EventUI extends JFrame implements ActionListener {
    guiDrawing map;
    ViewController viewController;


    HashMap<Integer, Zone> zones;

    public EventUI(ViewController viewController) {
        super("Event UI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.viewController = viewController;

        JPanel panel = new JPanel();
//        zones.put(0, new Zone(100,0,200,300));
//        zones.put(1, new Zone(200,0,500,300));
//        zones.put(2, new Zone(100,300,200,500));
//        zones.put(3, new Zone(200,300,500,500));
        zones = viewController.getZoneData();

        setSize(900, 900);
        this.map =  new guiDrawing(zones, viewController.fireList, viewController.droneList);
        map.setPreferredSize(new Dimension(900, 900));


        //map.add(new JLabel("Map"));
        panel.add(new JLabel("Legend"));
        //Logs location
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setBorder(BorderFactory.createLineBorder(Color.black));
        textArea.setWrapStyleWord(true);
        panel.getLayout().minimumLayoutSize(textArea);
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        this.add(map, BorderLayout.CENTER);
        //this.add(panel, BorderLayout.SOUTHri);

        setVisible(true);
        //new Timer(100, System.out.println("fffff"));



        Timer timer = new Timer(150, this);
        timer.setInitialDelay(200);
        timer.start();

    }

    public void updateMap() {
        map.updateData(viewController.fireList, viewController.droneList);
        map.repaint();
        System.out.println("map updated");
        //        int farthest_point = 600;
//        int numberZones = 5;
//        guiDrawing drawing = new guiDrawing(zones);
//        map = drawing;
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        updateMap();
    }
}

class guiDrawing extends JPanel {
    public HashMap<Integer, Zone> zones = new HashMap<>();
    private ArrayList<Fire> fires;
    private ArrayList<Drone> drones;

    public guiDrawing(HashMap<Integer, Zone> zones, ArrayList<Fire> fires, ArrayList<Drone> drones) {
        this.zones = zones;
        this.fires = fires;
        this.drones = drones;
    }

    public void updateData(ArrayList<Fire> fires, ArrayList<Drone> drones){
        this.fires = fires;
        this.drones = drones;
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
            g.setColor(Color.GREEN);
            g.fillRect(coordinates[0]+2, coordinates[1] +2, coordinates[2] -2, coordinates[3] -2);
            String temp = String.valueOf(x++);
            g.setColor(Color.BLACK);
            g.drawString(temp, coordinates[0] + 20, coordinates[1] + 20);
        }
        for (Fire fire: fires) {
            g.setColor(Color.RED);
            g.fillRect((int)(fire.getX() - 5), (int)(fire.getY() - 5), 10, 10);
        }

        for (Drone drone: drones) {
            g.setColor(Color.BLUE);
            g.fillRect((int)(drone.getX() - 5), (int)(drone.getY() - 5), 10, 10);
        }
        //System.out.println("zone size: " + zones.size());
        //g2d.dispose();
    }
}
