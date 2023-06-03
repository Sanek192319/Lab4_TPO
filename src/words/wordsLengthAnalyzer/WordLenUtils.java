package words.wordsLengthAnalyzer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordLenUtils {
    private static String[] wordsIn(String line) {
        return line.trim().split("(\\s|\\p{Punct})+");
    }

    public static Map<Integer, Long> getWordLens(List<String> lines) {
        Map<Integer, Long> lens = new HashMap<>();
        for (String line : lines) {
            for (String word : wordsIn(line)) {
                var len = word.length();
                if (len == 0) {
                    continue;
                }
                lens.put(len, lens.getOrDefault(len, 0L) + 1);
            }
        }
        return lens;
    }

    public static void mergeMaps(Map<Integer, Long> dest, Map<Integer, Long> source) {
        source.forEach((key, value) ->
                dest.merge(key, value, (oldValue, newValue) -> oldValue + newValue));
    }
}
