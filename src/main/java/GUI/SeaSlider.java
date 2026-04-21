package GUI;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.beans.value.ChangeListener;

public class SeaSlider extends Slider {

    private Label sliderLabel;

    public SeaSlider(int max, String label, ChangeListener<Number> change) {
        super(0, max, 0);
        setShowTickLabels(true);
        setShowTickMarks(true);

        this.sliderLabel = new Label(label);

        this.valueProperty().addListener((obs, oldValue, newValue) -> {
            sliderLabel.setText(String.format("Sea level: %.1f m", newValue.floatValue()));
            change.changed(obs, oldValue, newValue);
        });
    }

    public Label getSliderLabel() {return sliderLabel;}




}
