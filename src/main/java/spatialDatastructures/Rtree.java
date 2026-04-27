package spatialDatastructures;

import java.util.Comparator;
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

    public OsmNode getNearestNeighbor(float lat, float lon) {

        PriorityQueue<PQWrapper> Search = new PriorityQueue<>(
        Comparator.comparingDouble(PQWrapper::getPriorityDistance)
    );
        Search.add(new PQWrapper(root,rTreeCalculateDistence(root,lat,lon)));

        OsmNode shortestNode = null;
        float bestDistance = Float.MAX_VALUE;


        while (!Search.isEmpty()){

            PQWrapper currentBest = Search.peek();

            if(bestDistance < currentBest.getPriorityDistance()){
                break;
            }

            PQWrapper current = Search.poll();

            RtreeNode node = current.getRtreeNode();

            if (!node.leaf){

                for(int i = 0 ; i < node.children.size(); i++){
                    RtreeNode child = (RtreeNode) node.children.get(i);

                    float dist = rTreeCalculateDistence(child,lat,lon);

                    Search.add(new PQWrapper(child,dist));
                }

            } else {

                for(int i = 0 ; i<node.children.size(); i++){
                    OsmWay tempWay = (OsmWay) node.children.get(i);

                    for(int j = 0; j<tempWay.getNodes().size(); j++){
                        OsmNode tempnode = tempWay.getNodes().get(j);

                        float templat = tempnode.getLat();
                        float templon = tempnode.getLon();
                        float tempdistance = osmCalculateDistence(templat,templon,lat,lon);

                        if(bestDistance>tempdistance){
                            bestDistance = tempdistance;
                            shortestNode = tempnode;
                        }
                    }

                }

            }





        }


        return (OsmNode) shortestNode;
    }


    // Hjælp fra chatgpt til haversine formlen
    private float rTreeCalculateDistence(RtreeNode n, float lat, float lon){
        double R = 6371000;

        float minLon = n.bbox[0];
        float maxLon = n.bbox[1];
        float minLat = n.bbox[2];
        float maxLat = n.bbox[3];

        float clampedLat = Math.max(minLat, Math.min(lat, maxLat));
        float clampedLon = Math.max(minLon, Math.min(lon, maxLon));

        double dLat = Math.toRadians(clampedLat - lat);
        double dLon = Math.toRadians(clampedLon - lon);

        double lat1 = Math.toRadians(lat);
        double lat2 = Math.toRadians(clampedLat);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(lat1) * Math.cos(lat2) *
                                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        return (float) (R * c);
    }

    private float osmCalculateDistence(float Nlat, float Nlon, float lat, float lon){
        double R = 6371000;

        double dLat = Math.toRadians(Nlat - lat);
        double dLon = Math.toRadians(Nlon - lon);

        double lat1 = Math.toRadians(lat);
        double lat2 = Math.toRadians(Nlat);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(lat1) * Math.cos(lat2) *
                                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        return (float) (R * c);
    }


}
