package dk.itu.Model;

import dk.itu.parser.HeightCurve;
import dk.itu.parser.HeightCurveData;
import dk.itu.parser.Parser;
import dk.itu.renderer.osm.OsmNode;

import java.util.HashSet;
import java.util.Set;

public class SubmergedVertexFinder {
    public static Set<Integer> findSubmergedVertices(EdgeWeightedDigraph graph, Parser parser, HeightCurveData hcData, double seaLevel) {
        Set<Integer> submergedVertices = new HashSet<>();

        if (graph == null || parser == null || hcData == null) {
            return submergedVertices; //give back an empty set if either are null
        }

        hcData.getSea().resetSubmerged(); //reset to make sure we begin from stratch
        hcData.getSea().submerge(seaLevel); //submerge using the new sealevel

        for (OsmNode node : parser.getOsmNodeMap().values()) {
            if (!isSubmerged(node, hcData)) {
                continue;
            }

            try {
                submergedVertices.add(graph.indexOf(node.getId()));
            } catch (IllegalArgumentException e) {
                // Node is not part of the road graph, so it does not matter for routing.
            }
        }

        return submergedVertices;
    }

    private static boolean isSubmerged(OsmNode node, HeightCurveData hcData){
        for (HeightCurve curve : hcData.getCurves()) {
            if (!curve.isSubmerged()) {
                continue; //if not submerged continue resulting in return false TODO: is this relevant or can it be deleted and go directly to return false since it does not work for true? Or are these checks important to mitigate errors in the 3rd if stm
            }
            if (curve.getCoords().isEmpty()) {
                continue; //if coordinates are empty continue resulting in return false TODO: same as above
            }

            if (curve.getRegionPath().contains(node.getLon(), node.getLat())) {
                return true;
            }
        }

        return false;
    }
}
