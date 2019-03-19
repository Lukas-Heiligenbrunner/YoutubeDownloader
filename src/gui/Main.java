package gui;

import general.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static final String version = "1.0";

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("mainWindow.fxml"));
        primaryStage.setTitle("How To Steel Music");
        primaryStage.setScene(new Scene(root, 800, 500));
        primaryStage.show();
        new Logger().log("starging gui",Logger.INFO,1);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
