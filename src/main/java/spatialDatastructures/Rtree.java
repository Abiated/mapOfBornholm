package spatialDatastructures;

import java.util.PriorityQueue;

import interfaces.InsertableInRtree;
import models.HeightCurve;
import models.OsmElement;
import models.OsmWay;
import models.OsmNode;

import java.util.ArrayList;
import java.util.List;

public class Rtree {
    RtreeNode root;

    public Rtree(List<InsertableInRtree> items, int M) {
        this.root = BulkLoader.bulkLoad(items, M);
    }

    public List<InsertableInRtree> search(float[] query) {
        List<InsertableInRtree> results = new ArrayList<>();
        searchRecursive(root, query, results);
        return results;
    }

    private void searchRecursive(RtreeNode node, float[] query, List<InsertableInRtree> results) {
        if (!node.intersects(query)) return;

        if (node.leaf) {
            for (Object obj : node.children) {
                InsertableInRtree element = (InsertableInRtree) obj;
                if (element.intersects(query)) {
                    results.add(element);
                }
            }
        } else {
            for (Object obj : node.children) {
                searchRecursive((RtreeNode) obj, query, results);
            }
        }
    }

//    private OsmNode getNearestNeighbor(long lat, long lon, RtreeNode root) {
//        PriorityQueue<PQWrapper> Search = new PriorityQueue<>();
//        OsmElement shortestNode = null;
//        Long bestDistance = Long.MAX_VALUE;
//
//        for(int i = 0; i<root.children.size(); i++ ){
//
//        }
//
//        shortestNode = getNearestNeighborrecursive(Search,shortestNode,bestDistance);
//
//        return (OsmNode) shortestNode;
//    }
//
//    private OsmNode getNearestNeighborrecursive(PriorityQueue s, OsmElement m, long bestDistance) {
//
//
//
//        if (s.p)
//        return n;
//    }

}
