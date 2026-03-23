package Assignment4;

public class PartialSum implements Runnable{
    private final int from;
    private final int to;
    private int[] vec;
    private int sum; // This will hold the result of the partial sum

    public PartialSum(int _from, int _to,int[] vec){
        this.from = _from;
        this.to = _to;
        this.vec = vec;
        this.sum = 0;
    }

    public int getSum() {
        return sum;
    }

    @Override
    public void run(){
        sum = 0; // Initialize sum to 0 before starting the computation
        System.out.println(Thread.currentThread().getName() + " starting to sum from " + from + " to " + to);
        for(int i=from;i<to;i++)
            sum += vec[i]; // Compute the partial sum for the assigned range
        System.out.println(Thread.currentThread().getName() + " adding " + sum + " to total");
    }
}