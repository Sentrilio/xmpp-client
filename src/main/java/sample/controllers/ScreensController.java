package sample.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import sample.Main;

import java.util.HashMap;

public class ScreensController extends StackPane {

	private HashMap<String, Node> screens = new HashMap<String, Node>();

	public ScreensController() {
		super();
	}

	public void addScreen(String name, Node screen) {
		screens.put(name, screen);
	}

	public Node getScreen(String name) {
		return screens.get(name);
	}

	public boolean loadScreen(String name, String resource) {
		try {
			FXMLLoader myLoader = new FXMLLoader(getClass().getResource(resource));
			Parent loadScreen = (Parent) myLoader.load();
			ControlledScreen myScreenController = ((ControlledScreen) myLoader.getController());
			myScreenController.setScreenParent(this);
			addScreen(name, loadScreen);
			Main.listOfControllers.add(myScreenController);
			return true;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
//	public boolean loadScreen(String name, String resource,ControlledScreen previousController) {
//		try {
//			FXMLLoader myLoader = new FXMLLoader(getClass().getResource(resource));
//			Parent loadScreen = (Parent) myLoader.load();
//			ControlledScreen myScreenController = ((ControlledScreen) myLoader.getController());
//			myScreenController.setScreenParent(this);
//			addScreen(name, loadScreen);
//
//			////////////////////////////////////////
//			if(resource.equals("/loggedInScreen.fxml")){
//				FXMLLoader loader = new FXMLLoader(getClass().getResource("/loginScreen.fxml"));
//				Parent root = loader.load();
//				LoginController controller = loader.getController();
//				System.out.println(controller.temp);
//			}
//			return true;
//		} catch (Exception e) {
//			System.out.println(e.getMessage());
//			return false;
//		}
//	}

	public boolean setScreen(final String name) {
		if (screens.get(name) != null) {
			final DoubleProperty opacity = opacityProperty();

			if (!getChildren().isEmpty()) {

				Timeline fade = new Timeline(
						new KeyFrame(Duration.ZERO, new KeyValue(opacity, 1.0)),
						new KeyFrame(new Duration(100), new EventHandler<ActionEvent>() {
							//							@Override
							public void handle(ActionEvent t) {
								getChildren().remove(0);
								getChildren().add(0, screens.get(name));
								Timeline fadeIn = new Timeline(
										new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)),
										new KeyFrame(new Duration(100), new KeyValue(opacity, 1.0)));
								fadeIn.play();
							}
						}, new KeyValue(opacity, 0.0)));
				fade.play();
			} else {
				setOpacity(0.0);
				getChildren().add(screens.get(name));
				//tu moze byc cokolwiek
				Timeline fadeIn = new Timeline(
						new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)),
						new KeyFrame(new Duration(100), new KeyValue(opacity, 1.0)));
				fadeIn.play();
				//dotąd
			}
			return true;
		} else {
			System.out.println("screen hasn't been loaded!!! \n");
			return false;
		}
	}

	public boolean unloadScreen(String name) {
		if(screens.remove(name)==null){
			System.out.println("Screen didn't exist");
			return false;
		}else{
			return true;
		}
	}
}