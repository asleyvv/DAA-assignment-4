package suffix;

import java.util.Arrays;

// naive suffix array, only for small/test strings (not part of the timed stuff)
public class SimpleSA {
    public static int[] build(String s) {
        int n = s.length();
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        Arrays.sort(idx, (x, y) -> cmp(s, x, y));
        int[] sa = new int[n];
        for (int i = 0; i < n; i++) sa[i] = idx[i];
        return sa;
    }

    static int cmp(String s, int a, int b) {
        int n = s.length();
        while (a < n && b < n) {
            char c = s.charAt(a), d = s.charAt(b);
            if (c != d) return c - d;
            a++;
            b++;
        }
        return (n - a) - (n - b);   // shorter suffix first
    }
}
