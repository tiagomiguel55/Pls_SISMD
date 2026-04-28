import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Histogram Equalization - Thread Pool Implementation (local histograms)
 *
 * The ExecutorService is created ONCE in the constructor and reused across
 * all processImage() calls — thread creation overhead is paid only once.
 *
 * Stage 1 - Histogram: each task builds its own local int[256].
 *   After all tasks complete, local histograms are merged (256 additions).
 *   Zero contention — no AtomicInteger, no locks needed in Stage 1.
 *
 * Stage 2 - Cumulative: sequential (data dependency).
 * Stage 3 - Pixel remap: disjoint column slices — no synchronization needed.
 */
public class ThreadPoolFilter {

    private final Color[][] image;
    private final int numThreads;
    private final ExecutorService pool;

    public ThreadPoolFilter(String filename, int numThreads) {
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

        int[] hist       = computeHistogramParallel(tmp, width, height);
        int[] cumulative = computeCumulativeHistogram(hist);

        int cdfMin = 0;
        for (int i = 0; i < 256; i++) {
            if (cumulative[i] != 0) { cdfMin = cumulative[i]; break; }
        }

        transformPixelsParallel(tmp, width, height, cumulative, totalPixels, cdfMin);
        return tmp;
    }

    private int[] computeHistogramParallel(Color[][] tmp, int width, int height)
            throws Exception {

        int[][] localHists = new int[numThreads][256];
        int chunkSize      = width / numThreads;
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            final int startX   = t * chunkSize;
            final int endX     = (t == numThreads - 1) ? width : startX + chunkSize;
            futures.add(pool.submit((Callable<Void>) () -> {
                for (int i = startX; i < endX; i++)
                    for (int j = 0; j < height; j++) {
                        Color px = tmp[i][j];
                        localHists[threadId][computeLuminosity(
                                px.getRed(), px.getGreen(), px.getBlue())]++;
                    }
                return null;
            }));
        }
        for (Future<?> f : futures) f.get();

        // Merge local histograms — O(256 * numThreads), negligible cost
        int[] hist = new int[256];
        for (int t = 0; t < numThreads; t++)
            for (int i = 0; i < 256; i++)
                hist[i] += localHists[t][i];
        return hist;
    }

    private int[] computeCumulativeHistogram(int[] hist) {
        int[] cumulative = new int[256];
        cumulative[0] = hist[0];
        for (int i = 1; i < 256; i++)
            cumulative[i] = cumulative[i - 1] + hist[i];
        return cumulative;
    }

    private void transformPixelsParallel(Color[][] tmp, int width, int height,
                                         int[] cumulative, int totalPixels, int cdfMin)
            throws Exception {

        int chunkSize = width / numThreads;
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < numThreads; t++) {
            final int startX = t * chunkSize;
            final int endX   = (t == numThreads - 1) ? width : startX + chunkSize;
            futures.add(pool.submit((Callable<Void>) () -> {
                for (int i = startX; i < endX; i++)
                    for (int j = 0; j < height; j++) {
                        Color px   = tmp[i][j];
                        int lum    = computeLuminosity(px.getRed(), px.getGreen(), px.getBlue());
                        double cdf = (double) cumulative[lum] / (double) (totalPixels - cdfMin);
                        int newLum = Math.min(255, (int) Math.round(255.0 * cdf));
                        tmp[i][j]  = new Color(newLum, newLum, newLum);
                    }
                return null;
            }));
        }
        for (Future<?> f : futures) f.get();
    }

    private int computeLuminosity(int r, int g, int b) {
        return (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
    }
}