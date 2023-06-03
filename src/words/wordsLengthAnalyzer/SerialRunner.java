package words.wordsLengthAnalyzer;

import words.Folder;

import java.util.HashMap;
import java.util.Map;

public class SerialRunner implements Runner {
    @Override
    public Map<Integer, Long> run(Folder folder) {
        var len2count = new HashMap<Integer, Long>();

        folder.getSubFolders().forEach(subfolder ->
                WordLenUtils.mergeMaps(len2count, run(subfolder)));

        folder.getDocuments().forEach(document ->
                WordLenUtils.mergeMaps(len2count, WordLenUtils.getWordLens(document.getLines())));

        return len2count;
    }
}
