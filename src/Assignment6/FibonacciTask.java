package Assignment6;

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

class FibonacciTask extends RecursiveTask<Long> {
    private final int n;

    public FibonacciTask(int n) {
        this.n = n;
    }

    @Override
    protected Long compute() {
        if (n <= 1) return (long) n;

        FibonacciTask f1 = new FibonacciTask(n - 1);
        FibonacciTask f2 = new FibonacciTask(n - 2);

        f1.fork();                // executa em paralelo
        long result2 = f2.compute(); // calcula diretamente
        long result1 = f1.join();    // espera pelo resultado

        return result1 + result2;
    }
}

