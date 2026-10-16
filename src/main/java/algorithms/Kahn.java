package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

public class Kahn {

    public static class Result {
        public final boolean hasCycle;
        public final int[] order;
        public final int processed;

        Result(boolean hasCycle, int[] order, int processed) {
            this.hasCycle = hasCycle;
            this.order = order;
            this.processed = processed;
        }
    }

    public static Result run(Graph g, Metrics metrics) {
        if (!g.isDirected()) {
            throw new IllegalArgumentException("topological order needs a directed graph");
        }
        int n = g.n();
        int[] indeg = new int[n];
        for (Edge e : g.edges()) {
            indeg[e.to]++;
        }

        int[] queue = new int[n];
        int head = 0;
        int tail = 0;
        for (int v = 0; v < n; v++) {
            if (indeg[v] == 0) {
                queue[tail++] = v;
            }
        }

        while (head < tail) {
            int u = queue[head++];
            for (Edge e : g.adj(u)) {
                metrics.edgeScans++;
                indeg[e.to]--;
                if (indeg[e.to] == 0) {
                    queue[tail++] = e.to;
                }
            }
        }

        boolean cycle = tail < n;
        return new Result(cycle, cycle ? null : queue, tail);
    }

    public static boolean isValidOrder(Graph g, int[] order) {
        int n = g.n();
        if (order == null || order.length != n) {
            return false;
        }
        int[] pos = new int[n];
        java.util.Arrays.fill(pos, -1);
        for (int i = 0; i < n; i++) {
            int v = order[i];
            if (v < 0 || v >= n || pos[v] != -1) {
                return false;
            }
            pos[v] = i;
        }
        for (Edge e : g.edges()) {
            if (pos[e.from] >= pos[e.to]) {
                return false;
            }
        }
        return true;
    }
}