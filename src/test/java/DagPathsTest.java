
import algorithms.DagShortestPath;
import graph.Edge;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DagPathsTest {

    private static final long INF = Long.MAX_VALUE;

    private static long[] bellmanFord(Graph g) {
        int n = g.n();
        long[] d = new long[n];
        Arrays.fill(d, INF);
        boolean[] hasIn = new boolean[n];
        for (Edge e : g.edges()) {
            hasIn[e.to] = true;
        }
        for (int v = 0; v < n; v++) {
            if (!hasIn[v]) {
                d[v] = 0;
            }
        }
        for (int round = 0; round < n; round++) {
            for (Edge e : g.edges()) {
                if (d[e.from] != INF && d[e.from] + e.w < d[e.to]) {
                    d[e.to] = d[e.from] + e.w;
                }
            }
        }
        return d;
    }

    @Test
    void matchesBellmanFordOnRandomDagsWithNegativeWeights() {
        Random r = new Random(21);
        for (int round = 0; round < 200; round++) {
            int n = 1 + r.nextInt(30);
            Graph g = TestGraphs.randomDag(r, n, r.nextInt(3 * n + 1), -10, 10);
            long[] expected = bellmanFord(g);
            DagShortestPath.Result res = DagShortestPath.run(g, new Metrics());
            long sum = 0;
            for (int v = 0; v < n; v++) {
                assertEquals(expected[v], res.dist[v]);
                sum += expected[v];
            }
            assertEquals(sum, res.sumDist);
        }
    }

    @Test
    void exampleFileWithNegativeEdge() throws IOException {
        Graph g = Graph.fromFile("data/dag.txt");
        DagShortestPath.Result res = DagShortestPath.run(g, new Metrics());
        long[] expected = {0, 5, 3, 3, 5, 2};
        for (int v = 0; v < 6; v++) {
            assertEquals(expected[v], res.dist[v]);
        }
        assertEquals(18, res.sumDist);
        assertEquals(List.of(0, 2, 4, 5), res.pathTo(5));
    }

    @Test
    void severalSourcesAllStartAtZero() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 2, 5);
        g.addEdge(1, 2, -3);
        DagShortestPath.Result res = DagShortestPath.run(g, new Metrics());
        assertEquals(0, res.dist[0]);
        assertEquals(0, res.dist[1]);
        assertEquals(-3, res.dist[2]);
        assertEquals(List.of(1, 2), res.pathTo(2));
    }

    @Test
    void restoredPathHasTheSameLengthAsTheDistance() {
        Random r = new Random(33);
        for (int round = 0; round < 50; round++) {
            int n = 2 + r.nextInt(40);
            Graph g = TestGraphs.randomDag(r, n, 3 * n, -5, 20);
            DagShortestPath.Result res = DagShortestPath.run(g, new Metrics());
            for (int v = 0; v < n; v++) {
                List<Integer> path = res.pathTo(v);
                assertEquals(0, res.dist[path.get(0)]);
                assertEquals(res.dist[v], pathLength(g, path));
            }
        }
    }

    @Test
    void onePassScansEveryEdgeOnce() {
        Graph g = TestGraphs.randomDag(new Random(4), 300, 1500, 1, 100);
        Metrics m = new Metrics();
        DagShortestPath.run(g, m);
        assertEquals(g.m(), m.edgeScans);
        assertEquals(g.m(), m.comparisons);
    }

    @Test
    void graphWithCycleIsRejected() throws IOException {
        Graph g = Graph.fromFile("data/cyclic.txt");
        assertThrows(IllegalArgumentException.class, () -> DagShortestPath.run(g, new Metrics()));
    }

    @Test
    void singleVertexHasZeroDistance() {
        Graph g = new Graph(1, true);
        DagShortestPath.Result res = DagShortestPath.run(g, new Metrics());
        assertEquals(0, res.dist[0]);
        assertEquals(0, res.sumDist);
        assertEquals(List.of(0), res.pathTo(0));
    }

    private static long pathLength(Graph g, List<Integer> path) {
        long sum = 0;
        for (int i = 0; i + 1 < path.size(); i++) {
            int a = path.get(i);
            int b = path.get(i + 1);
            long best = INF;
            for (Edge e : g.adj(a)) {
                if (e.to == b && e.w < best) {
                    best = e.w;
                }
            }
            sum += best;
        }
        return sum;
    }
}
