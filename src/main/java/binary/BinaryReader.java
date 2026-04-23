package binary;

import java.io.*;
import java.util.*;

public class BinaryReader {

    public static void main(String[] args) {
        String file = "tunoe.bin";

        try {
            readBinary(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void readBinary(String file) throws Exception {

        DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(file))
        );

        // 1. Read header
        String header = in.readUTF();
        System.out.println("Header: " + header);

        if (!"OSMBIN1".equals(header)) {
            throw new IOException("Wrong file format!");
        }

        // 2. Read node count
        long nodeCount = in.readLong();
        System.out.println("Nodes: " + nodeCount);

        // 3. Read nodes
        Map<Long, double[]> nodeMap = new HashMap<>();

        for (long i = 0; i < nodeCount; i++) {
            long id = in.readLong();
            double lat = in.readDouble();
            double lon = in.readDouble();

            nodeMap.put(id, new double[]{lat, lon});

            // Print first few nodes for testing
            if (i < 5) {
                System.out.println("Node " + id + " → (" + lat + ", " + lon + ")");
            }
        }

        in.close();

        System.out.println("Finished reading nodes.");
    }
}