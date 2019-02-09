package sample.controllers;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import sample.Main;
import sample.XMPPClientSession;

import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class LoggedInController extends XMPPClientSession implements Initializable, ControlledScreen {

	ScreensController screensController;

	@FXML
	private Button refreshButton;

	@FXML
	private Button logoutButton;

	@FXML
	private Button sendButton;
	@FXML
	private VBox VBox;

	@FXML
	private TextField destName;

	@FXML
	public TextArea conversationField;

	@FXML
	private TextField sendTextField;

	@FXML
	private AnchorPane anchorPane;


	private List<Region> controls = new ArrayList<>();

	@FXML
	public void initialize(URL location, ResourceBundle resources) {
		controls.add(refreshButton);
		controls.add(logoutButton);
		controls.add(sendButton);
		controls.add(sendButton);
		controls.add(VBox);
		controls.add(destName);
		controls.add(conversationField);
		controls.add(sendTextField);
		for (Region r : controls) {
			if(r != refreshButton){
				r.setVisible(false);
			}
		}
		anchorPane.setOnKeyPressed(event -> {
			if(event.getCode() == KeyCode.ENTER){
				System.out.println("kliknięto enter");
				try {
					sendMessage();
				} catch (XmppStringprepException | SmackException.NotConnectedException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

	}


	@FXML
	void logoutButtonClick(ActionEvent event) {
		System.out.println("Logout button clicked");
		disconnect();
	}

	@FXML
	void sendButtonClick(ActionEvent event) throws XmppStringprepException, SmackException.NotConnectedException, InterruptedException {
		sendMessage();
	}

	private void sendMessage() throws XmppStringprepException, SmackException.NotConnectedException, InterruptedException {
		String message = sendTextField.getText();
		sendTextField.clear();
		conversationField.appendText("me: " + message + "\n");
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


	public void setScreenParent(ScreensController screenParent) {
		screensController = screenParent;
	}

	@FXML
	void refreshButtonClick(ActionEvent event) {
		setControlVariables();
	}

	private void setControlVariables() {
		for (Region r : controls) {
			if(r == refreshButton){
				r.setVisible(false);
			}else{
				r.setVisible(true);
			}
		}
		chatManager.addIncomingListener(new IncomingChatMessageListener() {
			public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
				String name = getNameFromJid(entityBareJid);
				conversationField.appendText(name + ": " + message.getBody() + "\n");// dodać do konkretnej historii

			}
		});
		System.out.println("started listening incoming messages");
	}


	private String getNameFromJid(EntityBareJid entityBareJid) {
		String name = "";
		for (char c : entityBareJid.toString().toCharArray()) {
			if (c != '@') {
				name += c;
			} else {
				break;
			}
		}
		return name;
	}

}
