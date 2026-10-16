
import algorithms.Dsu;
import algorithms.Kruskal;
import algorithms.MstResult;
import algorithms.Prim;
import graph.Edge;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MstTest {

    @Test
    void kruskalAndPrimGiveSameWeightOn100RandomConnectedGraphs() {
        for (int seed = 0; seed < 100; seed++) {
            Random r = new Random(seed);
            int n = 2 + r.nextInt(60);
            int extra = r.nextInt(150);
            Graph g = TestGraphs.randomConnected(r, n, extra, 10);

            MstResult k = Kruskal.run(g, new Metrics());
            MstResult p = Prim.run(g, new Metrics());

            assertEquals(k.totalWeight, p.totalWeight);
            assertEquals(n - 1, k.edges.size());
            assertEquals(n - 1, p.edges.size());
            assertEquals(1, k.components);
            assertEquals(1, p.components);
            assertTrue(isSpanningTree(n, k.edges));
            assertTrue(isSpanningTree(n, p.edges));
        }
    }

    @Test
    void bothAlgorithmsMatchBruteForceOnTinyGraphs() {
        for (int seed = 0; seed < 40; seed++) {
            Random r = new Random(1000 + seed);
            int n = 2 + r.nextInt(5);
            Graph g = TestGraphs.randomConnected(r, n, r.nextInt(6), 20);
            long best = bruteForceMst(g);
            assertEquals(best, Kruskal.run(g, new Metrics()).totalWeight);
            assertEquals(best, Prim.run(g, new Metrics()).totalWeight);
        }
    }

    @Test
    void smallGraphFromFile() throws IOException {
        Graph g = Graph.fromFile("data/small_undirected.txt");
        MstResult k = Kruskal.run(g, new Metrics());
        MstResult p = Prim.run(g, new Metrics());
        assertEquals(14, k.totalWeight);
        assertEquals(14, p.totalWeight);
        assertEquals(5, k.edges.size());
        assertEquals(1, k.components);
    }

    @Test
    void disconnectedGraphGivesForestAndDoesNotThrow() throws IOException {
        Graph g = Graph.fromFile("data/disconnected.txt");
        MstResult k = Kruskal.run(g, new Metrics());
        MstResult p = Prim.run(g, new Metrics());
        assertEquals(7, k.totalWeight);
        assertEquals(7, p.totalWeight);
        assertEquals(4, k.components);
        assertEquals(4, p.components);
        assertEquals(3, k.edges.size());
        assertEquals(3, p.edges.size());
    }

    @Test
    void graphWithoutEdgesIsOnlyIsolatedVertices() {
        Graph g = new Graph(5, false);
        MstResult k = Kruskal.run(g, new Metrics());
        MstResult p = Prim.run(g, new Metrics());
        assertEquals(0, k.totalWeight);
        assertEquals(0, p.totalWeight);
        assertEquals(5, k.components);
        assertEquals(5, p.components);
    }

    @Test
    void kruskalCountsOperations() {
        Graph g = TestGraphs.randomConnected(new Random(5), 50, 100, 100);
        Metrics m = new Metrics();
        MstResult res = Kruskal.run(g, m);
        assertTrue(m.edgeScans <= g.m());
        assertTrue(m.edgeScans >= g.n() - 1);
        assertTrue(m.comparisons > 0);
        assertEquals(2 * m.edgeScans + res.edges.size(), m.dsuOps);
    }

    private static boolean isSpanningTree(int n, List<Edge> edges) {
        Dsu d = new Dsu(n);
        for (Edge e : edges) {
            if (!d.union(e.from, e.to)) {
                return false;
            }
        }
        return d.components() == 1;
    }

    private static long bruteForceMst(Graph g) {
        int n = g.n();
        int m = g.m();
        List<Edge> edges = g.edges();
        long best = Long.MAX_VALUE;
        for (int mask = 0; mask < (1 << m); mask++) {
            if (Integer.bitCount(mask) != n - 1) {
                continue;
            }
            Dsu d = new Dsu(n);
            long sum = 0;
            boolean ok = true;
            for (int i = 0; i < m && ok; i++) {
                if ((mask & (1 << i)) != 0) {
                    Edge e = edges.get(i);
                    ok = d.union(e.from, e.to);
                    sum += e.w;
                }
            }
            if (ok && d.components() == 1 && sum < best) {
                best = sum;
            }
        }
        return best;
    }
}
