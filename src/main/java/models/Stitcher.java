package models;

import java.io.Serializable;
import java.util.*;

public class Stitcher implements Serializable {
    private static final long serialVersionUID = 1L;

    private Stitcher() {}

    public static List<OsmWay> stitch(List<OsmWay> ways) {
        List<OsmWay> result = new ArrayList<>();
        Map<Long, OsmWay> startMap = new HashMap<>();

        for(OsmWay way : ways) {
            if(way.getNodes() == null || way.getNodes().size() < 2 || way.getNodes().isEmpty()) continue;

            if(way.getNodes().getFirst().getId() == way.getNodes().getLast().getId()) {
                result.add(way); //closed added to result
            } else startMap.put(way.getNodes().getFirst().getId() ,way); //Open needs stitching
        }

        Set<Long> visited = new HashSet<>();

        for(OsmWay startWay : startMap.values()) {
            long startId = startWay.getNodes().getFirst().getId();
            if(visited.contains(startId)) continue;

            List<OsmNode> chain = new ArrayList<>();
            OsmWay current = startWay;

            while(current != null) {
                long currentStartId = current.getNodes().getFirst().getId();
                if (visited.contains(currentStartId)) break;
                visited.add(currentStartId);

                List<OsmNode> nodes = current.getNodes();
                for(int i = 0; i < nodes.size()-1; i++) {
                    if(nodes.get(i) != null) chain.add(nodes.get(i));
                }

                long endId = nodes.getLast().getId();
                if(endId == startId) {
                    chain.add(nodes.getLast());
                    break;
                }

                current = startMap.get(endId);
            }

            if (chain.size() >= 2) {
                //TODO: NOT ALL COASTLINE NOW
                Map<String, String> tags = new HashMap<>();
                tags.put("natural", "coastline");
                result.add(new OsmWay(-1L, chain, tags));
            }

        }

        return result;
    }
}
