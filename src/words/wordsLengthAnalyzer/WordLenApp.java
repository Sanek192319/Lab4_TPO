package words.wordsLengthAnalyzer;

import words.Folder;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class WordLenApp {
    public static void main(String[] args) throws IOException {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        Folder folder = Folder.fromDirectory(new File("./data"));
        int repeatCount = 30;
        int numThreads = 2;

        var parallelRunner = new ParallelRunner(numThreads);
        var serialRunner = new SerialRunner();

        System.out.println("ForkJoinFramework:");
        long[] parallelTime = runExperiment(parallelRunner, folder, repeatCount);

        System.out.println("Single thread:");
        long[] serialTime = runExperiment(serialRunner, folder, repeatCount);

        System.out.println("Comparison (Single Thread vs. ForkJoinFramework):");
        for (int idx = 0; idx < repeatCount; ++idx) {
            System.out.println(serialTime[idx] + " " + parallelTime[idx]);
        }
    }

    private static long[] runExperiment(Runner runner, Folder folder, int repeatCount) {
        var time = new long[repeatCount];

        for (int idx = 0; idx < repeatCount; ++idx) {
            long start = System.currentTimeMillis();
            var len2count = runner.run(folder);
            long stop = System.currentTimeMillis();

            time[idx] = stop - start;
            System.out.println("Time: " + time[idx] + "ms");
            computeStats(len2count);
        }
        return time;
    }

    private static void computeStats(Map<Integer, Long> len2count) {
        var len2prob = normalize(len2count);
        var count = computeCount(len2count);
        var std = computeStd(len2prob);
        var mean = computeMean(len2prob);
        var histogram = computeHistogram(len2count);

        System.out.println("Num words: " + count);
        System.out.println("Average word length: " + mean + " +- " + std);
        printHistogram(histogram);
    }

    private static Long computeCount(Map<Integer, Long> len2count) {
        return len2count.values().stream().reduce(0L, Long::sum);
    }

    private static Map<Integer, Double> normalize(Map<Integer, Long> len2count) {
        double count = computeCount(len2count);
        return len2count.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() / count));
    }

    private static double computeMean(Map<Integer, Double> len2prob) {
        return len2prob.entrySet()
                .stream()
                .reduce(0.0,
                        (mean, entry) -> {
                            double prob = entry.getValue();
                            int value = entry.getKey();
                            return mean + value * prob;
                        },
                        (first, second) -> first + second);
    }

    private static double computeStd(Map<Integer, Double> len2prob) {
        double mean = computeMean(len2prob);
        return Math.sqrt(
                len2prob.entrySet()
                        .stream()
                        .reduce(0.0,
                                (variance, entry) -> {
                                    double prob = entry.getValue();
                                    int value = entry.getKey();
                                    return variance + Math.pow(value - mean, 2) * prob;
                                },
                                (first, second) -> first + second)
        );
    }

    private static LinkedHashMap<Integer, Long> computeHistogram(Map<Integer, Long> len2count) {
        return len2count.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public static void printHistogram(LinkedHashMap<Integer, Long> histogram) {
        histogram.forEach((key, value) -> System.out.println(key + ": " + value));
    }
}
