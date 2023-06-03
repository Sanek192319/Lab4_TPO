package words.searchByKeyWords;

import words.Folder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SerialRunner implements Runner {
    @Override
    public Map<String, Double> run(Folder folder, Set<String> keywords) {
        var document2coverage = new HashMap<String, Double>();

        folder.getSubFolders().forEach(subfolder ->
                KeywordUtils.mergeMaps(document2coverage, run(subfolder, keywords)));

        folder.getDocuments().forEach(document -> {
            var foundKeywords = KeywordUtils.getFoundKeywords(document.getLines(), keywords);
            var coverage = foundKeywords.size() / (double) keywords.size();
            var path = document.getPath();
            document2coverage.put(path, coverage);
        });
        return document2coverage;
    }
}
