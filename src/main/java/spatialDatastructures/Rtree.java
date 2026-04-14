package spatialDatastructures;

import models.OsmWay;

import java.util.ArrayList;
import java.util.List;

class Rtree {
    RtreeNode root;

    public Rtree(List<OsmWay> items, int M) {
        this.root = BulkLoader.bulkLoad(items, M);
    }

    public List<OsmWay> search(float[] query) {
        List<OsmWay> results = new ArrayList<>();
        searchRecursive(root, query, results);
        return results;
    }

    private void searchRecursive(RtreeNode node, float[] query, List<OsmWay> results) {
        if (!node.intersects(query)) return;

        if (node.leaf) {
            for (Object obj : node.children) {
                OsmWay way = (OsmWay) obj;
                if (way.intersects(query)) {
                    results.add(way);
                }
            }
        } else {
            for (Object obj : node.children) {
                searchRecursive((RtreeNode) obj, query, results);
            }
        }
    }
}
