package Assignment4_V2;

import Assignment4.PartialSum;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParallelV2 {
    private static final int N_PARTITIONS = 10;
    private static final int N_THREADS = 5;
    private static final int VEC_SIZE = 2000;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(N_THREADS);
        int[] data = new int[VEC_SIZE];
        for(int i=0;i<VEC_SIZE;i++){
            data[i] = i+1;
        }

        int sizePart = VEC_SIZE / N_PARTITIONS;

        for(int i=0;i<N_PARTITIONS;i++){
            int from = i*sizePart;
            int to = (i+1)*sizePart;
            Runnable task = new PartialSum(from,to,data);
            System.out.println("Creating task to sum from " + from + " to " + to);
            executor.submit(task);
        }

        executor.shutdown();

        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            executor.shutdownNow();
        }

        System.out.println("All threads have finished.");

    }

}