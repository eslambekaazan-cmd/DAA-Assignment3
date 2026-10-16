# DAA Assignment 3 - City Network & Job Scheduler (Graphs)

Implementation of graph algorithms including Disjoint Set Union (DSU), Kruskal, Prim, Kosaraju, Kahn, and shortest paths in a Directed Acyclic Graph (DAG).

The project includes algorithm implementations, operation counters, performance benchmarks, automated tests, and experimental analysis.

## Requirements

- Java 17 or newer
- Maven 3.8+
- Python 3 with matplotlib (for generating plots)

## How to Build and Run

### Run all tests

```bash
mvn test
```

### Compile the project

```bash
mvn -q compile
```

### Run the benchmark

```bash
java -Xmx2g -cp target/classes benchmark.Benchmark
```

The benchmark generates results for 10 graphs using 5 algorithms and saves the measurements in `results/results.csv`.

### Generate plots

```bash
python3 scripts/analyze.py
```

The script analyzes the benchmark results and generates three plots in `results/plots/`.

## Project Structure

```text
DAA-Assignment3/
├── data/
│   ├── small_undirected.txt
│   ├── disconnected.txt
│   ├── cyclic.txt
│   └── dag.txt
├── results/
│   ├── results.csv
│   ├── analysis.txt
│   └── plots/
│       ├── 1_operations_vs_n.png
│       ├── 2_scans_per_edge.png
│       └── 3_kruskal_share.png
├── scripts/
│   └── analyze.py
├── src/
│   ├── main/
│   │   └── java/
│   │       ├── algorithms/
│   │       ├── benchmark/
│   │       ├── graph/
│   │       └── metrics/
│   └── test/
│       └── java/
├── pom.xml
├── README.md
└── REPORT.md
```

## Implemented Algorithms

### 1. Disjoint Set Union (DSU)

Implements the Disjoint Set Union data structure with path compression and union operations.

Main operations:

- `makeSet()`
- `find()`
- `union()`
- `components()`

DSU is used in Kruskal's algorithm to detect cycles and manage connected components.

### 2. Kruskal's Algorithm

Computes a Minimum Spanning Tree (MST) for an undirected weighted graph.

The algorithm sorts edges by weight and uses DSU to select edges without creating cycles.

For disconnected graphs, it produces a minimum spanning forest.

Time complexity: `O(E log E)`.

### 3. Prim's Algorithm

Computes a Minimum Spanning Tree using a priority queue.

The algorithm repeatedly selects the minimum-weight edge connecting a visited vertex to an unvisited vertex.

For disconnected graphs, it constructs a minimum spanning forest.

Time complexity: `O(E log V)` with a binary heap.

### 4. Kosaraju's Algorithm

Finds Strongly Connected Components (SCCs) in directed graphs.

The implementation uses iterative depth-first search to avoid stack overflow on large graphs.

Main steps:

1. Perform DFS to determine finishing order.
2. Reverse all graph edges.
3. Process vertices in reverse finishing order.
4. Identify strongly connected components.

Time complexity: `O(V + E)`.

### 5. Kahn's Algorithm

Performs topological sorting on a Directed Acyclic Graph.

The algorithm calculates incoming edge counts and processes vertices with zero in-degree.

It also detects cycles when a valid topological ordering does not exist.

Time complexity: `O(V + E)`.

### 6. Shortest Paths in a DAG

Computes shortest paths in a Directed Acyclic Graph using topological ordering.

The implementation supports negative edge weights because DAGs do not contain directed cycles.

Time complexity: `O(V + E)`.

## Input Format

Graph files use the following format:

```text
n m directed|undirected
u v w
u v w
...
```

Where:

- `n` is the number of vertices.
- `m` is the number of edges.
- `directed|undirected` specifies the graph type.
- `u` is the starting vertex.
- `v` is the destination vertex.
- `w` is the edge weight.

Vertices are numbered from `0` to `n-1`.

Empty lines and lines beginning with `#` are ignored.

## Input Graphs

| File | Description |
|---|---|
| `small_undirected.txt` | Connected undirected graph with MST weight 14 |
| `disconnected.txt` | Graph with 4 components, parallel edges, a self-loop, and an isolated vertex |
| `cyclic.txt` | Directed graph containing cycles and 4 strongly connected components |
| `dag.txt` | Directed acyclic graph containing a negative edge |

## Testing

JUnit 5 is used for automated testing.

### DsuTest

Tests DSU operations, connected components, random unions, and operation counters.

### MstTest

Compares Kruskal and Prim on 100 randomly generated connected graphs.

Also verifies MST weights using brute-force calculations on small graphs.

### SccTest

Compares Kosaraju's algorithm against a BFS-based reference implementation.

Includes tests with 100,000 vertices to verify iterative DFS behavior.

### TopoTest

Tests topological ordering, cycle detection, and order validation.

### DagPathsTest

Compares shortest-path results against Bellman-Ford on randomly generated DAGs with negative weights.

Also tests path reconstruction.

### GraphTest

Tests graph file reading, directed and undirected graphs, self-loops, parallel edges, and invalid input handling.

To execute all tests:

```bash
mvn test
```

## Benchmark and Performance Analysis

The benchmark evaluates graph algorithms on generated graphs of different sizes.

The following metrics are recorded:

- Execution time (`time_ms`)
- Edge scans (`edgeScans`)
- Comparisons (`comparisons`)
- DSU operations (`dsuOps`)
- Maximum DFS depth (`maxDepth`)

A fixed random seed is used to produce reproducible graph inputs.

The benchmark includes correctness checks for algorithm outputs and operation counts.

Successful execution prints:

```text
ALL CHECKS PASSED
```

## Results

Benchmark measurements are stored in:

```text
results/results.csv
```

Additional analysis is stored in:

```text
results/analysis.txt
```

Three plots are generated:

1. `1_operations_vs_n.png` — operation counts relative to graph size.
2. `2_scans_per_edge.png` — edge scan behavior.
3. `3_kruskal_share.png` — Kruskal operation distribution.

The experimental results are compared with theoretical time complexities.

Detailed explanations, complexity analysis, and algorithm comparisons are available in [REPORT.md](REPORT.md).

## Algorithm Complexity Summary

| Algorithm | Time Complexity |
|---|---|
| DSU | `O(α(V))` amortized per operation |
| Kruskal | `O(E log E)` |
| Prim | `O(E log V)` |
| Kosaraju | `O(V + E)` |
| Kahn | `O(V + E)` |
| DAG Shortest Paths | `O(V + E)` |

## Git Repository

The project is maintained using Git and hosted on GitHub.

Repository name: `DAA-Assignment3`

## Development Tools

Java, Maven, JUnit 5, Python, and matplotlib were used for implementation, testing, and analysis. AI assistance was used during development and documentation.

## Conclusion

This project demonstrates the implementation and evaluation of fundamental graph algorithms.

The algorithms are tested on connected, disconnected, directed, and acyclic graphs. Automated tests and benchmarks are used to evaluate correctness and computational complexity.

The results illustrate the practical differences between graph algorithms and their theoretical performance characteristics.