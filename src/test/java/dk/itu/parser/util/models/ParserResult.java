package dk.itu.parser.util.models;

import dk.itu.parser.Parser;
import dk.itu.parser.interfaces.IParser;
import dk.itu.parser.util.TestParser;

public class ParserResult {

    IParser actualParser;
    IParser expectedParser;
    String name;

    public ParserResult(String osmFile, String jsonFile) {
        actualParser = new Parser(osmFile);
        expectedParser = new TestParser(jsonFile);
        name = osmFile;

        actualParser.parse();
        expectedParser.parse();
    }

    public IParser getActualParser() {
        return actualParser;
    }

    public IParser getExpectedParser() {
        return expectedParser;
    }

    public String getName() {
        return name;
    }
}
