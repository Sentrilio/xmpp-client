package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.controllers.ScreensController;

public class Main extends Application {

	public static String screen1ID = "login";
	public static String screen1File = "/loginScreen.fxml";
	public static String screen2ID = "loggedIn";
	public static String screen2File = "/loggedInScreen.fxml";

    @Override
    public void start(Stage primaryStage) throws Exception{
		ScreensController screensController = new ScreensController();
		screensController.loadScreen(Main.screen1ID,Main.screen1File);
		screensController.loadScreen(Main.screen2ID,Main.screen2File);

		screensController.setScreen(Main.screen1ID);

		Group root = new Group();
		root.getChildren().addAll(screensController);
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.show();
		//
//        Parent root = FXMLLoader.load(getClass().getResource("/loginScreen.fxml"));
//        primaryStage.setTitle("Hello World");
//        primaryStage.setScene(new Scene(root, 300, 275));
//        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
