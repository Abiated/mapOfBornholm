package ParserTest.parser.util.models;

import parser.Parser;
import interfaces.IParser;
import ParserTest.parser.util.TestParser;

import java.io.InputStream;

public class ParserResult {

    IParser actualParser;
    IParser expectedParser;
    String name;

    public ParserResult(InputStream osmFile, String jsonFile) {
        actualParser = new Parser(osmFile);
        expectedParser = new TestParser(jsonFile);
        name = osmFile.toString();

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
