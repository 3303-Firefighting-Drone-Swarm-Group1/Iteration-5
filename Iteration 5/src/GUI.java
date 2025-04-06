import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GUI extends JFrame {

    JFrame frame;
    ArrayList<JPanel> panels;
    JPanel Legend;
    ViewController controller;

    public GUI() {
        super("Choose Input File");

//        Frame Settings
        frame.setSize(1000, 1000);
        frame.setLayout(new GridLayout(2,2));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        panels = new ArrayList<>();
        Legend = new JPanel();
    }
    public static void main(String[] args) {
        GUI gui = new GUI();
    }
}
