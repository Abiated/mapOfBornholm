package GUI;

import PathFinder.VehicleType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

public class VehicleMenu extends MenuButton {
    private Label label;
    private VehicleType vehicleType;

    public VehicleMenu() {
        super("Choose mode of transportation");

        MenuItem Car = new MenuItem("CAR");
        MenuItem Bike = new MenuItem("BIKE");
        MenuItem None = new MenuItem("ON FOOT");

        this.getItems().addAll(Car, Bike, None);

        label = new Label("Mode of transportation:");

        Car.setOnAction(event -> { label.setText("Mode of transportation: Car"); vehicleType = VehicleType.CAR;});
        Bike.setOnAction(event -> { label.setText("Mode of transportation: Bike"); vehicleType = VehicleType.BIKE;});
        None.setOnAction(event -> { label.setText("Mode of transportation: Walking"); vehicleType = VehicleType.WALK;});
    }

    public Label getLabel() {return label;}
    public VehicleType getVehicleType() {return vehicleType;}
}
