package words.commonWords;

import words.Folder;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Stream;

public class ParallelRunner implements Runner {
    private final ForkJoinPool forkJoinPool;

    ParallelRunner(int numThreads) {
        forkJoinPool = new ForkJoinPool(numThreads);
    }

    class DocumentTask extends RecursiveTask<Set<String>> {
        private static final int MAX_LINES = 1000;
        private final List<String> lines;

        public DocumentTask(List<String> lines) {
            super();
            this.lines = lines;
        }

        private Set<String> computeDirectly() {
            return CommonWordUtils.getCommonWords(lines);
        }

        private LinkedList<DocumentTask> generateTasks() {
            int numLines = lines.size();
            int midIdx = lines.size() / 2;

            var first = new DocumentTask(lines.subList(0, midIdx));
            var second = new DocumentTask(lines.subList(midIdx, numLines));

            return new LinkedList<>(List.of(first, second));
        }

        @Override
        protected Set<String> compute() {
            if (lines.size() <= MAX_LINES) {
                return computeDirectly();
            }
            var uniqueWords = new HashSet<String>();
            var tasks = ForkJoinTask.invokeAll(generateTasks());

            tasks.forEach(task -> uniqueWords.addAll(task.join()));
            return uniqueWords;
        }
    }

    class FolderTask extends RecursiveTask<Set<String>> {
        private final Folder folder;

        FolderTask(Folder folder) {
            super();
            this.folder = folder;
        }

        @Override
        protected Set<String> compute() {
            var commonWords = new HashSet<String>();

            var tasks = Stream.concat(
                    folder.getSubFolders().stream().map(subfolder -> {
                        var task = new ParallelRunner.FolderTask(subfolder);
                        task.fork();
                        return task;
                    }),
                    folder.getDocuments().stream().map(document -> {
                        var task = new ParallelRunner.DocumentTask(document.getLines());
                        task.fork();
                        return task;
                    })
            ).toList();

            tasks.forEach(task -> CommonWordUtils.mergeSets(commonWords, task.join()));
            return commonWords;
        }
    }

    @Override
    public Set<String> run(Folder folder) {
        return forkJoinPool.invoke(new FolderTask(folder));
    }
}
