package graph;

import javax.swing.*;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.stream.Collectors;

public class LinkedListPanel extends JPanel {
    private final LinkedListModel model;
    private Node selectedNode = null;
    private Node edgeFirstNode = null;
    private Node edgeSecondNode = null;
    private int newNodeCounter = 7; // For unique labels
    private Point popupPoint = null;

    public LinkedListPanel(LinkedListModel model) {
        this.model = model;
        setBackground(Color.WHITE);
        layoutSampleGraph();
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouse(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouse(e);
            }
            private void handleMouse(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupPoint = e.getPoint();
                    Node node = findNodeAt(popupPoint);
                    if (node == null) {
                        // Right-click on empty space: add node
                        showAddNodeMenu(popupPoint);
                        clearEdgeSelection();
                    }
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    Node node = findNodeAt(e.getPoint());
                    if (node != null) {
                        if (e.isShiftDown()) {
                            // Shift-click: select as second node for edge
                            if (edgeFirstNode != null && node != edgeFirstNode) {
                                edgeSecondNode = node;
                                showEdgeMenu(edgeFirstNode, edgeSecondNode, e.getPoint());
                            }
                        } else {
                            // Regular click: select as first node
                            edgeFirstNode = node;
                            edgeSecondNode = null;
                            selectedNode = node;
                        }
                    } else {
                        // Clicked empty space: clear selection
                        selectedNode = null;
                        clearEdgeSelection();
                    }
                    repaint();
                }
            }
        };
        addMouseListener(mouseAdapter);
    }

    private void clearEdgeSelection() {
        edgeFirstNode = null;
        edgeSecondNode = null;
    }

    private Node findNodeAt(Point p) {
        for (Node node : model.getNodes()) {
            if (node.getBounds().contains(p)) {
                return node;
            }
        }
        return null;
    }

    private void showAddNodeMenu(Point p) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem addNodeItem = new JMenuItem("Add New Node");
        addNodeItem.addActionListener(e -> addNodeAtPoint(p));
        menu.add(addNodeItem);
        menu.show(this, p.x, p.y);
    }

    private void addNodeAtPoint(Point p) {
        String label = JOptionPane.showInputDialog(this, "Enter node label:", "Node " + newNodeCounter);
        if (label == null || label.trim().isEmpty()) {
            label = "Node " + newNodeCounter;
        }
        Rectangle bounds = new Rectangle(p.x - 40, p.y - 20, 80, 40);
        Node newNode = new Node(label, bounds);
        model.addNode(newNode);
        newNodeCounter++;
        recalculateRanksAndLayout();
        repaint();
    }

    private void showEdgeMenu(Node n1, Node n2, Point p) {
        boolean edgeExists = model.hasEdge(n1, n2);
        JPopupMenu menu = new JPopupMenu();
        if (edgeExists) {
            JMenuItem delEdge = new JMenuItem("Delete Edge");
            delEdge.addActionListener(e -> {
                model.removeEdge(n1, n2);
                clearEdgeSelection();
                recalculateRanksAndLayout();
                repaint();
            });
            menu.add(delEdge);
        } else {
            JMenuItem addEdge = new JMenuItem("Add Edge");
            addEdge.addActionListener(e -> {
                model.addEdge(n1, n2);
                clearEdgeSelection();
                recalculateRanksAndLayout();
                repaint();
            });
            menu.add(addEdge);
        }
        menu.show(this, p.x, p.y);
    }

    private void layoutSampleGraph() {
        model.getNodes().clear();
        model.getEdges().clear();
        // Node positions (hand-placed for clarity)
        Node n1 = new Node("Node 1", new Rectangle(100, 100, 80, 40));
        Node n2 = new Node("Node 2", new Rectangle(300, 60, 80, 40));
        Node n3 = new Node("Node 3", new Rectangle(300, 180, 80, 40));
        Node n4 = new Node("Node 4", new Rectangle(500, 30, 80, 40));
        Node n5 = new Node("Node 5", new Rectangle(500, 90, 80, 40));
        Node n6 = new Node("Node 6", new Rectangle(500, 200, 80, 40));
        model.addNode(n1);
        model.addNode(n2);
        model.addNode(n3);
        model.addNode(n4);
        model.addNode(n5);
        model.addNode(n6);
        // Edges
        model.addEdge(n1, n2);
        model.addEdge(n1, n3);
        model.addEdge(n2, n4);
        model.addEdge(n2, n5);
        model.addEdge(n2, n3);
        model.addEdge(n3, n6);
    }

    private void recalculateRanksAndLayout() {
        List<Node> allNodes = model.getNodes();
        // 1. Reset all ranks
        for (Node n : allNodes) {
            n.setRank(-1);
        }
        // 2. Compute incoming edge count for each node
        java.util.Map<Node, Integer> incoming = new java.util.HashMap<>();
        for (Node n : allNodes) incoming.put(n, 0);
        for (Edge e : model.getEdges()) {
            incoming.put(e.getTarget(), incoming.get(e.getTarget()) + 1);
        }
        // 3. Find roots (no incoming edges)
        java.util.List<Node> roots = new java.util.ArrayList<>();
        for (Node n : allNodes) {
            if (incoming.get(n) == 0) roots.add(n);
        }
        // Fallback: if no roots, pick the first node as root
        if (roots.isEmpty() && !allNodes.isEmpty()) {
            roots.add(allNodes.get(0));
            System.out.println("No roots found, using fallback root: " + allNodes.get(0).getLabel());
        }
        // 4. BFS from all roots, assign shortest rank
        java.util.Map<Integer, java.util.List<Node>> rankMap = new java.util.HashMap<>();
        java.util.Queue<Node> queue = new java.util.LinkedList<>();
        for (Node root : roots) {
            root.setRank(0);
            queue.add(root);
        }
        while (!queue.isEmpty()) {
            Node n = queue.poll();
            int rank = n.getRank();
            rankMap.computeIfAbsent(rank, k -> new java.util.ArrayList<>()).add(n);
            for (Node child : n.getChildren()) {
                if (child.getRank() == -1 || child.getRank() > rank + 1) {
                    child.setRank(rank + 1);
                    queue.add(child);
                }
            }
        }
        // 5. Layout: nodes with same rank share x, different y
        int nodeWidth = 80, nodeHeight = 40, xStart = 100, xStep = 200, yStart = 100, yStep = 80;
        for (var entry : rankMap.entrySet()) {
            int rank = entry.getKey();
            java.util.List<Node> nodesAtRank = entry.getValue();
            int x = xStart + rank * xStep;
            int total = nodesAtRank.size();
            for (int i = 0; i < total; i++) {
                int y = yStart + i * yStep;
                nodesAtRank.get(i).getBounds().setLocation(x, y);
            }
        }
        // Debug output: print roots and ranks
        System.out.println("Roots: " + roots.stream().map(Node::getLabel).collect(java.util.stream.Collectors.toList()));
        for (Node n : allNodes) {
            System.out.println(n.getLabel() + " rank=" + n.getRank());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw edges
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(100, 100, 200));
        for (Edge edge : model.getEdges()) {
            Node src = edge.getSource();
            Node tgt = edge.getTarget();
            Rectangle r1 = src.getBounds();
            Rectangle r2 = tgt.getBounds();
            int x1 = r1.x + r1.width;
            int y1 = r1.y + r1.height / 2;
            int x2 = r2.x;
            int y2 = r2.y + r2.height / 2;
            // Offset control point based on child index
            int childIdx = src.getChildren().indexOf(tgt);
            int total = src.getChildren().size();
            int ctrlX = (x1 + x2) / 2;
            int ctrlY = (y1 + y2) / 2 - 60 + childIdx * 40 - (total - 1) * 20;

            // Try to avoid node overlap
            int maxTries = 10;
            int tryOffset = 0;
            boolean overlaps;
            do {
                overlaps = false;
                QuadCurve2D q = new QuadCurve2D.Float(x1, y1, ctrlX, ctrlY - tryOffset, x2, y2);
                Rectangle2D curveBounds = q.getBounds2D();
                for (Node other : model.getNodes()) {
                    if (other == src || other == tgt) continue;
                    if (curveBounds.intersects(other.getBounds())) {
                        overlaps = true;
                        break;
                    }
                }
                if (overlaps) tryOffset += 30; // Increase offset and try again
                else {
                    // Draw the curve and arrowhead as before
                    g2.draw(q);
                    // Calculate point and tangent at t
                    double t = 0.95; // near the end of the curve
                    double x = (1-t)*(1-t)*x1 + 2*(1-t)*t*ctrlX + t*t*x2;
                    double y = (1-t)*(1-t)*y1 + 2*(1-t)*t*ctrlY + t*t*y2;
                    double dx = 2*(1-t)*(ctrlX - x1) + 2*t*(x2 - ctrlX);
                    double dy = 2*(1-t)*(ctrlY - y1) + 2*t*(y2 - ctrlY);
                    double angle = Math.atan2(dy, dx);

                    // Arrowhead parameters
                    int arrowLen = 12;
                    int arrowWidth = 7;

                    // Points for arrowhead
                    int xArrow1 = (int) (x - arrowLen * Math.cos(angle - Math.PI / 6));
                    int yArrow1 = (int) (y - arrowLen * Math.sin(angle - Math.PI / 6));
                    int xArrow2 = (int) (x - arrowLen * Math.cos(angle + Math.PI / 6));
                    int yArrow2 = (int) (y - arrowLen * Math.sin(angle + Math.PI / 6));

                    // Draw arrowhead
                    Polygon arrowHead = new Polygon();
                    arrowHead.addPoint((int)x, (int)y);
                    arrowHead.addPoint(xArrow1, yArrow1);
                    arrowHead.addPoint(xArrow2, yArrow2);
                    g2.fill(arrowHead);
                }
            } while (overlaps && tryOffset < 300);
        }

        // Draw nodes
        for (Node node : model.getNodes()) {
            Rectangle r = node.getBounds();
            if (node == selectedNode || node == edgeFirstNode || node == edgeSecondNode) {
                g2.setColor(new Color(255, 220, 120)); // Highlight selected
                g2.fill(r);
                g2.setColor(Color.ORANGE);
                g2.setStroke(new BasicStroke(3f));
                g2.draw(r);
                g2.setStroke(new BasicStroke(1f));
            } else {
                g2.setColor(new Color(180, 220, 250));
                g2.fill(r);
                g2.setColor(Color.DARK_GRAY);
                g2.draw(r);
            }
            g2.setColor(Color.BLACK);
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(node.getLabel());
            int textX = r.x + (r.width - textWidth) / 2;
            int textY = r.y + (r.height + fm.getAscent()) / 2 - 4;
            g2.drawString(node.getLabel(), textX, textY);
        }
        g2.dispose();
    }
} 