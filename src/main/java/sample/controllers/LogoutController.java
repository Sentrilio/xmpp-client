package sample.controllers;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import sample.Main;
import sample.XMPPClientSession;

import java.net.URL;
import java.util.ResourceBundle;

public class LogoutController extends XMPPClientSession implements Initializable, ControlledScreen {

	ScreensController screensController;

	@FXML
	private Button logoutButton;

	@FXML
	private Button sendButton;
	@FXML
	private VBox VBox;

	@FXML
	private TextField destName;

	@FXML
	private TextArea conversationField;

	@FXML
	private TextField sendTextField;

	@FXML
	void logoutButtonClick(ActionEvent event) {
		System.out.println("Logout button clicked");
		disconnect();
	}

	@FXML
	void sendButtonClick(ActionEvent event) throws XmppStringprepException, SmackException.NotConnectedException, InterruptedException {
		String message = sendTextField.getText();
		sendTextField.clear();
		conversationField.appendText(message+ "\n");
		/////////////////////////////////////////////

		String jidString = destName.getText();
		jidString += "@" + xmppDomain;

		EntityBareJid jid = JidCreate.entityBareFrom(jidString);
		Chat chat = chatManager.chatWith(jid);
		chat.send(message);
	}

	public void disconnect() {
		try {
			connection.disconnect();
			System.out.println("disconnected");
			screensController.setScreen(Main.screen1ID);
		} catch (Exception e) {
			System.out.println("something went wrong with disconnecting");
			e.printStackTrace();
		}
	}

//	public void addListener(){
//		chatManager.addIncomingListener(new IncomingChatMessageListener() {
//			public void newIncomingMessage(EntityBareJid entityBareJid, org.jivesoftware.smack.packet.Message message, Chat chat) {
//				System.out.println("New message from " + entityBareJid + ": " + message.getBody());
//			}
//		});
//	}

	@FXML
	public void initialize(URL location, ResourceBundle resources) {
		System.out.println("before");
//		addListener();
		System.out.println("after");
	}

	public void setScreenParent(ScreensController screenParent) {
		screensController = screenParent;
	}


}
