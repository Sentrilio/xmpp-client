package sample.controllers;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import sample.Main;
import sample.model.XMPPSession;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class LoggedInController implements Initializable, ControlledScreen {

	ScreensController screensController;
	XMPPSession xmppSession;

	@FXML
	private ComboBox<String> presenceComboBox;

//	@FXML
//	private Button refreshButton;

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

	@FXML
	private VBox VBoxFriendList;


	private List<Region> controls = new ArrayList<>();

	@FXML
	public void initialize(URL location, ResourceBundle resources) {
//		controls.add(refreshButton);
		controls.add(logoutButton);
		controls.add(sendButton);
		controls.add(sendButton);
		controls.add(VBox);
		controls.add(destName);
		controls.add(conversationField);
		controls.add(sendTextField);
//		controls.add(tableColumn);
//		for (Region r : controls) {
//			if (r != refreshButton) {
//				r.setVisible(false);
//			}
//		}
		anchorPane.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				System.out.println("kliknięto enter");
				try {
//					if (!refreshButton.isVisible()) {
						System.out.println("Proba wysłania wiadomości");
						sendMessage();
//					}
				} catch (XmppStringprepException | SmackException.NotConnectedException | InterruptedException e) {
					e.printStackTrace();
				}
			}

		});
//		refreshButton.setOnKeyReleased(event -> {
//			if (event.getCode() == KeyCode.ENTER) {
//				if (refreshButton.isVisible()) {
//					System.out.println("connected to the server");
//					try {
//						setController();
//					} catch (IOException | SmackException.NotConnectedException | InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		});

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
		jidString += "@" + xmppSession.xmppDomain;
		EntityBareJid jid = JidCreate.entityBareFrom(jidString);
		Chat chat = xmppSession.chatManager.chatWith(jid);
		chat.send(message);
	}

	public void disconnect() {
		try {
			xmppSession.connection.disconnect();
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

//	@FXML
//	void refreshButtonClick(ActionEvent event) throws IOException, SmackException.NotConnectedException, InterruptedException {
//		setController();
//	}

	public void setController() throws IOException, SmackException.NotConnectedException, InterruptedException {

		for (ControlledScreen controlledScreen : Main.listOfControllers) {
			if (controlledScreen instanceof LoginController) {
				this.xmppSession = ((LoginController) controlledScreen).xmppSession;
			}
		}
		System.out.println(xmppSession.userAccount.getLogin());
		System.out.println(xmppSession.userAccount.getPassword());
		// wylogowanie
		xmppSession.connection.disconnect();
		//dodanie listenera:
		xmppSession.roster = Roster.getInstanceFor(xmppSession.connection);
		xmppSession.roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
		xmppSession.roster.addRosterListener(new RosterListener() {

			@Override
			public void entriesAdded(Collection<Jid> collection) {
			}

			@Override
			public void entriesUpdated(Collection<Jid> collection) {
			}

			@Override
			public void entriesDeleted(Collection<Jid> collection) {
			}

			public void presenceChanged(Presence presence) {
				System.out.println("Presence changed: " + presence.getFrom() + " " + presence);
			}
		});

		try {
			xmppSession.connection.connect();
			xmppSession.connection.login(xmppSession.userAccount.getLogin(), xmppSession.userAccount.getPassword());
		} catch (XMPPException | SmackException | InterruptedException e) {
			System.out.println("something went wrong with logging user on the server");
			e.printStackTrace();
		}

		for (Region r : controls) {
//			if (r == refreshButton) {
//				r.setVisible(false);
//			} else {
				r.setVisible(true);
//			}
		}
		Presence p = new Presence(Presence.Type.available, "I'm not busy", 42, Presence.Mode.dnd);
		xmppSession.connection.sendStanza(p);

		xmppSession.chatManager = ChatManager.getInstanceFor(xmppSession.connection);
		xmppSession.chatManager.addIncomingListener(new IncomingChatMessageListener() {
			public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
				String name = getNameFromJid(entityBareJid);
				conversationField.appendText(name + ": " + message.getBody() + "\n");
				System.out.println("New message from: " + name + ": " + message.getBody() + "\n");
				try {
					xmppSession.roster.createEntry(entityBareJid, "login", new String[0]);
				} catch (SmackException.NotLoggedInException |
						SmackException.NoResponseException |
						SmackException.NotConnectedException |
						XMPPException.XMPPErrorException |
						InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("his presence: " + xmppSession.roster.getPresence(entityBareJid));

			}
		});
		System.out.println("started listening incoming messages");
		System.out.println("Roster entries: ");
		xmppSession.roster = Roster.getInstanceFor(xmppSession.connection);
		Collection<RosterEntry> entries = xmppSession.roster.getEntries();
		for (RosterEntry entry : entries) {
			Button button = new Button();
			Presence s = xmppSession.roster.getPresence(entry.getJid());
			button.setText(getNameFromJid((EntityBareJid) entry.getJid()) + ": " + s.getStatus());
			VBoxFriendList.getChildren().add(button);
			System.out.println("entry: " + entry);
		}

		for(int i=0;i<20;i++){
			Button button = new Button();
			button.setText("test " +i);

			VBoxFriendList.getChildren().add(button);
		}
		presenceComboBox.getItems().addAll(
				"available",
				"chat",
				"away",
				"xa",
				"dnd"
		);

	}

	@FXML
	void presenceChanged(ActionEvent event) throws SmackException.NotConnectedException, InterruptedException {
		System.out.println("Presence changed!");
		System.out.println(presenceComboBox.getValue());
		Presence p = new Presence(Presence.Type.available, "I'm not busy", 42, Presence.Mode.valueOf(presenceComboBox.getValue()));
		xmppSession.connection.sendStanza(p);
//		presenceMenu.setText();
//		presenceMenu.setText(presenceMenu.getUserData().toString());
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


//	@FXML
//	void presenceChanged(ActionEvent event) {
//		Presence p = new Presence(presenceMenu.ge, "I'm not busy", 42, Presence.Mode.dnd);
//		xmppSession.connection.sendStanza(p);
//		presenceMenu.setText();
//		presenceMenu.setText(presenceMenu.getUserData().toString());
//	}
}
