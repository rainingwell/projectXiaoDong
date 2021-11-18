package com.speechAndroid;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 本文件用来演示如何通过文本相似度进行纠错
 * <p>
 * <pre>
 * String[] list = new String[]{"张三", "张衫", "张丹", "张成", "李四", "李奎"};
 * Search d = new Search(list);
 * System.out.println(d.search("张三", 10));
 * System.out.println(d.search("李四", 10));
 *
 * 输出：
 * [{word=张三, score=0}, {word=张衫, score=1}, {word=张丹, score=1}, {word=张成, score=5}, {word=李四, score=9}, {word=李奎, score=10}]
 * [{word=李四, score=0}, {word=李奎, score=3}, {word=张三, score=9}, {word=张衫, score=10}, {word=张丹, score=10}, {word=张成, score=12}]
 * </pre>
 */
public class Search {
    final List<Word> targets = new ArrayList<Word>();

    public Search(String[] list) throws PinyinException {
        for (String s : list) {
            Word w = new Word(s);
            targets.add(w);
        }
    }

   public static void main(String[] args) throws PinyinException {
        String[] list = new String[]{"张三", "张散", "张丹", "张成", "李四", "李奎"};
        Search d = new Search(list);
        System.out.println(d.search("张山", 10));
        System.out.println(d.search("李四", 10));
    }


    public List<Score> search(String input, int limit) throws PinyinException {
        Word w = new Word(input);
        return targets.stream().map(x -> {
            Score s = new Score();
            s.word = x;
            s.score = x.compareTo(w);
            return s;
        }).sorted().limit(limit).collect(Collectors.toList());
    }


    public static int getEditDistance(String s, String t) {
        int d[][]; // matrix
        int n; // length of s
        int m; // length of t
        int i; // iterates through s
        int j; // iterates through t
        char s_i; // ith character of s
        char t_j; // jth character of t
        int cost; // cost
        // Step 1
        n = s.length();
        m = t.length();
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        d = new int[n + 1][m + 1];

        // Step 2
        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }
        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }

        // Step 3
        for (i = 1; i <= n; i++) {
            s_i = s.charAt(i - 1);
            // Step 4
            for (j = 1; j <= m; j++) {
                t_j = t.charAt(j - 1);
                // Step 5
                cost = (s_i == t_j) ? 0 : 1;
                // Step 6
                d[i][j] = Minimum(d[i - 1][j] + 1, d[i][j - 1] + 1,
                        d[i - 1][j - 1] + cost);
            }
        }
        // Step 7
        return d[n][m];
    }

    private static int Minimum(int a, int b, int c) {
        int im = a < b ? a : b;
        return im < c ? im : c;
    }

    class Word implements Comparable {
        final String word;
        final String pinyin1;
        final String pinyin2;

        Word(String word) throws PinyinException {
            this.word = word;
            this.pinyin1 = PinyinHelper.convertToPinyinString(word, ",", PinyinFormat.WITH_TONE_NUMBER);
            this.pinyin2 = PinyinHelper.convertToPinyinString(word, ",", PinyinFormat.WITHOUT_TONE);
        }

        @Override
        public String toString() {
            return word;
        }

        @Override
        public int compareTo(Object o) {
            if (o instanceof Word) {
                Word o1 = (Word) o;
                int score1 = getEditDistance(this.pinyin1, o1.pinyin1);
                int score2 = getEditDistance(this.pinyin2, o1.pinyin2);
                return score1 + score2;
            }
            return 0;
        }
    }

    class Score implements Comparable {
        Word word;
        int score;

        @Override
        public int compareTo(Object o) {
            if (o instanceof Score) {
                return score - ((Score) o).score;
            }
            return 0;
        }

        @Override
        public String toString() {
            return "{" +
                    "word=" + word +
                    ", score=" + score +
                    '}';
        }
    }
}
