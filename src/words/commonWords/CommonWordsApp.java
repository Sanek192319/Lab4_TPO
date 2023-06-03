package words.commonWords;

import words.Folder;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class CommonWordsApp {
    public static void main(String[] args) throws IOException {
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
            var commonWords = runner.run(folder);
            long stop = System.currentTimeMillis();

            time[idx] = stop - start;
            System.out.println("Time: " + time[idx] + "ms");
            printCommonWords(commonWords);
        }
        return time;
    }

    private static void printCommonWords(Set<String> commonWords) {
        System.out.println("Common words (" + commonWords.size() + "):");
        System.out.println("{" + String.join(", ", commonWords) + "}");
    }
}
