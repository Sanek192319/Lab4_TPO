package words.searchByKeyWords;

import java.util.*;

public class KeywordUtils {
    private static String[] wordsIn(String line) {
        return line.trim().split("(\\s|\\p{Punct})+");
    }

    public static Set<String> getFoundKeywords(List<String> lines, Set<String> keywords) {
        var foundKeywords = new HashSet<String>();
        for (String line : lines) {
            for (String word : wordsIn(line)) {
                var processed = word.toLowerCase();

                if (keywords.contains(processed)) {
                    foundKeywords.add(processed);
                }
                if (foundKeywords.size() == keywords.size()) {
                    return foundKeywords;
                }
            }
        }
        return foundKeywords;
    }

    public static void mergeMaps(Map<String, Double> dest, Map<String, Double> source) {
        source.forEach((key, value) ->
                dest.merge(key, value, (oldValue, newValue) -> oldValue + newValue));
    }
}
