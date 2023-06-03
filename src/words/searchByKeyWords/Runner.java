package words.searchByKeyWords;

import words.Folder;

import java.util.Map;
import java.util.Set;

public interface Runner {
    Map<String, Double> run(Folder folder, Set<String> keywords);
}
