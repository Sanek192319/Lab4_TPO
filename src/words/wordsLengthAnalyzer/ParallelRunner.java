package words.wordsLengthAnalyzer;

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

//    class DocumentTask extends RecursiveTask<Map<Integer, Long>> {
//        private final Document document;
//        private static final int MAX_LEN = 100;
//
//
//        public DocumentTask(Document document) {
//            super();
//            this.document = document;
//        }
//
//        @Override
//        protected Map<Integer, Long> compute() {
//            return WordLenUtils.getWordLens(document.getLines());
//        }
//    }

    class DocumentTask extends RecursiveTask<Map<Integer, Long>> {
        private static final int MAX_LINES = 1000;
        private final List<String> lines;

        public DocumentTask(List<String> lines) {
            super();
            this.lines = lines;
        }

        private Map<Integer, Long> computeDirectly() {
            return WordLenUtils.getWordLens(lines);
        }

        private LinkedList<DocumentTask> generateTasks() {
            int numLines = lines.size();
            int midIdx = lines.size() / 2;

            var first = new DocumentTask(lines.subList(0, midIdx));
            var second = new DocumentTask(lines.subList(midIdx, numLines));

            return new LinkedList<>(List.of(first, second));
        }

        @Override
        protected Map<Integer, Long> compute() {
            if (lines.size() <= MAX_LINES) {
                return computeDirectly();
            }
            var len2count = new HashMap<Integer, Long>();
            var tasks = ForkJoinTask.invokeAll(generateTasks());

            tasks.forEach(task -> WordLenUtils.mergeMaps(len2count, task.join()));
            return len2count;
        }
    }

    class FolderTask extends RecursiveTask<Map<Integer, Long>> {
        private final Folder folder;

        FolderTask(Folder folder) {
            super();
            this.folder = folder;
        }

        @Override
        protected Map<Integer, Long> compute() {
            var len2count = new HashMap<Integer, Long>();

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

            tasks.forEach(task -> WordLenUtils.mergeMaps(len2count, task.join()));
            return len2count;
        }
    }

    @Override
    public Map<Integer, Long> run(Folder folder) {
        return forkJoinPool.invoke(new FolderTask(folder));
    }
}
