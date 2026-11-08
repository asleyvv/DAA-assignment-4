package matching;

import java.util.ArrayList;
import java.util.List;

public class Naive {
    public static long comps = 0;

    // empty pattern -> empty list (see README)
    public static List<Integer> search(String t, String p) {
        List<Integer> res = new ArrayList<>();
        comps = 0;
        int n = t.length(), m = p.length();
        if (m == 0 || m > n) return res;
        for (int i = 0; i <= n - m; i++) {
            int j = 0;
            while (j < m) {
                comps++;
                if (t.charAt(i + j) != p.charAt(j)) break;
                j++;
            }
            if (j == m) res.add(i);
        }
        return res;
    }
}
