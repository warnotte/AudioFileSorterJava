package io.github.warnotte.audiosorter.gui;

import atlantafx.base.theme.Dracula;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX Application entry point.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Apply AtlantaFX Dracula theme
        Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();
        controller.setStage(primaryStage);

        Scene scene = new Scene(root, 900, 650);

        primaryStage.setTitle("AudioFilesSorter");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(500);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
