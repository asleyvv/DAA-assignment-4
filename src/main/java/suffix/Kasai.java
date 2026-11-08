package suffix;

public class Kasai {
    public static long comps = 0;

    // lcp[i] = longest common prefix of suffixes sa[i-1] and sa[i], lcp[0] = 0
    public static int[] build(String s, int[] sa) {
        int n = s.length();
        int[] rank = new int[n];
        int[] lcp = new int[n];
        comps = 0;
        for (int i = 0; i < n; i++) rank[sa[i]] = i;

        int h = 0;
        for (int i = 0; i < n; i++) {
            if (rank[i] > 0) {
                int j = sa[rank[i] - 1];
                while (i + h < n && j + h < n) {
                    comps++;
                    if (s.charAt(i + h) != s.charAt(j + h)) break;
                    h++;
                }
                lcp[rank[i]] = h;
                if (h > 0) h--;     // next suffix has lcp at least h-1
            } else {
                h = 0;
            }
        }
        return lcp;
    }
}
