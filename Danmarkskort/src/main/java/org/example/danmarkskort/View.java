package org.example.danmarkskort;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class View {

    //constructor
    public View(Stage stage) throws IOException {
        String fxmlFile = "/org/example/danmarkskort/nav_1.fxml";  //where to find

        URL url = getClass().getResource(fxmlFile); //fillokation. Definerer at det er en fil

        assert url.getFile() != null; //fortsæt kun hvis ikke filepath er null


        FXMLLoader loader = new FXMLLoader(url); //setup before loading

        Scene scene = new Scene(loader.load(), 600, 400);

        stage.setScene(scene);
        stage.show();
    }


}
