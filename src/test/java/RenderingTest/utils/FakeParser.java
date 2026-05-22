package RenderingTest.utils;

import parser.Parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FakeParser extends Parser {

    private final List<Float> boundingBox;

    public FakeParser(List<Float> boundingBox) {
        super(new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        });
        this.boundingBox = boundingBox;
    }

    @Override
    public List<Float> getBoundingBox() {
        return boundingBox;
    }
}