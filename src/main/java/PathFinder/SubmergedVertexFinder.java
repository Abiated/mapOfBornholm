package PathFinder;

//import dk.itu.parser.HeightCurve;
//import dk.itu.parser.HeightCurveData;
//import dk.itu.parser.Parser;
//import dk.itu.renderer.osm.OsmNode;

import models.HeightCurve;
import models.HeightCurveData;
import parser.Parser;
import models.OsmNode;

import java.util.HashSet;
import java.util.Set;

public class SubmergedVertexFinder {

    /**
     * Finds all submerged vertices in the graph.
     *
     * Optimized version:
     * Instead of looping through ALL parser nodes,
     * we only loop through nodes that are actually vertices in the road graph.
     */
    public static Set<Integer> findSubmergedVertices2(EdgeWeightedDigraph graph, Parser parser, HeightCurveData hcData, float seaLevel
    ) {
        Set<Integer> submergedVertices = new HashSet<>();

        if (graph == null || parser == null || hcData == null) {
            return submergedVertices;
        }

        // Reset previous flooding state
        hcData.sea.resetSubmerged();

        // Apply new sea level
        hcData.sea.submerge(seaLevel);

        // ONLY iterate through graph vertices
        for (int vertex = 0; vertex < graph.vertexCount(); vertex++) {

            Long nodeId = graph.nodeIdOf(vertex);
            if (nodeId == null) {continue;}

            OsmNode node = parser.getOsmNodeMap().get(nodeId);
            if (node == null) {continue;
            }

            if (isSubmerged(node, hcData)) {
                submergedVertices.add(vertex);
            }
        }

        return submergedVertices;
    }

    /**
     * Checks whether a node lies inside any submerged height curve.
     */
    private static boolean isSubmerged(OsmNode node, HeightCurveData hcData) {

        for (HeightCurve curve : hcData.getCurves()) {

            if (!curve.isSubmerged()) {
                continue;
            }

            if (curve.getCoords().isEmpty()) {
                continue;
            }

            if (curve.getRegionPath().contains(node.getLon(), node.getLat())) {
                return true;
            }
        }

        return false;
    }
}

