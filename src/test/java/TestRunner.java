import dp.EditDistance;
import dp.LCS;
import matching.KMP;
import matching.Naive;
import matching.RabinKarp;
import suffix.Kasai;
import suffix.SimpleSA;

import java.util.*;

// no junit here (no internet for maven), so a tiny runner with main()
public class TestRunner {
    static int pass = 0, fail = 0;

    static void check(String name, Object exp, Object got) {
        if (Objects.equals(exp, got)) {
            pass++;
            System.out.println("PASS  " + name);
        } else {
            fail++;
            System.out.println("FAIL  " + name + "  expected=" + exp + " got=" + got);
        }
    }

    static boolean isSubseq(String s, String t) {
        int i = 0;
        for (int j = 0; j < t.length() && i < s.length(); j++) {
            if (s.charAt(i) == t.charAt(j)) i++;
        }
        return i == s.length();
    }

    // runs all 3 matchers + rabin karp with a tiny mod, all must give the same list
    static void match(String name, String t, String p, List<Integer> exp) {
        List<Integer> a = Naive.search(t, p);
        List<Integer> b = KMP.search(t, p);
        List<Integer> c = RabinKarp.search(t, p);
        List<Integer> d = RabinKarp.search(t, p, 7);
        check("match " + name + " (naive)", exp, a);
        check("match " + name + " (kmp)", exp, b);
        check("match " + name + " (rabin-karp)", exp, c);
        check("match " + name + " (rabin-karp mod 7)", exp, d);
        check("match " + name + " (all same)", true, a.equals(b) && b.equals(c) && c.equals(d));
    }

    static List<Integer> L(Integer... x) {
        return new ArrayList<>(Arrays.asList(x));
    }

    static void testLCS() {
        check("lcs identical", 6, LCS.length("abcdef", "abcdef"));
        check("lcs different", 0, LCS.length("abc", "xyz"));
        check("lcs empty first", 0, LCS.length("", "abc"));
        check("lcs empty second", 0, LCS.length("abc", ""));
        check("lcs both empty", 0, LCS.length("", ""));
        check("lcs ABCBDAB/BDCABA", 4, LCS.length("ABCBDAB", "BDCABA"));
        check("lcs AGGTAB/GXTXAYB", 4, LCS.length("AGGTAB", "GXTXAYB"));
        String s = LCS.build("ABCBDAB", "BDCABA");
        check("lcs build length", 4, s.length());
        check("lcs build is subseq of both", true, isSubseq(s, "ABCBDAB") && isSubseq(s, "BDCABA"));
    }

    static void testED() {
        check("ed identical", 0, EditDistance.dist("hello", "hello"));
        check("ed empty first", 3, EditDistance.dist("", "abc"));
        check("ed empty second", 3, EditDistance.dist("abc", ""));
        check("ed both empty", 0, EditDistance.dist("", ""));
        check("ed substitution", 1, EditDistance.dist("cat", "cut"));
        check("ed insertion", 1, EditDistance.dist("cat", "cart"));
        check("ed deletion", 1, EditDistance.dist("cart", "cat"));
        check("ed kitten/sitting", 3, EditDistance.dist("kitten", "sitting"));
        check("ed sunday/saturday", 3, EditDistance.dist("sunday", "saturday"));
        check("ed intention/execution", 5, EditDistance.dist("intention", "execution"));
        check("ed symmetric", EditDistance.dist("flaw", "lawn"), EditDistance.dist("lawn", "flaw"));
    }

    static void testMatching() {
        match("beginning", "abcdefgh", "abc", L(0));
        match("middle", "xxabcxx", "abc", L(2));
        match("end", "xxxxabc", "abc", L(4));
        match("multiple", "abcxabcyabc", "abc", L(0, 4, 8));
        match("overlapping", "abababab", "abab", L(0, 2, 4));
        match("all a", "aaaaa", "aa", L(0, 1, 2, 3));
        match("absent", "abcdef", "xyz", L());
        match("absent but close", "aaaaaaaa", "aaab", L());
        match("pattern longer than text", "abc", "abcdef", L());
        match("empty pattern", "abc", "", L());
        match("empty text", "", "a", L());
        match("both empty", "", "", L());
        match("pattern equals text", "abcabc", "abcabc", L(0));
        match("single char", "banana", "a", L(1, 3, 5));
        match("kmp classic", "ABABDABACDABABCABAB", "ABABCABAB", L(10));

        check("lps aabaaab", Arrays.toString(new int[]{0, 1, 0, 1, 2, 2, 3}), Arrays.toString(KMP.lps("aabaaab")));
        check("lps abcabc", Arrays.toString(new int[]{0, 0, 0, 1, 2, 3}), Arrays.toString(KMP.lps("abcabc")));

        // a tiny mod must give false positives but still the right answer
        RabinKarp.search("abcdefghijklmnopqrstuvwxyz", "xyz", 7);
        check("rk tiny mod has false positives", true, RabinKarp.falsePos > 0);
    }

    static void testKasai() {
        String s = "banana";
        int[] sa = {5, 3, 1, 0, 4, 2};      // a, ana, anana, banana, na, nana
        int[] lcp = Kasai.build(s, sa);
        check("kasai banana", "[0, 1, 3, 0, 0, 2]", Arrays.toString(lcp));
        check("simple sa banana", Arrays.toString(sa), Arrays.toString(SimpleSA.build(s)));

        check("kasai abab", "[0, 2, 0, 1]", Arrays.toString(Kasai.build("abab", new int[]{2, 0, 3, 1})));
        check("kasai aaaa", "[0, 1, 2, 3]", Arrays.toString(Kasai.build("aaaa", new int[]{3, 2, 1, 0})));
        check("kasai single char", "[0]", Arrays.toString(Kasai.build("a", new int[]{0})));
    }

    // random stuff compared against slow/obvious versions
    static void testRandom() {
        Random r = new Random(7);
        boolean okMatch = true, okKasai = true, okDp = true;
        for (int it = 0; it < 500; it++) {
            String t = rnd(r, 1 + r.nextInt(60), 2);
            String p = rnd(r, 1 + r.nextInt(5), 2);
            List<Integer> exp = new ArrayList<>();
            for (int i = 0; i + p.length() <= t.length(); i++) {
                if (t.startsWith(p, i)) exp.add(i);
            }
            if (!exp.equals(Naive.search(t, p)) || !exp.equals(KMP.search(t, p))
                    || !exp.equals(RabinKarp.search(t, p)) || !exp.equals(RabinKarp.search(t, p, 11))) okMatch = false;

            String s = rnd(r, 1 + r.nextInt(40), 3);
            int[] sa = SimpleSA.build(s);
            int[] lcp = Kasai.build(s, sa);
            for (int i = 1; i < s.length(); i++) {
                int h = 0;
                String x = s.substring(sa[i - 1]), y = s.substring(sa[i]);
                while (h < x.length() && h < y.length() && x.charAt(h) == y.charAt(h)) h++;
                if (h != lcp[i]) okKasai = false;
            }

            String a = rnd(r, r.nextInt(12), 3), b = rnd(r, r.nextInt(12), 3);
            int l = LCS.length(a, b), e = EditDistance.dist(a, b);
            // edit distance is at most the indel only distance, and at least the length difference
            if (e > a.length() + b.length() - 2 * l || e < Math.abs(a.length() - b.length())) okDp = false;
        }
        check("random: naive/kmp/rk vs brute force (500 cases)", true, okMatch);
        check("random: kasai vs brute force lcp (500 cases)", true, okKasai);
        check("random: lcs/edit distance sanity bounds (500 cases)", true, okDp);
    }

    static String rnd(Random r, int n, int k) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append((char) ('a' + r.nextInt(k)));
        return sb.toString();
    }

    public static void main(String[] args) {
        testLCS();
        testED();
        testMatching();
        testKasai();
        testRandom();
        System.out.println();
        System.out.println(pass + " passed, " + fail + " failed");
        if (fail > 0) System.exit(1);
    }
}
