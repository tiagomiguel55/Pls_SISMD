import java.awt.Color;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

/**
 * Histogram Equalization - Fork/Join Framework Implementation
 *
 * RecursiveTask<int[]> for the histogram — leaves build local int[256],
 * parents merge left + right results.
 * RecursiveAction for the pixel transformation — leaves write to disjoint
 * column slices, no synchronization needed.
 *
 * ForkJoinPool created once in the constructor with explicit parallelism
 * level so scalability experiments give meaningful results.
 */
public class ForkJoinFilter {

    private final Color[][] image;
    private final ForkJoinPool pool;
    private static final int THRESHOLD = 100;

    public ForkJoinFilter(String filename, int numThreads) {
        this.image = Utils.loadImage(filename);
        this.pool  = new ForkJoinPool(numThreads);
    }

    public void applyHistogramFilter(String outputFile) {
        Color[][] result = processImage();
        Utils.writeImage(result, outputFile);
    }

    public Color[][] processImage() {
        Color[][] tmp   = Utils.copyImage(image);
        int width       = tmp.length;
        int height      = tmp[0].length;
        int totalPixels = width * height;

        int[] hist       = pool.invoke(new HistogramTask(tmp, 0, width, height));
        int[] cumulative = computeCumulativeHistogram(hist);

        int cdfMin = 0;
        for (int i = 0; i < 256; i++) {
            if (cumulative[i] != 0) { cdfMin = cumulative[i]; break; }
        }

        pool.invoke(new TransformTask(tmp, 0, width, height, cumulative, totalPixels, cdfMin));
        return tmp;
    }

    private class HistogramTask extends RecursiveTask<int[]> {
        private final Color[][] tmp;
        private final int startX, endX, height;

        HistogramTask(Color[][] tmp, int startX, int endX, int height) {
            this.tmp = tmp; this.startX = startX; this.endX = endX; this.height = height;
        }

        @Override
        protected int[] compute() {
            if (endX - startX <= THRESHOLD) {
                int[] localHist = new int[256];
                for (int i = startX; i < endX; i++)
                    for (int j = 0; j < height; j++) {
                        Color px = tmp[i][j];
                        localHist[computeLuminosity(px.getRed(), px.getGreen(), px.getBlue())]++;
                    }
                return localHist;
            }
            int mid = startX + (endX - startX) / 2;
            HistogramTask left  = new HistogramTask(tmp, startX, mid, height);
            HistogramTask right = new HistogramTask(tmp, mid, endX, height);
            right.fork();
            int[] l = left.compute();
            int[] r = right.join();
            int[] merged = new int[256];
            for (int i = 0; i < 256; i++) merged[i] = l[i] + r[i];
            return merged;
        }
    }

    private int[] computeCumulativeHistogram(int[] hist) {
        int[] cumulative = new int[256];
        cumulative[0] = hist[0];
        for (int i = 1; i < 256; i++)
            cumulative[i] = cumulative[i - 1] + hist[i];
        return cumulative;
    }

    private class TransformTask extends RecursiveAction {
        private final Color[][] tmp;
        private final int startX, endX, height;
        private final int[] cumulative;
        private final int totalPixels, cdfMin;

        TransformTask(Color[][] tmp, int startX, int endX, int height,
                      int[] cumulative, int totalPixels, int cdfMin) {
            this.tmp = tmp; this.startX = startX; this.endX = endX; this.height = height;
            this.cumulative = cumulative; this.totalPixels = totalPixels; this.cdfMin = cdfMin;
        }

        @Override
        protected void compute() {
            if (endX - startX <= THRESHOLD) {
                for (int i = startX; i < endX; i++)
                    for (int j = 0; j < height; j++) {
                        Color px   = tmp[i][j];
                        int lum    = computeLuminosity(px.getRed(), px.getGreen(), px.getBlue());
                        double cdf = (double) cumulative[lum] / (double) (totalPixels - cdfMin);
                        int newLum = Math.min(255, (int) Math.round(255.0 * cdf));
                        tmp[i][j]  = new Color(newLum, newLum, newLum);
                    }
                return;
            }
            int mid = startX + (endX - startX) / 2;
            TransformTask left  = new TransformTask(tmp, startX, mid, height, cumulative, totalPixels, cdfMin);
            TransformTask right = new TransformTask(tmp, mid, endX, height, cumulative, totalPixels, cdfMin);
            right.fork();
            left.compute();
            right.join();
        }
    }

    private int computeLuminosity(int r, int g, int b) {
        return (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
    }
}