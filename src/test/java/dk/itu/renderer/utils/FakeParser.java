package dk.itu.renderer.utils;

import dk.itu.parser.Parser;

import java.util.List;

public class FakeParser extends Parser {

    private final List<Float> boundingBox;

    public FakeParser(List<Float> boundingBox) {
        super("");
        this.boundingBox = boundingBox;
    }

    @Override
    public List<Float> getBoundingBox() {
        return boundingBox;
    }
}