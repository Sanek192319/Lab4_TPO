package words.commonWords;

import words.Folder;

import java.util.HashSet;
import java.util.Set;

public class SerialRunner implements Runner {
    @Override
    public Set<String> run(Folder folder) {
        var commonWords = new HashSet<String>();

        folder.getSubFolders().forEach(subfolder ->
                CommonWordUtils.mergeSets(commonWords, run(subfolder)));

        folder.getDocuments().forEach(document ->
                CommonWordUtils.mergeSets(commonWords, CommonWordUtils.getCommonWords(document.getLines())));

        return commonWords;
    }
}
