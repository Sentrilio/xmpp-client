package sample.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import sample.model.UserAccount;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class LoginController {

	private String serverAddress = "127.0.0.1";
	private int port = 5222;
	private String xmppDomain = "openfire-server";
	private XMPPTCPConnectionConfiguration config;
	private XMPPTCPConnection connection;
	private ChatManager chatManager;
	private MessageListener messageListener;
	///////////////////
	@FXML
	private TextField loginTextField;

	@FXML
	private TextField passwordTextField;

	@FXML
	void logInButtonClick(ActionEvent event) throws IOException, NoSuchAlgorithmException {
		String tempMessage;
		String login =loginTextField.getText();
		String password = passwordTextField.getText();

		if (!(login.equals("") || password.equals(""))){
			System.out.println("niepusty");
		}
		UserAccount userAccount = new UserAccount(login, password);
		System.out.println("login: " + userAccount.getLoginUser());
		System.out.println("haslo: " + userAccount.getPasswordUser());

		this.config = XMPPTCPConnectionConfiguration.builder()
				.setUsernameAndPassword("username", "password")
				.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
				.setCustomSSLContext(SSLContext.getDefault())
				.setXmppDomain(xmppDomain)
				.setHost(serverAddress)
				.setPort(port)
				.build();
		this.connection = new XMPPTCPConnection(this.config);
		try {
			this.connection.connect();
			this.chatManager = ChatManager.getInstanceFor(connection);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("nie udalo sie zalogować");
		}
		try {
			if (connection != null && connection.isConnected()) {
				this.connection.login(userAccount.getLoginUser(), userAccount.getPasswordUser());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("nie udalo sie zalogować");

		}
		this.chatManager = ChatManager.getInstanceFor(connection);
		this.chatManager.addIncomingListener(new IncomingChatMessageListener() {
			public void newIncomingMessage(EntityBareJid entityBareJid, org.jivesoftware.smack.packet.Message message, Chat chat) {
				System.out.println("New message from " + entityBareJid + ": " + message.getBody());
			}
		});
		System.out.println("Zalogowano!");
	}

}
