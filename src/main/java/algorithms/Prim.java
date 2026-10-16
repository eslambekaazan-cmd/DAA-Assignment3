package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Prim {

    public static MstResult run(Graph g, Metrics metrics) {
        int n = g.n();
        boolean[] inTree = new boolean[n];
        List<Edge> tree = new ArrayList<>();
        long total = 0;
        int components = 0;

        PriorityQueue<Edge> pq = new PriorityQueue<>((a, b) -> {
            metrics.comparisons++;
            return Integer.compare(a.w, b.w);
        });

        for (int start = 0; start < n; start++) {
            if (inTree[start]) {
                continue;
            }
            components++;
            inTree[start] = true;
            scan(g, start, inTree, pq, metrics);

            while (!pq.isEmpty()) {
                Edge e = pq.poll();
                boolean a = inTree[e.from];
                boolean b = inTree[e.to];
                if (a && b) {
                    continue;
                }
                int v = a ? e.to : e.from;
                inTree[v] = true;
                tree.add(e);
                total += e.w;
                scan(g, v, inTree, pq, metrics);
            }
        }
        return new MstResult(total, tree, components);
    }

    private static void scan(Graph g, int u, boolean[] inTree, PriorityQueue<Edge> pq, Metrics metrics) {
        for (Edge e : g.adj(u)) {
            metrics.edgeScans++;
            if (!inTree[e.other(u)]) {
                pq.add(e);
            }
        }
    }
}