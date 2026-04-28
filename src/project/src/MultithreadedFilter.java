import java.awt.Color;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Histogram Equalization - Multithreaded Implementation (Producer-Consumer)
 *
 * Stage 1 - Histogram: Producer-Consumer pattern with ReentrantLock + Condition.
 *   Producers compute local histograms (no shared state) and place them on a
 *   bounded queue. The consumer merges partial histograms as they arrive.
 *   notEmpty: consumer awaits when queue is empty.
 *   notFull:  producers await when queue is at capacity.
 *
 * Stage 2 - Cumulative: sequential (data dependency).
 * Stage 3 - Pixel remap: disjoint column slices — no synchronization needed.
 */
public class MultithreadedFilter {

    private final Color[][] image;
    private final int numThreads;

    private final Queue<int[]>  histQueue      = new LinkedList<>();
    private final ReentrantLock queueLock      = new ReentrantLock();
    private final Condition     notEmpty       = queueLock.newCondition();
    private final Condition     notFull        = queueLock.newCondition();
    private static final int    QUEUE_CAPACITY = 4;

    public MultithreadedFilter(String filename, int numThreads) {
        this.image = Utils.loadImage(filename);
        this.numThreads = numThreads;
    }

    public void applyHistogramFilter(String outputFile) throws InterruptedException {
        Color[][] result = processImage();
        Utils.writeImage(result, outputFile);
    }

    public Color[][] processImage() throws InterruptedException {
        Color[][] tmp   = Utils.copyImage(image);
        int width       = tmp.length;
        int height      = tmp[0].length;
        int totalPixels = width * height;

        int[] hist       = computeHistogramProducerConsumer(tmp, width, height);
        int[] cumulative = computeCumulativeHistogram(hist);

        int cdfMin = 0;
        for (int i = 0; i < 256; i++) {
            if (cumulative[i] != 0) { cdfMin = cumulative[i]; break; }
        }

        transformPixelsParallel(tmp, width, height, cumulative, totalPixels, cdfMin);
        return tmp;
    }

    private int[] computeHistogramProducerConsumer(Color[][] tmp, int width, int height)
            throws InterruptedException {

        int[] mergedHist = new int[256];
        int chunkSize    = width / numThreads;

        // Consumer
        Thread consumer = new Thread(() -> {
            int received = 0;
            while (received < numThreads) {
                int[] partial = null;
                queueLock.lock();
                try {
                    while (histQueue.isEmpty()) notEmpty.await();
                    partial = histQueue.poll();
                    notFull.signalAll();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } finally {
                    queueLock.unlock();
                }
                for (int i = 0; i < 256; i++) mergedHist[i] += partial[i];
                received++;
            }
        });
        consumer.start();

        // Producers
        Thread[] producers = new Thread[numThreads];
        for (int t = 0; t < numThreads; t++) {
            final int startX = t * chunkSize;
            final int endX   = (t == numThreads - 1) ? width : startX + chunkSize;
            producers[t] = new Thread(() -> {
                int[] localHist = new int[256];
                for (int i = startX; i < endX; i++)
                    for (int j = 0; j < height; j++) {
                        Color px = tmp[i][j];
                        localHist[computeLuminosity(px.getRed(), px.getGreen(), px.getBlue())]++;
                    }
                queueLock.lock();
                try {
                    while (histQueue.size() >= QUEUE_CAPACITY) notFull.await();
                    histQueue.offer(localHist);
                    notEmpty.signal();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    queueLock.unlock();
                }
            });
            producers[t].start();
        }

        for (Thread p : producers) p.join();
        consumer.join();
        return mergedHist;
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
            throws InterruptedException {

        Thread[] threads = new Thread[numThreads];
        int chunkSize    = width / numThreads;

        for (int t = 0; t < numThreads; t++) {
            final int startX = t * chunkSize;
            final int endX   = (t == numThreads - 1) ? width : startX + chunkSize;
            threads[t] = new Thread(() -> {
                for (int i = startX; i < endX; i++)
                    for (int j = 0; j < height; j++) {
                        Color px   = tmp[i][j];
                        int lum    = computeLuminosity(px.getRed(), px.getGreen(), px.getBlue());
                        double cdf = (double) cumulative[lum] / (double) (totalPixels - cdfMin);
                        int newLum = Math.min(255, (int) Math.round(255.0 * cdf));
                        tmp[i][j]  = new Color(newLum, newLum, newLum);
                    }
            });
            threads[t].start();
        }
        for (Thread t : threads) t.join();
    }

    private int computeLuminosity(int r, int g, int b) {
        return (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
    }
}