package graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Graph {
    private final int n;
    private final boolean directed;
    private final List<Edge>[] adj;
    private final List<Edge> edges = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public Graph(int n, boolean directed) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0");
        }
        this.n = n;
        this.directed = directed;
        this.adj = new List[n];
        for (int i = 0; i < n; i++) {
            adj[i] = new ArrayList<>(2);
        }
    }

    public void addEdge(int u, int v, int w) {
        if (u < 0 || u >= n || v < 0 || v >= n) {
            throw new IllegalArgumentException("vertex out of range: " + u + " " + v);
        }
        Edge e = new Edge(u, v, w);
        edges.add(e);
        adj[u].add(e);
        if (!directed && u != v) {
            adj[v].add(e);
        }
    }

    public int n() {
        return n;
    }

    public int m() {
        return edges.size();
    }

    public boolean isDirected() {
        return directed;
    }

    public List<Edge> adj(int u) {
        return adj[u];
    }

    public List<Edge> edges() {
        return edges;
    }

    public Graph reverse() {
        if (!directed) {
            throw new IllegalStateException("reverse() only makes sense for directed graphs");
        }
        Graph r = new Graph(n, true);
        for (Edge e : edges) {
            r.addEdge(e.to, e.from, e.w);
        }
        return r;
    }

    public static Graph fromFile(String path) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(Path.of(path))) {
            String line = nextLine(br);
            if (line == null) {
                throw new IOException("empty file: " + path);
            }
            String[] h = line.split("\\s+");
            if (h.length != 3) {
                throw new IOException("bad header (need: n m directed|undirected): " + line);
            }
            int n = Integer.parseInt(h[0]);
            int m = Integer.parseInt(h[1]);
            boolean directed;
            if (h[2].equals("directed")) {
                directed = true;
            } else if (h[2].equals("undirected")) {
                directed = false;
            } else {
                throw new IOException("third word must be directed or undirected, got: " + h[2]);
            }
            Graph g = new Graph(n, directed);
            for (int i = 0; i < m; i++) {
                line = nextLine(br);
                if (line == null) {
                    throw new IOException("file has only " + i + " edges, header says " + m);
                }
                String[] p = line.split("\\s+");
                if (p.length != 3) {
                    throw new IOException("bad edge line: " + line);
                }
                g.addEdge(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
            }
            return g;
        }
    }

    private static String nextLine(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                return line;
            }
        }
        return null;
    }
}