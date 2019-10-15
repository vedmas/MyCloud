package ru.mycloud;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.mycloud.network.Network;

public class StartClient extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("My Cloud v1.0");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> new Network().closeConnection());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
