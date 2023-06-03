package words.wordsLengthAnalyzer;

import words.Folder;

import java.util.Map;

public interface Runner {
    Map<Integer, Long> run(Folder folder);
}
