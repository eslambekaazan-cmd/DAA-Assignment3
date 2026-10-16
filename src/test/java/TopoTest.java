
import algorithms.Kahn;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopoTest {

    @Test
    void validOrderOnRandomDags() {
        Random r = new Random(11);
        for (int round = 0; round < 50; round++) {
            int n = 1 + r.nextInt(100);
            Graph g = TestGraphs.randomDag(r, n, r.nextInt(4 * n), 1, 10);
            Metrics m = new Metrics();
            Kahn.Result res = Kahn.run(g, m);
            assertFalse(res.hasCycle);
            assertEquals(n, res.processed);
            assertTrue(Kahn.isValidOrder(g, res.order));
            assertEquals(g.m(), m.edgeScans);
        }
    }

    @Test
    void dagFileHasOrder() throws IOException {
        Graph g = Graph.fromFile("data/dag.txt");
        Kahn.Result res = Kahn.run(g, new Metrics());
        assertFalse(res.hasCycle);
        assertTrue(Kahn.isValidOrder(g, res.order));
    }

    @Test
    void cycleIsDetectedAndNoOrderIsReturned() throws IOException {
        Graph g = Graph.fromFile("data/cyclic.txt");
        Kahn.Result res = Kahn.run(g, new Metrics());
        assertTrue(res.hasCycle);
        assertNull(res.order);
        assertTrue(res.processed < g.n());
    }

    @Test
    void selfLoopAndTwoCycleAreCycles() {
        Graph loop = new Graph(3, true);
        loop.addEdge(0, 1, 1);
        loop.addEdge(2, 2, 1);
        assertTrue(Kahn.run(loop, new Metrics()).hasCycle);

        Graph two = new Graph(2, true);
        two.addEdge(0, 1, 1);
        two.addEdge(1, 0, 1);
        Kahn.Result res = Kahn.run(two, new Metrics());
        assertTrue(res.hasCycle);
        assertEquals(0, res.processed);
    }

    @Test
    void validatorRejectsWrongOrders() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        assertTrue(Kahn.isValidOrder(g, new int[]{0, 1, 2}));
        assertFalse(Kahn.isValidOrder(g, new int[]{2, 1, 0}));
        assertFalse(Kahn.isValidOrder(g, new int[]{0, 0, 2}));
        assertFalse(Kahn.isValidOrder(g, new int[]{0, 1}));
        assertFalse(Kahn.isValidOrder(g, null));
    }

    @Test
    void parallelEdgesAndIsolatedVerticesAreFine() {
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 1);
        g.addEdge(0, 1, 2);
        Kahn.Result res = Kahn.run(g, new Metrics());
        assertFalse(res.hasCycle);
        assertEquals(4, res.processed);
        assertTrue(Kahn.isValidOrder(g, res.order));
    }

    @Test
    void undirectedGraphIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Kahn.run(new Graph(3, false), new Metrics()));
    }
}
