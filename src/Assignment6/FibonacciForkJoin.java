package Assignment6;

import Assignment6.FibonacciTask;

import java.util.concurrent.ForkJoinPool;

public class FibonacciForkJoin {
    public static void main(String[] args) {
        ForkJoinPool pool = new ForkJoinPool();

        int n = 30; // cuidado: cresce exponencialmente
        long result = pool.invoke(new FibonacciTask(n));

        System.out.println("Fibonacci(" + n + ") = " + result);
    }
}