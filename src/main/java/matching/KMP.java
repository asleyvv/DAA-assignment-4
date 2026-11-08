package matching;

import java.util.ArrayList;
import java.util.List;

public class KMP {
    public static long prepComps = 0;
    public static long comps = 0;

    // lps[i] = length of longest proper prefix of p[0..i] that is also a suffix
    public static int[] lps(String p) {
        int m = p.length();
        int[] l = new int[m];
        int len = 0, i = 1;
        while (i < m) {
            prepComps++;
            if (p.charAt(i) == p.charAt(len)) {
                len++;
                l[i] = len;
                i++;
            } else if (len != 0) {
                len = l[len - 1];
            } else {
                l[i] = 0;
                i++;
            }
        }
        return l;
    }

    public static List<Integer> search(String t, String p) {
        List<Integer> res = new ArrayList<>();
        comps = 0;
        prepComps = 0;
        int n = t.length(), m = p.length();
        if (m == 0 || m > n) return res;
        int[] l = lps(p);
        int i = 0, j = 0;
        while (i < n) {
            comps++;
            if (t.charAt(i) == p.charAt(j)) {
                i++;
                j++;
                if (j == m) {
                    res.add(i - j);
                    j = l[j - 1];   // keep going, overlapping matches allowed
                }
            } else if (j != 0) {
                j = l[j - 1];       // i does not move back
            } else {
                i++;
            }
        }
        return res;
    }
}
