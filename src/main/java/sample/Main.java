package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.jivesoftware.smack.packet.Presence;
import sample.controllers.ControlledScreen;
import sample.controllers.LoggedInController;
import sample.controllers.LoginController;
import sample.controllers.ScreensController;
import sample.model.XMPPSession;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

	public static String screen1ID = "login";
	public static String screen1File = "/loginScreen.fxml";
	public static String screen2ID = "loggedIn";
	public static String screen2File = "/loggedInScreen.fxml";
	public static List<ControlledScreen> listOfControllers = new ArrayList<>();

	@Override
	public void start(Stage primaryStage) throws Exception {

		ScreensController screensController = new ScreensController();
		screensController.loadScreen(Main.screen1ID, Main.screen1File);
		screensController.loadScreen(Main.screen2ID, Main.screen2File);
		screensController.setScreen(Main.screen1ID);

		Group root = new Group();
		root.getChildren().addAll(screensController);
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.show();

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				System.out.println("Exit window button clicked");
				for (ControlledScreen controlledScreen : Main.listOfControllers) {
					if (controlledScreen instanceof LoginController) {
						try {
							XMPPSession xmppSession = ((LoginController) controlledScreen).xmppSession;
							if (xmppSession.connection != null) {
								xmppSession.presence.setMode(Presence.Mode.away);
								Presence presence = new Presence(Presence.Type.unavailable);
								presence.setMode(Presence.Mode.away);
								xmppSession.connection.disconnect(presence);
								System.out.println("Disconnected client");
							} else {
								System.out.println("There was no connection");
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
//					else if (controlledScreen instanceof LoggedInController) {
//						((LoggedInController) controlledScreen).cleanControllerAndRemoveSession();
//					}
				}
			}
		});

//        Parent root = FXMLLoader.load(getClass().getResource("/loginScreen.fxml"));
//        primaryStage.setTitle("Hello World");
//        primaryStage.setScene(new Scene(root, 800, 600));
//        primaryStage.show();
	}


	public static void main(String[] args) {
		launch(args);
	}
}
