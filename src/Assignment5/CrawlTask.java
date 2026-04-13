package Assignment5;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

// CrawlTask agora com Callable e limitação de links e domínio
class CrawlTask implements Callable<String> {
    private final String url;
    private final int depth;
    private final ExecutorService executor;
    private final Set<String> visited;
    private final int maxDepth;
    private final List<Future<String>> futures;
    private static final int MAX_LINKS = 50;       // máximo de páginas a baixar
    private static volatile int totalVisited = 0;  // contador global
    private final String domain;                   // domínio inicial para filtrar links

    public CrawlTask(String url, int depth, ExecutorService executor, Set<String> visited, int maxDepth, List<Future<String>> futures, String domain) {
        this.url = url;
        this.depth = depth;
        this.executor = executor;
        this.visited = visited;
        this.maxDepth = maxDepth;
        this.futures = futures;
        this.domain = domain;
    }

    @Override
    public String call() {
        // Limite de profundidade ou páginas visitadas
        synchronized (CrawlTask.class) {
            if (depth > maxDepth || visited.contains(url) || totalVisited >= MAX_LINKS) {
                return "Skipped: " + url;
            }
            totalVisited++;
        }

        visited.add(url);
        System.out.println("Visiting: " + url);

        try {
            Document document = Jsoup.connect(url).get();
            savePage(url, document.html());

            // Processa links
            Elements links = document.select("a[href]");
            for (Element link : links) {
                String nextUrl = link.attr("abs:href");

                // Ignora links fora do domínio inicial
                if (!nextUrl.startsWith(domain)) continue;

                if (!visited.contains(nextUrl)) {
                    Future<String> future = executor.submit(new CrawlTask(nextUrl, depth + 1, executor, visited, maxDepth, futures, domain));
                    synchronized (futures) {
                        futures.add(future);
                    }
                }
            }

            return "Downloaded: " + url;

        } catch (IOException e) {
            return "Failed: " + url + " (" + e.getMessage() + ")";
        }
    }

    private void savePage(String url, String content) throws IOException {
        String filename = url.replaceAll("[^a-zA-Z0-9\\.\\-]", "_") + ".html";
        try (FileOutputStream out = new FileOutputStream(filename)) {
            out.write(content.getBytes());
        }
    }
}