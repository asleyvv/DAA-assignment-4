package dp;

public class LCS {
    // counts how many table cells got filled (for the experiments)
    public static long cells = 0;

    public static int[][] table(String a, String b) {
        int n = a.length(), m = b.length();
        int[][] t = new int[n + 1][m + 1];
        cells = 0;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                cells++;
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    t[i][j] = t[i - 1][j - 1] + 1;
                } else {
                    t[i][j] = Math.max(t[i - 1][j], t[i][j - 1]);
                }
            }
        }
        return t;
    }

    public static int length(String a, String b) {
        int[][] t = table(a, b);
        return t[a.length()][b.length()];
    }

    // walk back through the table to get one actual LCS
    public static String build(String a, String b) {
        int[][] t = table(a, b);
        StringBuilder sb = new StringBuilder();
        int i = a.length(), j = b.length();
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                sb.append(a.charAt(i - 1));
                i--;
                j--;
            } else if (t[i - 1][j] >= t[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }
        return sb.reverse().toString();
    }
}
