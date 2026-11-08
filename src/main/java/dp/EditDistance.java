package dp;

public class EditDistance {
    public static long cells = 0;

    public static int dist(String a, String b) {
        int n = a.length(), m = b.length();
        int[][] d = new int[n + 1][m + 1];
        cells = 0;
        for (int i = 0; i <= n; i++) d[i][0] = i;   // delete everything
        for (int j = 0; j <= m; j++) d[0][j] = j;   // insert everything
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                cells++;
                int sub = d[i - 1][j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1);
                int del = d[i - 1][j] + 1;
                int ins = d[i][j - 1] + 1;
                d[i][j] = Math.min(sub, Math.min(del, ins));
            }
        }
        return d[n][m];
    }
}
