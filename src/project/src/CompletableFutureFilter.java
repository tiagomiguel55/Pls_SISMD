import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Histogram Equalization - CompletableFuture Implementation
 *
 * Three pipeline stages chained with thenComposeAsync.
 * Single blocking point: .join() at the end of processImage().
 * ExecutorService created once in the constructor and reused.
 *
 * Stage 1: stripe futures merged pairwise with thenCombine (no shared state).
 * Stage 2: prefix-sum wrapped in supplyAsync to keep pipeline non-blocking.
 * Stage 3: one runAsync per stripe, aggregated with allOf().
 */
public class CompletableFutureFilter {

    private final Color[][] image;
    private final int numThreads;
    private final ExecutorService pool;

    public CompletableFutureFilter(String filename, int numThreads) {
        this.image = Utils.loadImage(filename);
        this.numThreads = numThreads;
        this.pool = Executors.newFixedThreadPool(numThreads);
    }

    public void applyHistogramFilter(String outputFile) throws Exception {
        Color[][] result = processImage();
        Utils.writeImage(result, outputFile);
    }

    public Color[][] processImage() throws Exception {
        Color[][] tmp   = Utils.copyImage(image);
        int width       = tmp.length;
        int height      = tmp[0].length;
        int totalPixels = width * height;

        computeHistogramAsync(tmp, width, height)
                .thenComposeAsync(hist -> computeCumulativeAsync(hist), pool)
                .thenComposeAsync(cum  -> transformPixelsAsync(tmp, width, height, cum, totalPixels), pool)
                .join();

        return tmp;
    }

    private CompletableFuture<int[]> computeHistogramAsync(
            Color[][] tmp, int width, int height) {

        int chunkSize = width / numThreads;
        List<CompletableFuture<int[]>> futures = new ArrayList<>();

        for (int t = 0; t < numThreads; t++) {
            final int startX = t * chunkSize;
            final int endX   = (t == numThreads - 1) ? width : startX + chunkSize;
            futures.add(CompletableFuture.supplyAsync(() -> {
                int[] localHist = new int[256];
                for (int i = startX; i < endX; i++)
                    for (int j = 0; j < height; j++) {
                        Color px = tmp[i][j];
                        localHist[computeLuminosity(px.getRed(), px.getGreen(), px.getBlue())]++;
                    }
                return localHist;
            }, pool));
        }

        CompletableFuture<int[]> merged = futures.get(0);
        for (int t = 1; t < futures.size(); t++) {
            final CompletableFuture<int[]> next = futures.get(t);
            merged = merged.thenCombine(next, (a, b) -> {
                int[] result = new int[256];
                for (int i = 0; i < 256; i++) result[i] = a[i] + b[i];
                return result;
            });
        }
        return merged;
    }

    private CompletableFuture<int[]> computeCumulativeAsync(int[] hist) {
        return CompletableFuture.supplyAsync(() -> {
            int[] cumulative = new int[256];
            cumulative[0] = hist[0];
            for (int i = 1; i < 256; i++)
                cumulative[i] = cumulative[i - 1] + hist[i];
            return cumulative;
        }, pool);
    }

    private CompletableFuture<Void> transformPixelsAsync(
            Color[][] tmp, int width, int height, int[] cumulative, int totalPixels) {

        int cdfMin = 0;
        for (int i = 0; i < 256; i++) {
            if (cumulative[i] != 0) { cdfMin = cumulative[i]; break; }
        }
        final int cdfMinFinal = cdfMin;

        int chunkSize = width / numThreads;
        CompletableFuture<?>[] stripeFutures = new CompletableFuture[numThreads];

        for (int t = 0; t < numThreads; t++) {
            final int startX = t * chunkSize;
            final int endX   = (t == numThreads - 1) ? width : startX + chunkSize;
            stripeFutures[t] = CompletableFuture.runAsync(() -> {
                for (int i = startX; i < endX; i++)
                    for (int j = 0; j < height; j++) {
                        Color px   = tmp[i][j];
                        int lum    = computeLuminosity(px.getRed(), px.getGreen(), px.getBlue());
                        double cdf = (double) cumulative[lum] / (double) (totalPixels - cdfMinFinal);
                        int newLum = Math.min(255, (int) Math.round(255.0 * cdf));
                        tmp[i][j]  = new Color(newLum, newLum, newLum);
                    }
            }, pool);
        }
        return CompletableFuture.allOf(stripeFutures);
    }

    private int computeLuminosity(int r, int g, int b) {
        return (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
    }
}