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
}