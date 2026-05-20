package BenchMarking;

import models.HeightCurveData;
import org.junit.jupiter.api.Test;
import parser.HcParser;
import parser.Parser;

import java.io.*;

public class BinaryLoaderBenchMarking {

    private InputStream getStream(String s) {
        return BinaryLoaderBenchMarking.class.getClassLoader().getResourceAsStream(s);
    }

    private Parser parseOsm(String s) {
        Parser p = new Parser(getStream(s));
        p.parse();
        return p;
    }

    private HeightCurveData parseHc(String s) {
        HcParser h = new HcParser(getStream(s));
        HeightCurveData hc = h.parse();
        return hc;
    }

    private File serializeToTemp(String s) throws Exception {
        boolean isOsm = s.endsWith(".osm");
        boolean isHc = s.endsWith(".hc");

        Parser p = null;
        HeightCurveData hc = null;
        if (isOsm) {
            p = new Parser(getStream(s));
            p.parse();
        } else if (isHc) {
            HcParser h = new HcParser(getStream(s));
            hc = h.parse();
        }

        File tmp = File.createTempFile("benchmark", ".bin");
        tmp.deleteOnExit();

        try(ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(tmp)))) {
            if(isOsm) oos.writeObject(p);
            else if (isHc) oos.writeObject(hc);
        }

        return tmp;
    }

    void parserVsBinaryLoader(String input) throws Exception {
        boolean isOsm = input.endsWith(".osm");
        boolean isHc = input.endsWith(".hc");

        Parser p = null;
        HeightCurveData hc = null;

        //Warm up JVM - JIT
        for(int i = 0; i<2; i++) {
            if(isOsm) p = parseOsm(input);
            else if (isHc) hc = parseHc(input);
        }

        //Parse timing
        long start = System.nanoTime();
        for(int i = 0; i < 10; i++) {
            if(isOsm) parseOsm(input);
            else if(isHc) parseHc(input);
        }
        long avgParseTime = (System.nanoTime() - start) / 10;

        //Binary timing
        File bin = serializeToTemp(input);

        start = System.nanoTime();
        for(int i = 0; i < 10; i++) {
            try (ObjectInputStream ois = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(bin)))) {
                ois.readObject();
            }
        }
        long avgBinTime = (System.nanoTime() - start) / 10;

        String island = input.substring(input.indexOf('/')+1);
        int nodes = (isOsm) ? p.getOsmNodeMap().size() : 0;
        int way_curves = (isOsm) ? p.getOsmWayMap().size() : hc.curves.size();

        System.out.printf("%-12s Parse: %5.0f ms, Binary: %5.0f ms, Speedup: %5.1fx, Nodes: %6d, Ways/Curves: %d%n",
                island, avgParseTime/1e6, avgBinTime/1e6, (double)avgParseTime/avgBinTime, nodes, way_curves);
    }

    @Test
    void timedBinaryVsParser() throws Exception {
        //OSM BenchMarking
        parserVsBinaryLoader("osmData/tunoe.osm");
        parserVsBinaryLoader("osmData/samso.osm");
        parserVsBinaryLoader("osmData/bornholm.osm");

        //HC BenchMarking
        parserVsBinaryLoader("hcData/tunoe.hc");
        parserVsBinaryLoader("hcData/samso.hc");
        parserVsBinaryLoader("hcData/bornholm.hc");
    }

}
