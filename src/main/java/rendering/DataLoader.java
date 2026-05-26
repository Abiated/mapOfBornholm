package rendering;

import models.HeightCurveData;
import parser.HcParser;
import parser.Parser;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DataLoader {

    private static final String DEFAULT_OSM = "/hc-osm-files/Default/bornholmosm.bin";
    private static final String DEFAULT_HC = "/hc-osm-files/Default/bornholmhc.bin";
    private static final String DEFAULT_ZIP = "/hc-osm-files/Default/bornholmzip.bin";


    public record MapData(Parser OsmData, HeightCurveData HcData) {}

    public MapData load(File chosen) {
        if (chosen == null) {
            // No file chosen – loads default
            return loadDefaultData();
        } else if (chosen.getName().endsWith(".zip")) {
            //Zip file chosen
            return loadCache(chosen, "zip");
        } else if (chosen.getName().endsWith(".hc")) {
            //Hc file chosen
            return loadCache(chosen, "hc");
        } else if (chosen.getName().endsWith(".osm")) {
            //Osm file chosen
            return loadCache(chosen, "osm");
        } else if (chosen.getName().endsWith(".bin")) {
            if (chosen.getName().endsWith("zip.bin")) return loadCache(chosen, "zip.bin");
            if (chosen.getName().endsWith("osm.bin")) return loadCache(chosen, "osm.bin");
            if (chosen.getName().endsWith("hc.bin")) return loadCache(chosen, "hc.bin");
        }
        return null;
    }

    //BINARY HANDLER
    private void saveCache(File cache, Parser OsmData, HeightCurveData HCdata) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(cache)))) {
            oos.writeObject(OsmData);
            oos.writeObject(HCdata);
            System.out.println("Saved cache: " + cache.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MapData loadCache(File chosen, String type) {
        //Checks file type, of not .bin it searches for binary counterpart
        File cache = switch (type) {
            case "zip" -> new File(chosen.getAbsolutePath().replace(".zip", "zip.bin"));
            case "osm" -> new File(chosen.getAbsolutePath().replace(".osm", "osm.bin"));
            case "hc" -> new File(chosen.getAbsolutePath().replace(".hc", "hc.bin"));
            default -> new File(chosen.getAbsolutePath());
        };

        Parser OsmData = null;
        HeightCurveData HCdata = null;

        if(cache == null) return null;

        if (cache.exists()) {
            try (ObjectInput ois = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(cache)))) {
                OsmData = (Parser) ois.readObject();
                HCdata = (HeightCurveData) ois.readObject();
                //System.out.println("Running cache");
                return new MapData(OsmData, HCdata);
            } catch (Exception e) {
                System.err.println("Cache failed, reparsing: " + e.getMessage());
            }
        } else {
            //Checks which file to load of no binary file exist
            if (type.contains("bin")) throw new IllegalArgumentException("No such file exist");
            switch (type) {
                case "zip" -> {
                    try {
                        ZipInputStream zip = new ZipInputStream(new FileInputStream(chosen));
                        ZipEntry entry;
                        boolean loadedHc = false;
                        boolean loadedOsm = false;

                        while ((entry = zip.getNextEntry()) != null) {
                            if (entry.isDirectory() || entry.getName().contains("__MACOSX") || entry.getName().contains(".DS_Store")) {
                                zip.closeEntry();
                                continue;
                            }

                            byte[] bytes = zip.readAllBytes();

                            if (entry.getName().endsWith(".hc")) {
                                HCdata = new HcParser(new ByteArrayInputStream(bytes)).parse();
                                loadedHc = true;
                            }
                            if (entry.getName().endsWith(".osm")) {
                                OsmData = new Parser(new ByteArrayInputStream(bytes));
                                OsmData.parse();
                                loadedOsm = true;
                            }
                            zip.closeEntry();
                        }

                        if (!loadedHc || !loadedOsm) {
                            throw new IllegalArgumentException("Zip file must contain both .hc and .osm files");
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                case "osm" -> {
                    OsmData = new Parser(openFile(chosen));
                    OsmData.parse();
                }
                case "hc" -> {
                    HCdata = new HcParser(openFile(chosen)).parse();
                }
            }

            saveCache(cache, OsmData, HCdata);
        }
        return new MapData(OsmData, HCdata);
    }

    private InputStream openFile(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //LOADING DEFAULT DATA
    public MapData loadDefaultData() {
        InputStream is = getClass().getResourceAsStream(DEFAULT_ZIP);
        if (is == null) {
            throw new IllegalStateException("Default binary not found on classpath: " + DEFAULT_ZIP);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(is))) {
            Parser osmData    = (Parser)          ois.readObject();
            HeightCurveData hcData = (HeightCurveData) ois.readObject();
            return new MapData(osmData, hcData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load default data from " + DEFAULT_ZIP, e);
        }
    }

}
