package gui;

import general.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    static final String version = "1.2";

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("mainWindow.fxml"));
        primaryStage.setTitle("How To Steel Music");
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();
        Logger.log("starging gui",Logger.INFO,1);
    }


    /**
     * main metod
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
