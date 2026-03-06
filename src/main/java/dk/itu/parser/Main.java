package dk.itu.parser;

public class Main {

    public static void main(String[] args) {
        Parser parser = new Parser("Bornholm(mini).osm");
        parser.parse();

    }
}
