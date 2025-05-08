package graph;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Node {
    private final String label;
    private final Rectangle bounds;
    private final List<Node> children = new ArrayList<>();
    private int rank = -1; // BFS rank/level
    private Node parent;

    public Node(String label, Rectangle bounds) {
        this.label = label;
        this.bounds = bounds;
    }

    public String getLabel() {
        return label;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }
} 