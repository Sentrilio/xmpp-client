package sample.controllers;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import sample.Main;
import sample.XMPPClientSession;

import java.net.URL;
import java.util.ResourceBundle;

public class LogoutController extends XMPPClientSession implements Initializable, ControlledScreen {


	ScreensController screensController;

	@FXML
	private Button logoutButton;

	@FXML
	void logoutButtonClick(ActionEvent event) {
		System.out.println("Logout button clicked");
		try {
			connection.disconnect();
			System.out.println("disconnected");
			screensController.setScreen(Main.screen1ID);
		} catch (Exception e) {
			System.out.println("something went wrong with disconnecting");
			e.printStackTrace();
			screensController.setScreen(Main.screen2ID);
		}
	}


	public void initialize(URL location, ResourceBundle resources) {

	}

	public void setScreenParent(ScreensController screenParent) {
		screensController = screenParent;
	}
}
