package Assignment6;

import java.util.*;
import java.util.concurrent.*;

public class AverageTest {

    public static void testAverage() {
        List<Long> list = new ArrayList<>();
        ForkJoinPool pool = new ForkJoinPool();
        Random rand = new Random();

        for (int i = 0; i < 10_000_000; i++) {
            list.add((long) rand.nextInt(500));
        }

        // 🔹 ForkJoin
        long start = System.nanoTime();

        SumTask task = new SumTask(list);
        Long sum = pool.invoke(task);

        double avg = (double) sum / list.size();
        long end = System.nanoTime();

        System.out.println("Average (ForkJoin): " + avg);
        System.out.println("Time: " + (end - start) + " ns");

        // 🔹 Sequencial
        start = System.nanoTime();

        sum = 0L;
        for (long l : list) sum += l;

        avg = (double) sum / list.size();
        end = System.nanoTime();

        System.out.println("Average (Sequential): " + avg);
        System.out.println("Time: " + (end - start) + " ns");
    }

    public static void main(String[] args) {
        testAverage();
    }
}