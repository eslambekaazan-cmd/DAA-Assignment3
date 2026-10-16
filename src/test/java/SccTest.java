
import algorithms.Kosaraju;
import graph.Edge;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SccTest {

    @Test
    void matchesBruteForceOnRandomGraphs() {
        Random r = new Random(7);
        for (int round = 0; round < 40; round++) {
            int n = 1 + r.nextInt(200);
            int m = r.nextInt(3 * n + 1);
            Graph g = TestGraphs.randomDirected(r, n, m);

            boolean[][] reach = new boolean[n][];
            for (int s = 0; s < n; s++) {
                reach[s] = bfs(g, s);
            }

            Kosaraju.Result res = Kosaraju.run(g, new Metrics());
            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    boolean sameByBrute = reach[u][v] && reach[v][u];
                    assertEquals(sameByBrute, res.comp[u] == res.comp[v]);
                }
            }
            Set<Integer> ids = new HashSet<>();
            for (int c : res.comp) {
                ids.add(c);
            }
            assertEquals(ids.size(), res.count);
        }
    }

    @Test
    void cyclicFileHasFourComponents() throws IOException {
        Graph g = Graph.fromFile("data/cyclic.txt");
        Kosaraju.Result res = Kosaraju.run(g, new Metrics());
        assertEquals(4, res.count);
        assertEquals(res.comp[0], res.comp[1]);
        assertEquals(res.comp[1], res.comp[2]);
        assertEquals(res.comp[3], res.comp[4]);
        assertEquals(res.comp[4], res.comp[5]);
        assertTrue(res.comp[0] != res.comp[3]);
        assertTrue(res.comp[6] != res.comp[7]);
    }

    @Test
    void everyEdgeIsScannedTwice() {
        Graph g = TestGraphs.randomDirected(new Random(3), 500, 2000);
        Metrics m = new Metrics();
        Kosaraju.run(g, m);
        assertEquals(2L * g.m(), m.edgeScans);
    }

    @Test
    void graphWithoutVerticesAndWithIsolatedVertices() {
        assertEquals(0, Kosaraju.run(new Graph(0, true), new Metrics()).count);
        assertEquals(6, Kosaraju.run(new Graph(6, true), new Metrics()).count);
    }

    @Test
    void pathOf100000VerticesDoesNotOverflowTheStack() {
        int n = 100_000;
        Graph g = new Graph(n, true);
        for (int i = 0; i + 1 < n; i++) {
            g.addEdge(i, i + 1, 1);
        }
        Metrics m = new Metrics();
        Kosaraju.Result res = Kosaraju.run(g, m);
        assertEquals(n, res.count);
        assertEquals(n, m.maxDepth);
        assertEquals(2L * (n - 1), m.edgeScans);
    }

    @Test
    void oneBigCycleOf100000VerticesIsOneComponent() {
        int n = 100_000;
        Graph g = new Graph(n, true);
        for (int i = 0; i < n; i++) {
            g.addEdge(i, (i + 1) % n, 1);
        }
        Kosaraju.Result res = Kosaraju.run(g, new Metrics());
        assertEquals(1, res.count);
    }

    @Test
    void twoVerticesWithMutualEdgesFormOneComponent() {
        Graph g = new Graph(2, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 0, 1);
        Kosaraju.Result res = Kosaraju.run(g, new Metrics());
        assertEquals(1, res.count);
        assertEquals(res.comp[0], res.comp[1]);
    }

    private static boolean[] bfs(Graph g, int s) {
        boolean[] seen = new boolean[g.n()];
        ArrayDeque<Integer> q = new ArrayDeque<>();
        seen[s] = true;
        q.add(s);
        while (!q.isEmpty()) {
            int u = q.poll();
            for (Edge e : g.adj(u)) {
                if (!seen[e.to]) {
                    seen[e.to] = true;
                    q.add(e.to);
                }
            }
        }
        return seen;
    }
}
