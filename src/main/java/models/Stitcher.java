package models;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class Stitcher implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Stitcher() {}

    public static List<OsmWay> stitch(List<OsmWay> ways) {
        List<OsmWay> result = new ArrayList<>();

        List<OsmWay> openWays = new ArrayList<>();
        for(OsmWay way : ways) {
            if(way.getNodes() == null || way.getNodes().size() < 2 || way.getNodes().isEmpty()) continue;
            if(way.isClosed()) {
                result.add(way); //closed added to result
            } else {
                openWays.add(way); //Open needs stitching

            }
        }

        Map<Long, long[]> endpointToWay = new HashMap<>();

        Map<Long, OsmWay> forwardMap = new HashMap<>();
        Map<Long, OsmWay> backwardMap = new HashMap<>();

        for (OsmWay way : openWays) {
            long first = way.getNodes().getFirst().getId();
            long last = way.getNodes().getLast().getId();
            forwardMap.put(first, way);
            backwardMap.put(last,way);
        }

        Set<OsmWay> visited = new HashSet<>();

        for(OsmWay startWay : openWays) {

            long startId = startWay.getNodes().getFirst().getId();

            List<OsmNode> chain = new ArrayList<>();

            OsmWay current = startWay;
            boolean reversed = false;

            while(current != null && !visited.contains(current)) {
                visited.add(current);

                List<OsmNode> nodes = current.getNodes();
                if(reversed)
                    for(int i = nodes.size()-1; i > 0; i--) {
                        if(nodes.get(i) != null) chain.add(nodes.get(i));
                } else {
                    for (int i = 0; i < nodes.size() - 1; i++)
                        if(nodes.get(i) != null) chain.add(nodes.get(i));
                }

                long arrivedAt = reversed
                        ? nodes.getFirst().getId()
                        : nodes.getLast().getId();

                if (arrivedAt == startId) {
                    chain.add(reversed ? nodes.getFirst() : nodes.getLast());
                    break;
                }

                OsmWay nextForward = forwardMap.get(arrivedAt);
                OsmWay nextBackward = backwardMap.get(arrivedAt);

                if(nextForward != null && !visited.contains(nextForward)) {
                    current = nextForward;
                    reversed = false;
                } else if(nextBackward != null && !visited.contains(nextBackward)) {
                    current = nextBackward;
                    reversed = true;
                } else {
                    chain.add(reversed ? nodes.getFirst() : nodes.getLast());
                    break;
                }

            }

            if (chain.size() >= 2) {
                result.add(new OsmWay(-1L, chain, null));
            }
        }
        return result;
    }
}
