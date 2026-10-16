package graph;

public final class Edge {
    public final int from;
    public final int to;
    public final int w;

    public Edge(int from, int to, int w) {
        this.from = from;
        this.to = to;
        this.w = w;
    }

    public int other(int x) {
        return x == from ? to : from;
    }
}