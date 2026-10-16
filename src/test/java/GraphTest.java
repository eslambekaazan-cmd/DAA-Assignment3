
import graph.Graph;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphTest {

    @Test
    void readsUndirectedFile() throws IOException {
        Graph g = Graph.fromFile("data/small_undirected.txt");
        assertEquals(6, g.n());
        assertEquals(9, g.m());
        assertFalse(g.isDirected());
        assertEquals(3, g.adj(1).size());
    }

    @Test
    void readsDirectedFile() throws IOException {
        Graph g = Graph.fromFile("data/cyclic.txt");
        assertEquals(8, g.n());
        assertEquals(10, g.m());
        assertTrue(g.isDirected());
        assertEquals(2, g.adj(6).size());
    }

    @Test
    void selfLoopsAndParallelEdgesAreKept() throws IOException {
        Graph g = Graph.fromFile("data/disconnected.txt");
        assertEquals(6, g.m());
        assertEquals(2, g.adj(3).size());
        assertEquals(1, g.adj(5).size());
        assertEquals(0, g.adj(6).size());
    }

    @Test
    void reverseTurnsEdgesAround() throws IOException {
        Graph g = Graph.fromFile("data/dag.txt");
        Graph r = g.reverse();
        assertEquals(g.m(), r.m());
        assertEquals(0, r.adj(0).size());
        assertEquals(2, r.adj(3).size());
    }

    @Test
    void badInputIsReportedWithIOException() throws IOException {
        Path dir = Files.createTempDirectory("graphtest");
        Path bad = dir.resolve("bad.txt");
        Files.writeString(bad, "3 2 sideways\n0 1 1\n0 2 1\n");
        assertThrows(IOException.class, () -> Graph.fromFile(bad.toString()));

        Path shortFile = dir.resolve("short.txt");
        Files.writeString(shortFile, "3 2 directed\n0 1 1\n");
        assertThrows(IOException.class, () -> Graph.fromFile(shortFile.toString()));
    }

    @Test
    void edgeWithWrongVertexIsRejected() {
        Graph g = new Graph(3, true);
        assertThrows(IllegalArgumentException.class, () -> g.addEdge(0, 3, 1));
    }
}
