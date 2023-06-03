package words.searchByKeyWords;

import words.Folder;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class ParallelRunner implements Runner {
    private final ForkJoinPool forkJoinPool;

    ParallelRunner(int numThreads) {
        forkJoinPool = new ForkJoinPool(numThreads);
    }

    class DocumentTask extends RecursiveTask<Set<String>> {
        private static final int MAX_LINES = 1000;
        private final String path;
        private final List<String> lines;
        private final Set<String> keywords;

        public DocumentTask(String path, List<String> lines, Set<String> keywords) {
            super();
            this.path = path;
            this.lines = lines;
            this.keywords = keywords;
        }

        public String getPath() {
            return path;
        }

        private Set<String> computeDirectly() {
            return KeywordUtils.getFoundKeywords(lines, keywords);
        }

        private LinkedList<DocumentTask> generateTasks() {
            int numLines = lines.size();
            int midIdx = lines.size() / 2;

            var first = new DocumentTask(path, lines.subList(0, midIdx), keywords);
            var second = new DocumentTask(path, lines.subList(midIdx, numLines), keywords);

            return new LinkedList<>(List.of(first, second));
        }

        @Override
        protected Set<String> compute() {
            if (lines.size() <= MAX_LINES) {
                return computeDirectly();
            }
            var foundKeywords = new HashSet<String>();
            var tasks = ForkJoinTask.invokeAll(generateTasks());

            tasks.forEach(task -> foundKeywords.addAll(task.join()));
            return foundKeywords;
        }
    }

    class FolderTask extends RecursiveTask<Map<String, Double>> {
        private final Folder folder;
        private final Set<String> keywords;

        FolderTask(Folder folder, Set<String> keywords) {
            super();
            this.folder = folder;
            this.keywords = keywords;
        }

        @Override
        protected Map<String, Double> compute() {
            var document2coverage = new HashMap<String, Double>();

            var folderTasks = folder.getSubFolders()
                .stream()
                .map(subfolder -> {
                    var task = new ParallelRunner.FolderTask(subfolder, keywords);
                    task.fork();
                    return task;
                }).toList();

            var documentTasks = folder.getDocuments()
                .stream()
                .map(document -> {
                    var task = new ParallelRunner.DocumentTask(document.getPath(), document.getLines(), keywords);
                    task.fork();
                    return task;
                }).toList();

            documentTasks.forEach(task -> {
                var foundKeywords = task.join();
                var coverage = foundKeywords.size() / (double) keywords.size();
                var path = task.getPath();
                document2coverage.put(path, coverage);
            });
            folderTasks.forEach(task -> KeywordUtils.mergeMaps(document2coverage, task.join()));
            return document2coverage;
        }
    }

    @Override
    public Map<String, Double> run(Folder folder, Set<String> keywords) {
        return forkJoinPool.invoke(new FolderTask(folder, keywords));
    }
}
