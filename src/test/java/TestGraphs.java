
import graph.Graph;

import java.util.Random;

final class TestGraphs {

    private TestGraphs() {
    }

    static Graph randomConnected(Random r, int n, int extra, int maxW) {
        Graph g = new Graph(n, false);
        for (int i = 1; i < n; i++) {
            g.addEdge(r.nextInt(i), i, 1 + r.nextInt(maxW));
        }
        for (int k = 0; k < extra; k++) {
            g.addEdge(r.nextInt(n), r.nextInt(n), 1 + r.nextInt(maxW));
        }
        return g;
    }

    static Graph randomDirected(Random r, int n, int m) {
        Graph g = new Graph(n, true);
        for (int k = 0; k < m; k++) {
            g.addEdge(r.nextInt(n), r.nextInt(n), 1 + r.nextInt(10));
        }
        return g;
    }

    static Graph randomDag(Random r, int n, int m, int minW, int maxW) {
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            rank[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {
            int j = r.nextInt(i + 1);
            int t = rank[i];
            rank[i] = rank[j];
            rank[j] = t;
        }
        Graph g = new Graph(n, true);
        if (n < 2) {
            return g;
        }
        for (int k = 0; k < m; k++) {
            int a = r.nextInt(n);
            int b = r.nextInt(n);
            if (a == b) {
                continue;
            }
            if (rank[a] > rank[b]) {
                int t = a;
                a = b;
                b = t;
            }
            g.addEdge(a, b, minW + r.nextInt(maxW - minW + 1));
        }
        return g;
    }
}
