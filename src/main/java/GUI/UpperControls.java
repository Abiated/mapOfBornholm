package GUI;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class UpperControls extends BorderPane {

    public UpperControls() {
        VehicleMenu menu = new VehicleMenu();

        //Vehicle menu
        HBox menuTopLeft = new HBox(12, menu, menu.getLabel());
        menuTopLeft.setStyle("-fx-padding: 10px; -fx-alignment: center-left;");

        setLeft(menuTopLeft);
    }

}
