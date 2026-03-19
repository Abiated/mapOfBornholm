package org.example.danmarkskort;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class Controller {

    private SuperAffine transform = new SuperAffine();
    private double lastX;
    private double lastY;

    /** Updates view to show new things based on the input ex. MouseEvent, ScrollEvents etc*/
    private void updateView(){
        //TODO: Change code when we render base on parsed input rather than using imageView on screenshot
        imageView.setTranslateX(transform.getTranslateX());
        imageView.setTranslateY(transform.getTranslateY());
        imageView.setScaleX(transform.getScaleX());
        imageView.setScaleY(transform.getScaleY());
    }

    @FXML private TextField textInputTo;
    @FXML private TextField textInputFrom;

    //TODO: Exchange with the Pane we render map in. ImageView with the image is a placeholder to get a feel for the final GUI
    @FXML private ImageView imageView;


 //for panning. Saves the coordinates when user presses the mouse
    @FXML
    protected void onMousePressed(MouseEvent mouseEvent) {
        lastX = mouseEvent.getX();
        lastY = mouseEvent.getY();
    }

    // also for panning. Moves the content of the scene by however much the user dragged the mouse
    @FXML
    protected void onMouseDragged(MouseEvent mouseEvent) {
        double deltaX = mouseEvent.getX() - lastX;
        double deltaY = mouseEvent.getY() - lastY;

        transform.prependTranslation(deltaX, deltaY);

        //TODO: create method that actually updates view
        updateView();

        //updates the x and y coordinates
        lastX = mouseEvent.getX();
        lastY = mouseEvent.getY();
    }

    //for zoom done when the user uses the scroll on mouse or mousepad
    @FXML
    protected void onScroll(ScrollEvent scrollEvent) {
        //save a factor we zoom by. Useful to ensure we do not distort the view when zooming in or out
        double zoomByFactor;
        if(scrollEvent.getDeltaY() > 0) {zoomByFactor = 1.1;} //zooms in
        else{zoomByFactor = -1.1;} //zooms out

        transform.prependTranslation(zoomByFactor, zoomByFactor);

        //TODO: create method that actually updates view
        updateView();
    }

    @FXML
    protected void findRouteClicked(ActionEvent event) {
        //TODO: take input from text fields, call route finder method from model, draw route in GUI
        //For now: it just prints the method clicked what's in text fields
        System.out.println("findRouteClicked");
        System.out.println("textInputTo: " + textInputTo.getText());
        System.out.println("textInputFrom: " + textInputFrom.getText());
    }

    @FXML
    protected void mapClicked(MouseEvent event) {
        //TODO: when map is clicked first time (uneven number) it should place a "to" point and fill out the TextField "to"
        //Perhaps TODO: when map is clicked second time (even number) it should place a from point  and fill out the TextField "from"
        // Do we want it instead to say: from and then to?
        // Which Pane is the drawn map in? How do we get the specific node from model?
        //remember to create fields up top to match what is pressed. Here I guess MapPane or canvas. Currently it is imageView.
        System.out.println("mapClicked");
    }

    @FXML
    protected void scrollBarIncrease(){ //event click on up arrow in scrollbar or drag scrollbar
        // TODO: Increase sealevel in model by x meters, re-render, recalculate route if path in shortest route is flooded
        // Should we increase by 0,5 or should user be able to pull however much they want?
        // Take inspiration from scroll in PanZoom to see what they

    }

    @FXML
    protected void scrollBarDecrease(){
        // TODO: Decrease sealevel in model by x meters, re-render, recalculate route if path in shortest route is flooded

    }

    }

