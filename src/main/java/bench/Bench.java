package bench;

import dp.EditDistance;
import dp.LCS;
import matching.KMP;
import matching.Naive;
import matching.RabinKarp;
import suffix.Kasai;
import suffix.SimpleSA;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

public class Bench {
    static final String AB = "abcdefghijklmnopqrstuvwxyz";   // alphabet for all experiments
    static final int RUNS = 5;
    static final int WARM = 100;    // min number of untimed warm up runs (jit)
    static final int PLEN = 50;     // pattern length for the normal matching runs

    static volatile long sink;      // so the jvm cant throw the calls away

    interface Job {
        void run();
    }

    static String gen(int n, long seed) {
        Random r = new Random(seed);
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(AB.charAt(r.nextInt(AB.length())));
        return sb.toString();
    }

    static String rep(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    // average of RUNS timed runs in ns
    static long time(Job j) {
        long start = System.nanoTime();
        int w = 0;
        while (w < WARM || System.nanoTime() - start < 300_000_000L) {   // warm up at least 300 ms
            j.run();
            w++;
        }
        long tot = 0;
        for (int i = 0; i < RUNS; i++) {
            long s = System.nanoTime();
            j.run();
            tot += System.nanoTime() - s;
        }
        return tot / RUNS;
    }

    public static void main(String[] args) throws IOException {
        runDP();
        runMatching();
        runKasai();
    }

    static void runDP() throws IOException {
        int[] sizes = {100, 500, 1000};
        PrintWriter out = new PrintWriter("results/dp.csv");
        out.println("algo,n,avg_ns,cells");
        System.out.println("== LCS / Edit Distance ==");
        for (int n : sizes) {
            String a = gen(n, 42), b = gen(n, 43);

            long t1 = time(() -> sink = LCS.length(a, b));
            LCS.length(a, b);
            long c1 = LCS.cells;
            long t2 = time(() -> sink = EditDistance.dist(a, b));
            EditDistance.dist(a, b);
            long c2 = EditDistance.cells;

            out.println("LCS," + n + "," + t1 + "," + c1);
            out.println("EditDistance," + n + "," + t2 + "," + c2);
            System.out.printf("n=%d  LCS %.3f ms (len %d)   ED %.3f ms (dist %d)%n",
                    n, t1 / 1e6, LCS.length(a, b), t2 / 1e6, EditDistance.dist(a, b));
        }
        out.close();
    }

    static void matchRow(PrintWriter out, String cs, int n, String t, String p) {
        long t1 = time(() -> sink = Naive.search(t, p).size());
        List<Integer> r1 = Naive.search(t, p);
        long c1 = Naive.comps;

        long t2 = time(() -> sink = KMP.search(t, p).size());
        List<Integer> r2 = KMP.search(t, p);
        long c2 = KMP.comps, pc2 = KMP.prepComps;

        long t3 = time(() -> sink = RabinKarp.search(t, p).size());
        List<Integer> r3 = RabinKarp.search(t, p);
        long c3 = RabinKarp.comps, up3 = RabinKarp.updates, fp3 = RabinKarp.falsePos;

        if (!r1.equals(r2) || !r2.equals(r3)) {
            throw new IllegalStateException("algorithms disagree on " + cs + " n=" + n);
        }
        int k = r1.size();
        out.println(cs + "," + n + ",Naive," + t1 + "," + c1 + ",0,0,0," + k);
        out.println(cs + "," + n + ",KMP," + t2 + "," + c2 + "," + pc2 + ",0,0," + k);
        out.println(cs + "," + n + ",RabinKarp," + t3 + "," + c3 + ",0," + up3 + "," + fp3 + "," + k);
        System.out.printf("%-8s n=%-7d naive %9.3f ms  kmp %9.3f ms  rk %9.3f ms  (matches %d)%n",
                cs, n, t1 / 1e6, t2 / 1e6, t3 / 1e6, k);
    }

    static void runMatching() throws IOException {
        int[] sizes = {10000, 50000, 100000};
        PrintWriter out = new PrintWriter("results/matching.csv");
        PrintWriter out2 = new PrintWriter("results/matching_worst.csv");
        String head = "case,n,algo,avg_ns,cmp,prep_cmp,hash_updates,false_pos,matches";
        out.println(head);
        out2.println(head);
        System.out.println("== String matching ==");
        for (int n : sizes) {
            String t = gen(n, 44);
            String pres = t.substring(n / 2, n / 2 + PLEN);
            String abs = pres.substring(0, PLEN - 1) + "#";    // '#' is not in the alphabet
            matchRow(out, "present", n, t, pres);
            matchRow(out, "absent", n, t, abs);
        }
        System.out.println("== String matching, bad cases (extra) ==");
        for (int n : sizes) {
            String t = rep('a', n);
            matchRow(out2, "worstA", n, t, rep('a', 99) + "b");   // no match, long partial matches
            matchRow(out2, "worstB", n, t, rep('a', 100));        // match everywhere
        }
        out.close();
        out2.close();
    }

    static void runKasai() throws IOException {
        int[] sizes = {1000, 5000, 10000};
        PrintWriter out = new PrintWriter("results/kasai.csv");
        out.println("n,avg_ns,cmp");
        System.out.println("== Kasai ==");
        for (int n : sizes) {
            String s = gen(n, 45);
            int[] sa = SimpleSA.build(s);      // built outside the timed part

            long t = time(() -> sink = Kasai.build(s, sa)[n / 2]);
            int[] lcp = Kasai.build(s, sa);
            long c = Kasai.comps;

            // quick brute force check so we know the timed output is right
            for (int i = 1; i < n; i++) {
                int h = 0, x = sa[i - 1], y = sa[i];
                while (x + h < n && y + h < n && s.charAt(x + h) == s.charAt(y + h)) h++;
                if (h != lcp[i]) throw new IllegalStateException("bad lcp at " + i);
            }
            out.println(n + "," + t + "," + c);
            System.out.printf("n=%d  kasai %.3f ms  comps %d%n", n, t / 1e6, c);
        }
        out.close();
    }
}
