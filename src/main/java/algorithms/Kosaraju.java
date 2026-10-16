package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.Arrays;
import java.util.List;

public class Kosaraju {

    public static class Result {
        public final int count;
        public final int[] comp;

        Result(int count, int[] comp) {
            this.count = count;
            this.comp = comp;
        }
    }

    public static Result run(Graph g, Metrics metrics) {
        int n = g.n();
        boolean[] visited = new boolean[n];
        int[] ptr = new int[n];
        int[] stack = new int[n];
        int[] finish = new int[n];

        int pos = 0;
        for (int s = 0; s < n; s++) {
            if (!visited[s]) {
                pos = dfs(g, s, visited, ptr, stack, finish, pos, null, 0, metrics);
            }
        }

        Graph rev = g.reverse();
        Arrays.fill(visited, false);
        Arrays.fill(ptr, 0);
        int[] comp = new int[n];
        int count = 0;
        for (int i = n - 1; i >= 0; i--) {
            int s = finish[i];
            if (!visited[s]) {
                dfs(rev, s, visited, ptr, stack, null, 0, comp, count, metrics);
                count++;
            }
        }
        return new Result(count, comp);
    }

    private static int dfs(Graph g, int s, boolean[] visited, int[] ptr, int[] stack,
                           int[] out, int pos, int[] comp, int compId, Metrics metrics) {
        int sp = 0;
        stack[sp++] = s;
        visited[s] = true;
        if (comp != null) {
            comp[s] = compId;
        }
        metrics.updateDepth(sp);

        while (sp > 0) {
            int u = stack[sp - 1];
            List<Edge> list = g.adj(u);
            if (ptr[u] < list.size()) {
                Edge e = list.get(ptr[u]++);
                metrics.edgeScans++;
                int v = e.to;
                if (!visited[v]) {
                    visited[v] = true;
                    if (comp != null) {
                        comp[v] = compId;
                    }
                    stack[sp++] = v;
                    metrics.updateDepth(sp);
                }
            } else {
                sp--;
                if (out != null) {
                    out[pos++] = u;
                }
            }
        }
        return pos;
    }
}