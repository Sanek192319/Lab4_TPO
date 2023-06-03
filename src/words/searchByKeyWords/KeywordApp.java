package words.searchByKeyWords;

import words.Folder;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KeywordApp {
    public static void main(String[] args) throws IOException {
        Folder folder = Folder.fromDirectory(new File("./data"));
        int repeatCount = 30;
        int numThreads = 2;

        var keywords = new HashSet<>(Set.of("network", "graph", "matrix", "thread", "benchmark", "vector", "cluster", "deployment"));
//        var keywords = new HashSet<>(Set.of("matrix", "vector"));
        printKeywords(keywords);

        var parallelRunner = new ParallelRunner(numThreads);
        var serialRunner = new SerialRunner();

        System.out.println("ForkJoinFramework:");
        long[] parallelTime = runExperiment(parallelRunner, folder, keywords, repeatCount);

        System.out.println("Single thread:");
        long[] serialTime = runExperiment(serialRunner, folder, keywords, repeatCount);

        System.out.println("Comparison (Single Thread vs. ForkJoinFramework):");
        for (int idx = 0; idx < repeatCount; ++idx) {
            System.out.println(serialTime[idx] + " " + parallelTime[idx]);
        }
    }

    private static long[] runExperiment(Runner runner, Folder folder, Set<String> keywords, int repeatCount) {
        var time = new long[repeatCount];

        for (int idx = 0; idx < repeatCount; ++idx) {
            long start = System.currentTimeMillis();
            var document2coverage = runner.run(folder, keywords);
            long stop = System.currentTimeMillis();

            time[idx] = stop - start;
            System.out.println("Time: " + time[idx] + "ms");
            printCoverage(document2coverage);
        }
        return time;
    }

    public static void printKeywords(Set<String> keywords) {
        System.out.println("Keywords:");
        System.out.println("{" + String.join(", ", keywords) + "}");
    }

    public static void printCoverage(Map<String, Double> coverage) {
        System.out.println("Keyword coverage (by document):");
        coverage.forEach((key, value) -> System.out.println(key + ": " + value));
    }
}
