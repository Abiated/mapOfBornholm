package GUI;

import javafx.beans.value.ChangeListener;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class BottomControls extends BorderPane {
    private final ScaleBar scale;
    private final SeaSlider slider;

    public BottomControls(ChangeListener<Number> change) {
        slider = new SeaSlider(25, "Sea level: 0.0 m", change);

        //HBox that puts slider and sliderLabel side by side with 12px padding
        HBox seaSlider = new HBox(12.0, slider, slider.getSliderLabel());
        //Styles controls with 10px padding and alignment
        seaSlider.setStyle("-fx-padding: 10; -fx-alignment: center-left;");

        scale = new ScaleBar();

        //HBox that puts scale on the screen
        HBox scaleBar = new HBox(scale);
        //Styles controls with 20px padding and alignment
        scaleBar.setStyle("-fx-padding: 20; -fx-alignment: center-right;");

        setRight(scaleBar);
        setLeft(seaSlider);
    }

    public void update(float zoom) {
        scale.update(zoom);
    }

}
