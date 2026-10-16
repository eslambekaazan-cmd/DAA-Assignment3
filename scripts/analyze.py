import csv
import math
import os

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

CSV_FILE = "results/results.csv"
PLOT_DIR = "results/plots"
ALGOS = ["Kruskal", "Prim", "Kosaraju", "Kahn", "DagShortestPath"]
COLORS = {"Kruskal": "tab:blue", "Prim": "tab:orange", "Kosaraju": "tab:green",
          "Kahn": "tab:red", "DagShortestPath": "tab:purple"}


def load():
    data = {}
    with open(CSV_FILE) as f:
        for r in csv.DictReader(f):
            dens = "sparse" if r["graph"].endswith("sparse") else "dense"
            row = {k: (float(v) if k == "time_ms" else int(v)) for k, v in r.items()
                   if k not in ("algorithm", "graph")}
            data.setdefault((r["algorithm"], dens), []).append(row)
    for rows in data.values():
        rows.sort(key=lambda x: x["n"])
    return data


def slope(xs, ys):
    """least squares slope of log(y) against log(x)"""
    lx = [math.log(x) for x in xs]
    ly = [math.log(y) for y in ys]
    mx = sum(lx) / len(lx)
    my = sum(ly) / len(ly)
    return sum((a - mx) * (b - my) for a, b in zip(lx, ly)) / sum((a - mx) ** 2 for a in lx)


def total_ops(r):
    return r["dsu_ops"] + r["edge_scans"] + r["comparisons"]


def main():
    data = load()
    os.makedirs(PLOT_DIR, exist_ok=True)

    formulas = [
        ("Kruskal dsuOps / E", "Kruskal", "dsu_ops", lambda r: r["m"]),
        ("Kruskal dsuOps / edgeScans", "Kruskal", "dsu_ops", lambda r: r["edge_scans"]),
        ("Kruskal comparisons / (E log2 E)", "Kruskal", "comparisons", lambda r: r["m"] * math.log2(r["m"])),
        ("Prim edgeScans / (2E)", "Prim", "edge_scans", lambda r: 2 * r["m"]),
        ("Prim comparisons / (E log2 E)", "Prim", "comparisons", lambda r: r["m"] * math.log2(r["m"])),
        ("Kosaraju edgeScans / (V+E)", "Kosaraju", "edge_scans", lambda r: r["n"] + r["m"]),
        ("Kahn edgeScans / (V+E)", "Kahn", "edge_scans", lambda r: r["n"] + r["m"]),
        ("DagShortestPath edgeScans / (V+E)", "DagShortestPath", "edge_scans", lambda r: r["n"] + r["m"]),
    ]
    print("## Counter / formula")
    print("| ratio | density | " + " | ".join(str(r["n"]) for r in data[("Kruskal", "sparse")]) + " |")
    print("|---|---|" + "---|" * 5)
    for name, algo, counter, f in formulas:
        for dens in ("sparse", "dense"):
            vals = [r[counter] / f(r) for r in data[(algo, dens)]]
            print(f"| {name} | {dens} | " + " | ".join(f"{v:.3f}" for v in vals) + " |")

    print("\n## Slopes (log-log, fitted on n = 1000 ... 100000)")
    print("| algorithm | counter | sparse | dense |")
    print("|---|---|---|---|")
    for algo in ALGOS:
        for counter in ("edge_scans", "comparisons", "dsu_ops"):
            s = {}
            for dens in ("sparse", "dense"):
                rows = data[(algo, dens)]
                ys = [r[counter] for r in rows]
                s[dens] = f"{slope([r['n'] for r in rows], ys):.3f}" if all(y > 0 for y in ys) else "-"
            if s["sparse"] != "-":
                print(f"| {algo} | {counter} | {s['sparse']} | {s['dense']} |")
    for algo in ALGOS:
        for dens in ("sparse", "dense"):
            rows = data[(algo, dens)]
            print(f"total ops slope {algo} {dens}: {slope([r['n'] for r in rows], [total_ops(r) for r in rows]):.3f}")

    print("\n## Kruskal edgeScans / m")
    for dens in ("sparse", "dense"):
        print(dens, [f"{r['edge_scans'] / r['m']:.3f}" for r in data[("Kruskal", dens)]])
    print("\n## max_depth Kosaraju")
    for dens in ("sparse", "dense"):
        print(dens, [(r["n"], r["max_depth"]) for r in data[("Kosaraju", dens)]])

    fig, ax = plt.subplots(figsize=(8, 5.5))
    for algo in ALGOS:
        for dens, ls, mk in (("sparse", "-", "o"), ("dense", "--", "s")):
            rows = data[(algo, dens)]
            ax.plot([r["n"] for r in rows], [total_ops(r) for r in rows], ls, marker=mk, ms=4,
                    color=COLORS[algo], label=f"{algo} ({dens})")
    ax.set_xscale("log")
    ax.set_yscale("log")
    ax.set_xlabel("n (number of vertices)")
    ax.set_ylabel("operations = dsuOps + edgeScans + comparisons")
    ax.set_title("Operations vs n (log-log)\nKosaraju and DagShortestPath lie on the same line (both 2m operations)", fontsize=10)
    ax.grid(True, which="both", alpha=0.3)
    ax.legend(fontsize=7, ncol=4, loc="upper center", bbox_to_anchor=(0.5, -0.14))
    fig.savefig(f"{PLOT_DIR}/1_operations_vs_n.png", dpi=150, bbox_inches="tight")
    plt.close(fig)

    fig, ax = plt.subplots(figsize=(8, 5.5))
    for algo in ALGOS:
        for dens, ls, mk in (("sparse", "-", "o"), ("dense", "--", "s")):
            rows = data[(algo, dens)]
            ax.plot([r["n"] for r in rows], [r["edge_scans"] / r["m"] for r in rows], ls, marker=mk, ms=4,
                    color=COLORS[algo], label=f"{algo} ({dens})")
    ax.set_xscale("log")
    ax.set_xlabel("n (number of vertices)")
    ax.set_ylabel("edgeScans / m")
    ax.set_ylim(0, 2.4)
    ax.set_title("Scans per edge (flat line = linear work)\nKosaraju and Prim lie on 2.0, Kahn and DagShortestPath on 1.0", fontsize=10)
    ax.grid(True, which="both", alpha=0.3)
    ax.legend(fontsize=7, ncol=4, loc="upper center", bbox_to_anchor=(0.5, -0.14))
    fig.savefig(f"{PLOT_DIR}/2_scans_per_edge.png", dpi=150, bbox_inches="tight")
    plt.close(fig)

    fig, ax = plt.subplots(figsize=(8, 5.5))
    for dens, mk in (("sparse", "o"), ("dense", "s")):
        rows = data[("Kruskal", dens)]
        ax.plot([r["n"] for r in rows], [100 * r["edge_scans"] / r["m"] for r in rows], "-", marker=mk,
                label=f"Kruskal {dens} (m = {'4n' if dens == 'sparse' else '20n'})")
    ax.set_xscale("log")
    ax.set_ylim(0, 105)
    ax.set_xlabel("n (number of vertices)")
    ax.set_ylabel("share of edges Kruskal looked at, %")
    ax.set_title("Share of edges examined by Kruskal (edgeScans / m)")
    ax.grid(True, which="both", alpha=0.3)
    ax.legend()
    fig.tight_layout()
    fig.savefig(f"{PLOT_DIR}/3_kruskal_share.png", dpi=150)
    plt.close(fig)
    print("\nplots written to", PLOT_DIR)


if __name__ == "__main__":
    main()
