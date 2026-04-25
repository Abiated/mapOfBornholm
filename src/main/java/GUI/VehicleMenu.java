package GUI;

import PathFinder.VehicleType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

public class VehicleMenu extends MenuButton {
    private Label label;
    private VehicleType vehicleType;
    private Runnable onVehicleTypeChanged;

    public VehicleMenu() {
        super("Choose mode of transportation");

        MenuItem Car = new MenuItem("CAR");
        MenuItem Bike = new MenuItem("BIKE");
        MenuItem None = new MenuItem("ON FOOT");

        this.getItems().addAll(Car, Bike, None);

        label = new Label("Mode of transportation:");
        vehicleType = VehicleType.CAR;
        setText("CAR");
        label.setText("Mode of transportation: Car");

        Car.setOnAction(event -> { label.setText("Mode of transportation: Car"); vehicleType = VehicleType.CAR; setText("CAR"); notifyVehicleTypeChanged();});
        Bike.setOnAction(event -> { label.setText("Mode of transportation: Bike"); vehicleType = VehicleType.BIKE; setText("BIKE"); notifyVehicleTypeChanged();});
        None.setOnAction(event -> { label.setText("Mode of transportation: Walking"); vehicleType = VehicleType.WALK; setText("ON FOOT"); notifyVehicleTypeChanged();});
    }

    public Label getLabel() {return label;}
    public VehicleType getVehicleType() {return vehicleType;}
    public void setOnVehicleTypeChanged(Runnable onVehicleTypeChanged) {this.onVehicleTypeChanged = onVehicleTypeChanged;}

    private void notifyVehicleTypeChanged() {
        if (onVehicleTypeChanged != null) {
            onVehicleTypeChanged.run();
        }
    }
}
