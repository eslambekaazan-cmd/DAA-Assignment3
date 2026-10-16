package benchmark;

import algorithms.DagShortestPath;
import algorithms.Kahn;
import algorithms.Kosaraju;
import algorithms.Kruskal;
import algorithms.MstResult;
import algorithms.Prim;
import graph.Graph;
import metrics.Metrics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class Benchmark {

    static final int[] SIZES = {1000, 3162, 10000, 31623, 100000};
    static final int[] FACTORS = {4, 20};
    static final int RUNS = 5;

    static final long[] REF_MST = {147970, 30363, 468911, 97589, 1486139, 309550, 4715515, 968517, 15012094, 3056681};
    static final int[] REF_SCC = {27, 1, 137, 1, 373, 1, 1188, 1, 3923, 1};
    static final long[] REF_DAG = {396274, 252411, 1271911, 723157, 3941828, 2381342, 12551891, 7376612, 40096539, 23512087};

    static class Run<T> {
        T result;
        Metrics metrics;
        double medianMs;
    }

    public static void main(String[] args) throws IOException {
        String outFile = args.length > 0 ? args[0] : "results/results.csv";
        List<String> rows = new ArrayList<>();

        System.out.println("algorithm        graph            n        m          time_ms  dsu_ops    edge_scans  comparisons  max_depth  result");

        for (int si = 0; si < SIZES.length; si++) {
            for (int fi = 0; fi < FACTORS.length; fi++) {
                int idx = si * FACTORS.length + fi;
                int n = SIZES[si];
                int m = n * FACTORS[fi];
                String name = "n" + n + (fi == 0 ? "_sparse" : "_dense");

                Graph u = GraphGenerator.undirected(n, m);
                check(u.m() == m, name + ": generator made " + u.m() + " edges, wanted " + m);

                Run<MstResult> kr = measure(mt -> Kruskal.run(u, mt));
                Run<MstResult> pr = measure(mt -> Prim.run(u, mt));
                check(kr.result.totalWeight == pr.result.totalWeight,
                        name + ": Kruskal " + kr.result.totalWeight + " != Prim " + pr.result.totalWeight);
                check(kr.result.edges.size() == n - 1 && pr.result.edges.size() == n - 1,
                        name + ": the tree does not have n-1 edges");
                check(kr.result.totalWeight == REF_MST[idx],
                        name + ": MST weight " + kr.result.totalWeight + " but reference is " + REF_MST[idx]);
                rows.add(row("Kruskal", name, n, m, kr.medianMs, kr.metrics, kr.result.totalWeight));
                rows.add(row("Prim", name, n, m, pr.medianMs, pr.metrics, pr.result.totalWeight));

                Graph d = GraphGenerator.directed(n, m);
                Run<Kosaraju.Result> ko = measure(mt -> Kosaraju.run(d, mt));
                check(ko.result.count == REF_SCC[idx],
                        name + ": SCC count " + ko.result.count + " but reference is " + REF_SCC[idx]);
                check(ko.metrics.edgeScans == 2L * m,
                        name + ": Kosaraju scanned " + ko.metrics.edgeScans + " edges, must be 2m = " + (2L * m));

                rows.add(row("Kosaraju", name, n, m, ko.medianMs, ko.metrics, ko.result.count));

                Graph a = GraphGenerator.dag(n, m);
                Run<Kahn.Result> ka = measure(mt -> Kahn.run(a, mt));
                check(!ka.result.hasCycle && ka.result.processed == n, name + ": Kahn found a cycle in a DAG");
                check(Kahn.isValidOrder(a, ka.result.order), name + ": topological order is not valid");
                check(ka.metrics.edgeScans == m, name + ": Kahn scanned " + ka.metrics.edgeScans + " edges, must be m");
                rows.add(row("Kahn", name, n, m, ka.medianMs, ka.metrics, ka.result.processed));

                Run<DagShortestPath.Result> ds = measure(mt -> DagShortestPath.run(a, mt));
                check(ds.result.sumDist == REF_DAG[idx],
                        name + ": sum of distances " + ds.result.sumDist + " but reference is " + REF_DAG[idx]);
                check(ds.metrics.edgeScans == m, name + ": DAG relaxation scanned " + ds.metrics.edgeScans + " edges, must be m");
                rows.add(row("DagShortestPath", name, n, m, ds.medianMs, ds.metrics, ds.result.sumDist));
            }
        }

        check(rows.size() == 50, "expected 50 rows but got " + rows.size());

        Path out = Path.of(outFile);
        if (out.getParent() != null) {
            Files.createDirectories(out.getParent());
        }
        List<String> lines = new ArrayList<>();
        lines.add("algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result");
        lines.addAll(rows);
        Files.write(out, lines);
        System.out.println();
        System.out.println("ALL CHECKS PASSED: MST weights equal, orders valid, scans are 2m/m/m, reference values match.");
        System.out.println("Written " + rows.size() + " rows to " + out);
    }

    static <T> Run<T> measure(Function<Metrics, T> algo) {
        double[] times = new double[RUNS];
        Run<T> run = new Run<>();
        for (int i = 0; i < RUNS; i++) {
            Metrics mt = new Metrics();
            long start = System.nanoTime();
            T res = algo.apply(mt);
            times[i] = (System.nanoTime() - start) / 1e6;
            run.result = res;
            run.metrics = mt;
        }
        Arrays.sort(times);
        run.medianMs = times[RUNS / 2];
        return run;
    }

    static String row(String algo, String graph, int n, int m, double ms, Metrics mt, long result) {
        String line = String.format(Locale.ROOT, "%s,%s,%d,%d,%.3f,%d,%d,%d,%d,%d",
                algo, graph, n, m, ms, mt.dsuOps, mt.edgeScans, mt.comparisons, mt.maxDepth, result);
        System.out.println(String.format(Locale.ROOT, "%-16s %-16s %-8d %-9d %-8.1f %-10d %-11d %-12d %-10d %d",
                algo, graph, n, m, ms, mt.dsuOps, mt.edgeScans, mt.comparisons, mt.maxDepth, result));
        return line;
    }

    static void check(boolean ok, String message) {
        if (!ok) {
            throw new IllegalStateException("CHECK FAILED: " + message);
        }
    }
}