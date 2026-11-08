package matching;

import java.util.ArrayList;
import java.util.List;

public class RabinKarp {
    static final long BASE = 256;
    static final long MOD = 1_000_000_007L;

    public static long comps = 0;       // chars compared while verifying
    public static long updates = 0;     // rolling hash updates
    public static long falsePos = 0;    // hash equal but strings different

    public static List<Integer> search(String t, String p) {
        return search(t, p, MOD);
    }

    // mod is a parameter so the tests can use a tiny one and force collisions
    public static List<Integer> search(String t, String p, long mod) {
        List<Integer> res = new ArrayList<>();
        comps = 0;
        updates = 0;
        falsePos = 0;
        int n = t.length(), m = p.length();
        if (m == 0 || m > n) return res;

        long h = 1;                          // BASE^(m-1) % mod
        for (int i = 0; i < m - 1; i++) h = h * BASE % mod;

        long hp = 0, ht = 0;
        for (int i = 0; i < m; i++) {
            hp = (hp * BASE + p.charAt(i)) % mod;
            ht = (ht * BASE + t.charAt(i)) % mod;
        }

        for (int i = 0; i <= n - m; i++) {
            if (hp == ht) {
                int k = 0;
                while (k < m) {
                    comps++;
                    if (t.charAt(i + k) != p.charAt(k)) break;
                    k++;
                }
                if (k == m) res.add(i);
                else falsePos++;
            }
            if (i < n - m) {
                updates++;
                ht = ((ht - t.charAt(i) * h % mod + mod) % mod * BASE + t.charAt(i + m)) % mod;
            }
        }
        return res;
    }
}
