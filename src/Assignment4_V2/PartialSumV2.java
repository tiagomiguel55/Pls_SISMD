package Assignment4_V2;

import java.util.concurrent.Callable;

public class PartialSumV2 implements Callable {
    private final int from;
    private final int to;
    private int[] vec;

    PartialSumV2(int _from, int _to, int[] vec) {
        this.from = _from;
        this.to = _to;
        this.vec = vec;
    }


    @Override
    public Object call() throws Exception {
        int partialSum = 0;
        System.out.println(Thread.currentThread().getName() + " starting to sum from " + from + " to " + to);
        for (int i = from; i < to; i++)
            partialSum += vec[i];
        System.out.println(Thread.currentThread().getName() + " adding " + partialSum + " to total");
        return partialSum;
    }
}