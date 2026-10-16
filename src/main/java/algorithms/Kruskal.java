package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.List;

public class Kruskal {

    public static MstResult run(Graph g, Metrics metrics) {
        int n = g.n();
        Edge[] sorted = g.edges().toArray(new Edge[0]);
        mergeSort(sorted, metrics);

        Dsu dsu = new Dsu(n);
        List<Edge> tree = new ArrayList<>();
        long total = 0;

        for (Edge e : sorted) {
            if (tree.size() == n - 1) {
                break;
            }
            metrics.edgeScans++;
            if (dsu.find(e.from) != dsu.find(e.to)) {
                dsu.union(e.from, e.to);
                tree.add(e);
                total += e.w;
            }
        }

        metrics.dsuOps += dsu.findCalls + dsu.unionCalls;
        return new MstResult(total, tree, dsu.components());
    }

    private static void mergeSort(Edge[] a, Metrics m) {
        Edge[] tmp = new Edge[a.length];
        sort(a, tmp, 0, a.length, m);
    }

    private static void sort(Edge[] a, Edge[] tmp, int lo, int hi, Metrics m) {
        if (hi - lo < 2) {
            return;
        }
        int mid = (lo + hi) >>> 1;
        sort(a, tmp, lo, mid, m);
        sort(a, tmp, mid, hi, m);
        int i = lo;
        int j = mid;
        int k = lo;
        while (i < mid && j < hi) {
            m.comparisons++;
            if (a[j].w < a[i].w) {
                tmp[k++] = a[j++];
            } else {
                tmp[k++] = a[i++];
            }
        }
        while (i < mid) {
            tmp[k++] = a[i++];
        }
        while (j < hi) {
            tmp[k++] = a[j++];
        }
        System.arraycopy(tmp, lo, a, lo, hi - lo);
    }
}