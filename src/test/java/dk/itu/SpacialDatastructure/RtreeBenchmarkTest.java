package dk.itu.SpacialDatastructure;

import interfaces.InsertableInRtree;
import org.junit.jupiter.api.Test;
import spatialDatastructures.Rtree;

import java.util.List;

public class RtreeBenchmarkTest {

    @Test
    void benchmark1000() {

        Util.resetIds();
        List<InsertableInRtree> items =  Util.grid(32, 32, 1f); // ~1024 nodes

        long start = System.nanoTime();
        Rtree tree = new Rtree(items, 4);
        long end = System.nanoTime();
        double ms = (end - start) / 1_000_000.0;
        System.out.println("1000 items: " + ms + " ms");
    }

    @Test
    void benchmark10000() {

        Util.resetIds();
        List<InsertableInRtree> items =
                Util.grid(100, 100, 1f);

        long start = System.nanoTime();

        Rtree tree = new Rtree(items, 4);

        long end = System.nanoTime();

        double ms = (end - start) / 1_000_000.0;

        System.out.println("10000 items: " + ms + " ms");
    }

    @Test
    void benchmarkScaling() {

        int[] sizes = {100, 1000, 10000, 50000};

        for (int size : sizes) {

            Util.resetIds();

            int side = (int)Math.sqrt(size);

            List<InsertableInRtree> items =
                    Util.grid(side, side, 1f);

            long start = System.nanoTime();

            Rtree tree = new Rtree(items, 4);

            long end = System.nanoTime();

            double ms =  (end - start) / 1_000_000.0;

            System.out.printf("%d elements -> %.3f ms%n", items.size(), ms);
        }
    }

    @Test
    void benchmarkSearch() {

        Util.resetIds();

        List<InsertableInRtree> items = Util.grid(300, 300, 1f);

        Rtree tree = new Rtree(items, 4);

        float[] query = {50f, 100f, 50f, 100f};

        long total = 0;

        for (int i = 0; i < 1000; i++) {

            long start = System.nanoTime();

            tree.search(query);

            long end = System.nanoTime();

            total += end - start;
        }

        double avgMs = (total / 1000.0) / 1_000_000.0;
        System.out.println("Average query time: " + avgMs + " ms");
    }
}