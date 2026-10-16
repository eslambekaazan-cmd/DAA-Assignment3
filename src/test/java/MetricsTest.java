import metrics.Metrics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricsTest {

    @Test
    void resetClearsAllCounters() {
        Metrics m = new Metrics();

        m.dsuOps = 10;
        m.edgeScans = 20;
        m.comparisons = 30;
        m.updateDepth(5);

        m.reset();

        assertEquals(0, m.dsuOps);
        assertEquals(0, m.edgeScans);
        assertEquals(0, m.comparisons);
        assertEquals(0, m.maxDepth);
    }
}
