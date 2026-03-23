package Assignment4V2;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ParallelV2_ {
    private static final int N_PARTITIONS = 10;
    private static final int N_THREADS = 5;
    private static final int VEC_SIZE = 2000;

    public static void main(String[] args) {

        List<Future<Integer>> futures = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(N_THREADS);
        int[] data = new int[VEC_SIZE];
        for (int i = 0; i < VEC_SIZE; i++) {
            data[i] = i + 1;
        }

        int sizePart = VEC_SIZE / N_PARTITIONS;

        for (int i = 0; i < N_PARTITIONS; i++) {
            int from = i * sizePart;
            int to = (i + 1) * sizePart;
            Callable<Integer> task = new PartialSumV2_(from, to, data); // Create a Callable task for the partial sum
            System.out.println("Creating task to sum from " + from + " to " + to);
            Future<Integer> future = executor.submit(task); // Submit the task and get a Future object
            futures.add(future); // Add the Future object to the list for later retrieval of results
        }

        executor.shutdown();


        System.out.println("All threads have finished.");

        int totalSum = 0; // Aggregate the results from all partial sums
        for (Future<Integer> future : futures) { // Loop through each Future object to get the computed partial sum
            try {
                totalSum += future.get(); // Add the partial sum to the total sum
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();

            }
        }
        System.out.println("Total sum: " + totalSum);
    }
}