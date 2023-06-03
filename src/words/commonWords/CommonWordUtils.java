package words.commonWords;

import java.util.*;

public class CommonWordUtils {
    private static String[] wordsIn(String line) {
        return line.trim().split("(\\s|\\p{Punct})+");
    }

    public static Set<String> getCommonWords(List<String> lines) {
        var commonWords = new HashSet<String>();
        for (String line : lines) {
            for (String word : wordsIn(line)) {
                if (word.length() == 0) {
                    continue;
                }
                commonWords.add(word.toLowerCase());
            }
        }
        return commonWords;
    }

    public static void mergeSets(Set<String> first, Set<String> second) {
        if (first.isEmpty()) {
            first.addAll(second);
        } else {
            first.retainAll(second);
        }
    }
}
