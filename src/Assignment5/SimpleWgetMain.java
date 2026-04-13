package Assignment5;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SimpleWgetMain {
    public static void main(String[] args) throws Exception {
        String startUrl = "https://jsoup.org"; // URL inicial
        String domain = "https://jsoup.org";   // restrição de domínio
        int maxDepth = 2;
        int threads = 4;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        Set<String> visited = Collections.synchronizedSet(new HashSet<>());
        List<Future<String>> futures = Collections.synchronizedList(new ArrayList<>());

        // Submete primeira tarefa
        Future<String> firstFuture = executor.submit(new CrawlTask(startUrl, 0, executor, visited, maxDepth, futures, domain));
        futures.add(firstFuture);

        // Espera todas as tarefas terminarem
        boolean done = false;
        while (!done) {
            List<Future<String>> copy;
            synchronized(futures) {
                copy = new ArrayList<>(futures);
            }

            done = true;
            for (Future<String> f : copy) {
                if (!f.isDone()) {
                    done = false;
                    break;
                }
            }

            Thread.sleep(100); // evita busy-wait
        }

        // Imprime resultados de todas as tarefas
        List<Future<String>> copyFinal;
        synchronized(futures) {
            copyFinal = new ArrayList<>(futures);
        }

        for (Future<String> f : copyFinal) {
            try {
                System.out.println(f.get());
            } catch (Exception e) {
                System.err.println("Erro ao processar uma tarefa: " + e.getMessage());
            }
        }

        executor.shutdown();
        System.out.println("Crawling completo!");
    }
}