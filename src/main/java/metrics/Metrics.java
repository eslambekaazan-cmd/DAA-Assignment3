package metrics;

public class Metrics {
    public long dsuOps;
    public long edgeScans;
    public long comparisons;
    public int maxDepth;

    public void updateDepth(int depth) {
        if (depth > maxDepth) {
            maxDepth = depth;
        }
    }

    public void reset() {
        dsuOps = 0;
        edgeScans = 0;
        comparisons = 0;
        maxDepth = 0;
    }
}