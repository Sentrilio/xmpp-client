package sample.controllers;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import sample.Main;
import sample.XMPPClientSession;

import java.io.IOException;
import java.net.URL;
import java.util.*;

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
			if (r != refreshButton) {
				r.setVisible(false);
			}
		}
		anchorPane.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				System.out.println("kliknięto enter");
				try {
					if (!refreshButton.isVisible()) {
						System.out.println("Proba wysłania wiadomości");
						sendMessage();
					}
				} catch (XmppStringprepException | SmackException.NotConnectedException | InterruptedException e) {
					e.printStackTrace();
				}
			}

		});
		refreshButton.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				if (refreshButton.isVisible()) {
					System.out.println("connected to the server");
					try {
						setController();
					} catch (IOException e) {
						e.printStackTrace();
					}
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
	void refreshButtonClick(ActionEvent event) throws IOException {
		setController();
	}

	private void setController() throws IOException {
		for (Region r : controls) {
			if (r == refreshButton) {
				r.setVisible(false);
			} else {
				r.setVisible(true);
			}
		}
		chatManager.addIncomingListener(new IncomingChatMessageListener() {
			public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
				String name = getNameFromJid(entityBareJid);
				conversationField.appendText(name + ": " + message.getBody() + "\n");

			}
		});
		System.out.println("started listening incoming messages");

		System.out.println("printing rosters:");
		Roster roster = Roster.getInstanceFor(connection);
		Collection<RosterEntry> entries = roster.getEntries();
		for (RosterEntry entry : entries) {
			System.out.println(entry);
		}
		//////////////////////
		System.out.println("printing temp");
		for (ControlledScreen controlledScreen : Main.listOfControllers) {
			if (controlledScreen instanceof LoginController) {
				System.out.println(((LoginController) controlledScreen).temp);
			}
		}

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
