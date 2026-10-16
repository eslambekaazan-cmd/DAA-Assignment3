package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DagShortestPath {

    public static final long INF = Long.MAX_VALUE;

    public static class Result {
        public final long[] dist;
        public final int[] parent;
        public final long sumDist;

        Result(long[] dist, int[] parent, long sumDist) {
            this.dist = dist;
            this.parent = parent;
            this.sumDist = sumDist;
        }

        public List<Integer> pathTo(int v) {
            List<Integer> path = new ArrayList<>();
            for (int x = v; x != -1; x = parent[x]) {
                path.add(x);
            }
            Collections.reverse(path);
            return path;
        }
    }

    public static Result run(Graph g, Metrics metrics) {
        Kahn.Result topo = Kahn.run(g, new Metrics());
        if (topo.hasCycle) {
            throw new IllegalArgumentException("graph has a cycle, it is not a DAG");
        }

        int n = g.n();
        long[] dist = new long[n];
        int[] parent = new int[n];
        java.util.Arrays.fill(dist, INF);
        java.util.Arrays.fill(parent, -1);

        boolean[] hasIncoming = new boolean[n];
        for (Edge e : g.edges()) {
            hasIncoming[e.to] = true;
        }
        for (int v = 0; v < n; v++) {
            if (!hasIncoming[v]) {
                dist[v] = 0;
            }
        }

        for (int u : topo.order) {
            if (dist[u] == INF) {
                continue;
            }
            for (Edge e : g.adj(u)) {
                metrics.edgeScans++;
                metrics.comparisons++;
                long cand = dist[u] + e.w;
                if (cand < dist[e.to]) {
                    dist[e.to] = cand;
                    parent[e.to] = u;
                }
            }
        }

        long sum = 0;
        for (int v = 0; v < n; v++) {
            if (dist[v] != INF) {
                sum += dist[v];
            }
        }
        return new Result(dist, parent, sum);
    }
}