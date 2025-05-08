package graph;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        super("Graph Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 1200);
        setLocationRelativeTo(null);

        LinkedListModel model = new LinkedListModel();
        LinkedListPanel panel = new LinkedListPanel(model);
        add(panel, BorderLayout.CENTER);
        // Toolbar and Add Node button removed; node addition is now via right-click context menu
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
} 