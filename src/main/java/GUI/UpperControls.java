package GUI;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class UpperControls extends BorderPane {
    private final VehicleMenu menu;
    private final Label routeStatusLabel;

    public UpperControls() {
        menu = new VehicleMenu();
        routeStatusLabel = new Label("Click start and target on the map");

        //Vehicle menu
        HBox menuTopLeft = new HBox(12, menu, menu.getLabel());
        menuTopLeft.setStyle("-fx-padding: 10px; -fx-alignment: center-left;");
        HBox statusTopRight = new HBox(routeStatusLabel);
        statusTopRight.setStyle("-fx-padding: 10px; -fx-alignment: center-right;");

        setLeft(menuTopLeft);
        setRight(statusTopRight);
    }

    public VehicleMenu getVehicleMenu() {return menu;}
    public void setRouteStatus(String text) {routeStatusLabel.setText(text);}
}
