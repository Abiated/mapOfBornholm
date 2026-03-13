package org.example.danmarkskort;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        new View(stage);






    /*Group root = new Group(); // to have a root, which is needed for the scene
        Scene scene = new Scene(root,320, 240); //sizing redundant is you keep full screen
        stage.setFullScreen(true);

        stage.setTitle("Navigator");

        Text text = new Text();
        text.setText("Hello World");
        text.setX(50);
        text.setY(50);
        root.getChildren().add(text);

        TextField from =  new TextField("from");
        from.setPromptText("I want to go from..."); //this is not shown. I want it as a greyed out prompt text


        TextField to =  new TextField("to");
        to.setPromptText("I want to go to...");






        root.getChildren().addAll(to, from); //the fields are currently layered on top of each other
        */
    }





}
