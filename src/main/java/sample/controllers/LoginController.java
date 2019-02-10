package sample.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import sample.Main;
import sample.model.UserAccount;
import sample.model.XMPPSession;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;

public class LoginController implements Initializable, ControlledScreen {

	ScreensController screensController;
	public XMPPSession xmppSession = new XMPPSession();

	@FXML
	private Label info;
	@FXML
	private TextField loginTextField;

	@FXML
	private PasswordField passwordTextField;

	@FXML
	private AnchorPane anchorPane;

	@FXML
	void logInButtonClick(ActionEvent event) throws IOException, NoSuchAlgorithmException, SmackException.NotConnectedException, InterruptedException {
		loginUser();
	}

	private void loginUser() throws IOException, NoSuchAlgorithmException, SmackException.NotConnectedException, InterruptedException {
		if (loginTextField.getText().equals("") || passwordTextField.getText().equals("")) {
			info.setText("Pola nie mogą być puste!");
			return;
		}
		xmppSession.userAccount = new UserAccount(loginTextField.getText(), passwordTextField.getText());
		System.out.println("login: " + xmppSession.userAccount.getLogin());
		System.out.println("haslo: " + xmppSession.userAccount.getPassword());

		xmppSession.config = XMPPTCPConnectionConfiguration.builder()
				.setUsernameAndPassword(xmppSession.userAccount.getLogin(), xmppSession.userAccount.getPassword())
				.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
				.setCustomSSLContext(SSLContext.getDefault())
				.setXmppDomain(xmppSession.xmppDomain)
				.setHost(xmppSession.serverAddress)
				.setPort(xmppSession.port)
				.build();
		xmppSession.connection = new XMPPTCPConnection(xmppSession.config);
		try {
			xmppSession.connection.connect();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("nie udalo sie połączyć z serwerem");
			info.setText("Nie udalo sie połączyć z serwerem");
			return;
		}
		try {
			if (xmppSession.connection != null && xmppSession.connection.isConnected()) {
				xmppSession.connection.login(xmppSession.userAccount.getLogin(), xmppSession.userAccount.getPassword());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("nie udalo sie zalogować 2");
			info.setText("Niepoprawny login lub hasło");
			return;
		}
		System.out.println(Main.screen2ID);
		screensController.setScreen(Main.screen2ID);
		for (ControlledScreen controlledScreen : Main.listOfControllers) {
			if (controlledScreen instanceof LoggedInController) {
				((LoggedInController) controlledScreen).prepareControllerForDisplay();
			}
		}
		System.out.println("Zalogowano!");
		info.setText("");
		loginTextField.setText("");
		passwordTextField.setText("");
	}

	public void setScreenParent(ScreensController screenParent) {
		screensController = screenParent;
	}

	public void initialize(URL location, ResourceBundle resources) {
		anchorPane.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				System.out.println("kliknięto enter");
				try {
					loginUser();
				} catch (NoSuchAlgorithmException | IOException | SmackException.NotConnectedException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

	}
}
