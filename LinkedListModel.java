package graph;

import java.util.ArrayList;
import java.util.List;

public class LinkedListModel {
    private final List<Node> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void addEdge(Node source, Node target) {
        // Remove any existing parent edge for the target
        edges.removeIf(e -> e.getTarget() == target);
        // Remove from previous parent's children list
        for (Node n : nodes) {
            n.getChildren().remove(target);
        }
        // Add as child to new parent
        if (!source.getChildren().contains(target)) {
            source.addChild(target);
        }
        if (!hasEdge(source, target)) {
            edges.add(new Edge(source, target));
        }
    }

    public boolean hasEdge(Node source, Node target) {
        for (Edge e : edges) {
            if (e.getSource() == source && e.getTarget() == target) {
                return true;
            }
        }
        return false;
    }

    public void removeEdge(Node source, Node target) {
        edges.removeIf(e -> e.getSource() == source && e.getTarget() == target);
        source.getChildren().remove(target);
    }
} 