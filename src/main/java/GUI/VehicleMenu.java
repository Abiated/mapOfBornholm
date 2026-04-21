package GUI;

import PathFinder.VehicleType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

public class VehicleMenu extends MenuButton {
    private Label label;
    private VehicleType vehicleType;

    public VehicleMenu() {
        super("Click to choose mode of transportation");

        MenuItem Car = new MenuItem("CAR");
        MenuItem Bike = new MenuItem("BIKE");
        MenuItem None = new MenuItem("ON FOOT");

        this.getItems().addAll(Car, Bike, None);

        label = new Label("Mode of transportation has yet to be chosen");

        Car.setOnAction(event -> { label.setText("Mode of transportation: Car"); vehicleType = VehicleType.CAR; System.out.println(vehicleType);});
        Bike.setOnAction(event -> { label.setText("Mode of transportation: Bike"); vehicleType = VehicleType.BIKE; System.out.println(vehicleType);});
        None.setOnAction(event -> { label.setText("Mode of transportation: Walking"); vehicleType = VehicleType.WALK; System.out.println(vehicleType);});
    }

    public Label getLabel() {return label;}
    public VehicleType getVehicleType() {return vehicleType;}
}
