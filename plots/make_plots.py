# reads results/*.csv and makes the png plots in plots/
# usage (from repo root): python3 plots/make_plots.py
import csv
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt


def read(path):
    with open(path) as f:
        return list(csv.DictReader(f))


# 1. LCS and edit distance
dp = read("results/dp.csv")
fig, ax = plt.subplots(1, 2, figsize=(11, 4))
for algo in ["LCS", "EditDistance"]:
    rows = [r for r in dp if r["algo"] == algo]
    n = [int(r["n"]) for r in rows]
    ms = [int(r["avg_ns"]) / 1e6 for r in rows]
    ax[0].plot(n, ms, marker="o", label=algo)
    per_cell = [int(r["avg_ns"]) / int(r["cells"]) for r in rows]
    ax[1].plot(n, per_cell, marker="o", label=algo)
ax[0].set_title("LCS and Edit Distance: time vs string length")
ax[0].set_xlabel("string length n (both strings)")
ax[0].set_ylabel("average time (ms)")
ax[0].legend()
ax[0].grid(True)
ax[1].set_title("time per table cell (should be ~constant)")
ax[1].set_xlabel("string length n")
ax[1].set_ylabel("ns per cell")
ax[1].legend()
ax[1].grid(True)
plt.tight_layout()
plt.savefig("plots/dp_time.png", dpi=130)
plt.close()


# 2. string matching (present / absent)
def match_plot(csv_path, cases, titles, out_png, logy=False):
    m = read(csv_path)
    fig, ax = plt.subplots(1, len(cases), figsize=(5.5 * len(cases), 4))
    if len(cases) == 1:
        ax = [ax]
    for a, cs, ttl in zip(ax, cases, titles):
        for algo in ["Naive", "KMP", "RabinKarp"]:
            rows = [r for r in m if r["case"] == cs and r["algo"] == algo]
            n = [int(r["n"]) for r in rows]
            ms = [int(r["avg_ns"]) / 1e6 for r in rows]
            a.plot(n, ms, marker="o", label=algo)
        a.set_title(ttl)
        a.set_xlabel("text length n")
        a.set_ylabel("average time (ms)")
        if logy:
            a.set_yscale("log")
        a.legend()
        a.grid(True)
    plt.tight_layout()
    plt.savefig(out_png, dpi=130)
    plt.close()


match_plot("results/matching.csv", ["present", "absent"],
           ["pattern present (length 50)", "pattern absent (length 50)"],
           "plots/matching_time.png")

# extra: bad cases
match_plot("results/matching_worst.csv", ["worstA", "worstB"],
           ["text a^n, pattern a^99 b (no match)", "text a^n, pattern a^100 (match everywhere)"],
           "plots/matching_worst.png", logy=True)

# 3. kasai
k = read("results/kasai.csv")
n = [int(r["n"]) for r in k]
ms = [int(r["avg_ns"]) / 1e6 for r in k]
cm = [int(r["cmp"]) for r in k]
fig, ax = plt.subplots(1, 2, figsize=(11, 4))
ax[0].plot(n, ms, marker="o", color="tab:green")
ax[0].set_title("Kasai: time vs string length")
ax[0].set_xlabel("string length n")
ax[0].set_ylabel("average time (ms)")
ax[0].grid(True)
ax[1].plot(n, cm, marker="o", color="tab:green", label="measured char comparisons")
ax[1].plot(n, [2 * x for x in n], linestyle="--", color="gray", label="2n")
ax[1].set_title("Kasai: character comparisons")
ax[1].set_xlabel("string length n")
ax[1].set_ylabel("comparisons")
ax[1].legend()
ax[1].grid(True)
plt.tight_layout()
plt.savefig("plots/kasai_time.png", dpi=130)
plt.close()
print("done")
