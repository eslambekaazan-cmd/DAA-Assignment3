package algorithms;

public class Dsu {
    private int[] parent;
    private int[] rank;
    private int components;

    public long findCalls;
    public long unionCalls;

    public Dsu(int n) {
        makeSet(n);
    }

    public void makeSet(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
        components = n;
        findCalls = 0;
        unionCalls = 0;
    }

    public int find(int x) {
        findCalls++;
        return root(x);
    }

    public boolean union(int x, int y) {
        int a = root(x);
        int b = root(y);
        if (a == b) {
            return false;
        }
        unionCalls++;
        if (rank[a] < rank[b]) {
            parent[a] = b;
        } else if (rank[a] > rank[b]) {
            parent[b] = a;
        } else {
            parent[b] = a;
            rank[a]++;
        }
        components--;
        return true;
    }

    public int components() {
        return components;
    }

    private int root(int x) {
        int r = x;
        while (parent[r] != r) {
            r = parent[r];
        }
        while (parent[x] != r) {
            int next = parent[x];
            parent[x] = r;
            x = next;
        }
        return r;
    }
}