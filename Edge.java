package graph;

// Represents a directed edge from source to target node in a general graph
public class Edge {
    private final Node source;
    private final Node target;

    public Edge(Node source, Node target) {
        this.source = source;
        this.target = target;
    }

    public Node getSource() {
        return source;
    }

    public Node getTarget() {
        return target;
    }
} 