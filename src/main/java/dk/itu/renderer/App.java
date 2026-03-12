package dk.itu.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends DrawingApp {

    private final List<Drawable> drawableList = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        /* BASIC COMPONENT SETUP */
        stage.setTitle("Drawing App"); // Sets the title
        stage.setResizable(false); // Set non-resizable
        stage.setWidth(getWIDTH()); // Sets width
        stage.setHeight(getHEIGHT()); // Sets height

        stage.setScene(
                new Scene(
                        new StackPane( // Root component => StackPane stacks components (like stacking cards)
                                this.imageView // Our primary component from DrawingApp
                        // ... <= Components can be rendered on top of our map
                        )
                )
        );
        stage.show(); // Sets the stage as visible

        /* DRAWING */
        // Example with line across screen
        this.drawableList.add((gc, color, strokeWidth) -> {
            gc.setColor(color);
            gc.drawLine(100, 100, 700, 700);
        });

        this.draw();

        this.render(); // This will actually show what has been drawn
    }

    public void draw() {
        // This provides our main drawing API
        Graphics2D gc = getNewGraphicsContext();

        // Uncomment if drawing actual OSM data
        // DrawingUtils.applyTransformation(gc);
        // IMPORTANT - IF DRAWING BONDEHOLM, ENABLE THIS AND MULTIPLY LONGITUDE BY 0.56 AND LATITUDE BY -1 (the reason why will be explained later in the course)
        // Here we can do any pre-processing such a setting the background or transformations
        gc.setColor(Color.BLUE); // Sets the color
        gc.fillRect(0, 0, getWIDTH(), getHEIGHT()); // Fills the rectangle with the set color

        // Draws our drawables
        for (Drawable drawable : drawableList) {
            // In the actual project, color and strokeWidth arguments won't be in the draw method signature
            // Color and strokeWidth are only there for test purposes in the assignment
            drawable.drawForTest(gc, Color.BLACK, 2);
        }
    }
}
