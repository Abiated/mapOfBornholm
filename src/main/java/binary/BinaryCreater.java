package binary;

import javax.xml.stream.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BinaryCreater {

    public static void main(String[] args) {
        String inputFile = "/hc-osm-files/tuna/tuna.zip";
        String outputFile = "/hc-osm-files/Default/default(tuna).zip";

        try {
            convertOsmToBinary(inputFile, outputFile);
            System.out.println("Conversion finished.");
            System.out.println("Binary file created at: " + new File(outputFile).getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void convertOsmToBinary(String osmFile, String binFile) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();

        long nodeCount = countNodes(osmFile, factory);

        InputStream resourceStream = BinaryCreater.class.getResourceAsStream(osmFile);
        if (resourceStream == null) {
            throw new FileNotFoundException("Resource not found: " + osmFile);
        }

        try (
                InputStream inputStream = new BufferedInputStream(resourceStream);
                DataOutputStream out = new DataOutputStream(
                        new BufferedOutputStream(new FileOutputStream(binFile))
                )
        ) {
            out.writeUTF("OSMBIN1");
            out.writeLong(nodeCount);

            XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT &&
                        "node".equals(reader.getLocalName())) {

                    long id = Long.parseLong(reader.getAttributeValue(null, "id"));
                    double lat = Double.parseDouble(reader.getAttributeValue(null, "lat"));
                    double lon = Double.parseDouble(reader.getAttributeValue(null, "lon"));

                    out.writeLong(id);
                    out.writeDouble(lat);
                    out.writeDouble(lon);
                }

                if (event == XMLStreamConstants.START_ELEMENT &&
                        "way".equals(reader.getLocalName())) {
                    long id = Long.parseLong(reader.getAttributeValue(null, "id"));
                    List<String[]> tags = new ArrayList<>();
                    List<Long> refs = new ArrayList<>();

                    while (reader.hasNext()) {
                        int innerEvent = reader.next();

                        if (innerEvent == XMLStreamConstants.START_ELEMENT &&
                                "nd".equals(reader.getLocalName())) {

                            long ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                            refs.add(ref);
                        } else if (innerEvent == XMLStreamConstants.START_ELEMENT &&
                                "tag".equals(reader.getLocalName())) {

                            String key = reader.getAttributeValue(null, "k");
                            String value = reader.getAttributeValue(null, "v");
                            tags.add(new String[]{key, value});
                        }

                        else if (innerEvent == XMLStreamConstants.END_ELEMENT &&
                                "way".equals(reader.getLocalName())) {
                            break; // end of this way
                        }
                    }

                    out.writeLong(id);

                    out.writeInt(refs.size());
                    for (long ref : refs) {
                        out.writeLong(ref);
                    }

                    out.writeInt(tags.size());
                    for (String[] tag : tags) {
                        out.writeUTF(tag[0]); // key
                        out.writeUTF(tag[1]); // value
                    }

                }
            }

            reader.close();
        }
    }

    private static long countNodes(String osmFile, XMLInputFactory factory) throws Exception {
        long count = 0;

        InputStream resourceStream = BinaryCreater.class.getResourceAsStream(osmFile);
        if (resourceStream == null) {
            throw new FileNotFoundException("Resource not found: " + osmFile);
        }

        try (InputStream inputStream = new BufferedInputStream(resourceStream)) {
            XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT &&
                        "node".equals(reader.getLocalName())) {
                    count++;
                }
            }

            reader.close();
        }

        return count;
    }

    private static long countWays(String osmFile, XMLInputFactory factory) throws Exception{
        long count = 0;
        InputStream resourceStream = BinaryCreater.class.getResourceAsStream("/data/" + osmFile);
        if (resourceStream == null) {
            throw new FileNotFoundException("Resource not found: " + osmFile);
        }

        try (InputStream inputStream = new BufferedInputStream(resourceStream)) {
            XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT &&
                        "way".equals(reader.getLocalName())) {
                    count++;
                }
            }

            reader.close();
        }


        return count;
    }
}