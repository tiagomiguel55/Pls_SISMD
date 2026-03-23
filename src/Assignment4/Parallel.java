package Assignment4;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Parallel {
    private static final int N_PARTITIONS = 10;
    private static final int N_THREADS = 5;
    private static final int VEC_SIZE = 2000;

    static PartialSum[] partialSums = new PartialSum[N_PARTITIONS]; // Array to hold the PartialSum instances for each partition

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(N_THREADS);
        int[] data = new int[VEC_SIZE];
        for (int i = 0; i < VEC_SIZE; i++) {
            data[i] = i + 1;
        }
        int sizePart = VEC_SIZE / N_PARTITIONS;

        for (int i = 0; i < N_PARTITIONS; i++) {
            int from = i * sizePart;
            int to = (i + 1) * sizePart;
            partialSums[i] = new PartialSum(from, to, data);
            System.out.println("Creating task to sum from " + from + " to " + to);
            executor.submit(partialSums[i]);
        }


        executor.shutdown();

        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            executor.shutdownNow();
        }

        System.out.println("All threads have finished.");

        int totalSum = 0; // Aggregate the results from all partial sums
        for (PartialSum ps : partialSums) { // Loop through each PartialSum instance to get the computed sum
            totalSum += ps.getSum(); // Add the partial sum to the total sum
        }
        System.out.println("Total sum: " + totalSum);

    }



}