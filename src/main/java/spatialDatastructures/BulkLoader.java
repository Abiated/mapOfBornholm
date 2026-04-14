package spatialDatastructures;

import models.OsmWay;

import java.util.*;

class BulkLoader {

    public static RtreeNode bulkLoad(List<OsmWay> items, int M) {
        List<RtreeNode> level = buildLeafLevel(items, M);

        while (level.size() > 1) {
            level = buildUpperLevel(level, M);
        }

        return level.get(0);
    }

    private static List<RtreeNode> buildLeafLevel(List<OsmWay> items, int M) {
        if (items.size() <= M) {
            return List.of(new RtreeNode(new ArrayList<>(items), true));
        }

        int S = (int) Math.ceil(Math.sqrt((float) items.size() / M));

        items.sort(Comparator.comparing(i -> i.getBoundingBox()[0]));

        List<RtreeNode> nodes = new ArrayList<>();
        int sliceSize = (int) Math.ceil((float) items.size() / S);

        for (int i = 0; i < items.size(); i += sliceSize) {
            List<OsmWay> slice = items.subList(i, Math.min(i + sliceSize, items.size()));

            slice.sort(Comparator.comparing(i2 -> i2.getBoundingBox()[2]));

            for (int j = 0; j < slice.size(); j += M) {
                List<OsmWay> group = slice.subList(j, Math.min(j + M, slice.size()));
                nodes.add(new RtreeNode(new ArrayList<>(group), true));
            }
        }

        return nodes;
    }

    private static List<RtreeNode> buildUpperLevel(List<RtreeNode> nodes, int M) {
        int S = (int) Math.ceil(Math.sqrt((float) nodes.size() / M));

        nodes.sort(Comparator.comparing(n -> n.bbox[0]));

        List<RtreeNode> newLevel = new ArrayList<>();
        int sliceSize = (int) Math.ceil((float) nodes.size() / S);

        for (int i = 0; i < nodes.size(); i += sliceSize) {
            List<RtreeNode> slice = nodes.subList(i, Math.min(i + sliceSize, nodes.size()));

            slice.sort(Comparator.comparing(n -> n.bbox[2]));

            for (int j = 0; j < slice.size(); j += M) {
                List<RtreeNode> group = slice.subList(j, Math.min(j + M, slice.size()));
                newLevel.add(new RtreeNode(new ArrayList<>(group), false));
            }
        }

        return newLevel;
    }
}
