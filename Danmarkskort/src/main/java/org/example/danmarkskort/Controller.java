package org.example.danmarkskort;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class Controller {

    @FXML private TextField textInputTo;
    @FXML private TextField textInputFrom;

    @FXML
    protected void findRouteClicked(ActionEvent event) {
        System.out.println("findRouteClicked");
        System.out.println("textInputTo: " + textInputTo.getText());
        System.out.println("textInputFrom: " + textInputFrom.getText());
    }


    // @FXML
    //private Label welcomeText;

    //@FXML
    //protected void onHelloButtonClick() {welcomeText.setText("Welcome to JavaFX Application!");
    }

