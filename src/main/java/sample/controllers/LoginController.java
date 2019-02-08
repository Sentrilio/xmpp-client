package sample.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import sample.Main;
import sample.XMPPClientSession;
import sample.model.UserAccount;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;

public class LoginController extends XMPPClientSession implements Initializable, ControlledScreen {

	ScreensController screensController;

	@FXML
	private Label info;
	@FXML
	private TextField loginTextField;

	@FXML
	private PasswordField passwordTextField;

	@FXML
	void logInButtonClick(ActionEvent event) throws IOException, NoSuchAlgorithmException {

		String login = loginTextField.getText();
		String password = passwordTextField.getText();

		if (login.equals("") || password.equals("")) {
//			screensController.setScreen(Main.screen1ID);
			info.setText("Pola nie mogą być puste!");
			return;
		}
		UserAccount userAccount = new UserAccount(login, password);
		System.out.println("login: " + userAccount.getLoginUser());
		System.out.println("haslo: " + userAccount.getPasswordUser());

		config = XMPPTCPConnectionConfiguration.builder()
				.setUsernameAndPassword("username", "password")
				.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
				.setCustomSSLContext(SSLContext.getDefault())
				.setXmppDomain(xmppDomain)
				.setHost(serverAddress)
				.setPort(port)
				.build();
		connection = new XMPPTCPConnection(config);
		try {
			connection.connect();
			chatManager = ChatManager.getInstanceFor(connection);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("nie udalo sie połączyć z serwerem");
			info.setText("Nie udalo sie połączyć z serwerem");
			return;
		}
		try {
			if (connection != null && connection.isConnected()) {
				connection.login(userAccount.getLoginUser(), userAccount.getPasswordUser());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("nie udalo sie zalogować 2");
			info.setText("Niepoprawny login lub hasło");
			return;
		}
		chatManager = ChatManager.getInstanceFor(connection);
		screensController.setScreen(Main.screen2ID);
		chatManager.addIncomingListener(new IncomingChatMessageListener() {
			public void newIncomingMessage(EntityBareJid entityBareJid, org.jivesoftware.smack.packet.Message message, Chat chat) {
				System.out.println("New message from " + entityBareJid + ": " + message.getBody());
			}
		});
		System.out.println("Zalogowano!");
		info.setText("");
		loginTextField.setText("");
		passwordTextField.setText("");
	}

	public void setScreenParent(ScreensController screenParent) {
		screensController = screenParent;
	}

	public void initialize(URL location, ResourceBundle resources) {

	}
}
