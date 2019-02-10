package sample.controllers;


import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
	private Button removeFriendButton;
	@FXML
	private TextField nameFriendField;

	@FXML
	private Button addFriendButton;
	@FXML
	private TextField statusTextField;

	@FXML
	private ComboBox<String> presenceComboBox;

	@FXML
	private Button logoutButton;

	@FXML
	private Button sendButton;
	@FXML
	private VBox VBoxConversation;


	private String nameFromSelectedButton = "";
	private String status = "";

	@FXML
	private TextField sendTextField;

	@FXML
	private AnchorPane anchorPane;

	@FXML
	private VBox VBoxFriendList;

	private List<Region> controls = new ArrayList<>();

	private HashMap<String, TextArea> mapOfConversations = new HashMap<>();
	private HashMap<String, String> mapOfFriends = new HashMap<>();


	void removeEntry() {
		System.out.println("name from button: " + nameFromSelectedButton);
		Collection<RosterEntry> entries = xmppSession.roster.getEntries();
		for (RosterEntry entry : entries) {
			if (entry.getName().equals(nameFromSelectedButton)) {
				try {
					System.out.println("Removing entry: " + entry.getName());
					xmppSession.roster.removeEntry(entry);
				} catch (SmackException.NotLoggedInException |
						SmackException.NoResponseException |
						SmackException.NotConnectedException |
						XMPPException.XMPPErrorException |
						InterruptedException e) {
					e.printStackTrace();
				}
			}


		}
	}

	@FXML
	void removeFriendButtonClick(ActionEvent event) {
		removeEntry();
		clearFriendList();
		fillFriendList();
	}

	@FXML
	void addFriendButtonClick(ActionEvent event) throws XmppStringprepException, SmackException.NotLoggedInException, XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {
		String jidString = nameFriendField.getText();
		if (!jidString.equals("")) {
			jidString += "@" + xmppSession.xmppDomain;
			EntityBareJid jid = JidCreate.entityBareFrom(jidString);
			xmppSession.roster.createEntry(jid, nameFriendField.getText(), new String[0]);
			System.out.println("Utworzono entry");
		} else {
			System.out.println("Wprowadź nazwę kumpla");
		}
		clearFriendList();
		fillFriendList();
	}

	@FXML
	void setStatusButtonClick(ActionEvent event) throws SmackException.NotConnectedException, InterruptedException {
		if (!statusTextField.getText().equals("")) {
			status = statusTextField.getText();
			xmppSession.presence.setStatus(status);
		}
		xmppSession.connection.sendStanza(xmppSession.presence);
	}

	@FXML
	public void initialize(URL location, ResourceBundle resources) {
		controls.add(logoutButton);
		controls.add(sendButton);
		controls.add(sendButton);
		controls.add(VBoxConversation);
		controls.add(sendTextField);
//		for (Region r : controls) {}

		anchorPane.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				System.out.println("kliknięto enter");
				try {
					System.out.println("Proba wysłania wiadomości");
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
		mapOfConversations.get(nameFromSelectedButton).appendText("me: " + message + "\n");
//		conversationField.appendText("me: " + message + "\n");
		String jidString = getNameFromFriendList(nameFromSelectedButton);
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
				System.out.println("Presence changed: " + getNameFromPresenceString(presence.getFrom().toString()));

				for (Node nodeIn : VBoxFriendList.getChildren()) {
					String nameFromButton = getNameFromFriendList(((Button) nodeIn).getText());
					String nameFromChangedPresence = getNameFromPresenceString(presence.getFrom().toString());
					if (nameFromButton.equals(nameFromChangedPresence))
						try {
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									// Update UI here.
									if (presence.getStatus() == null) {
										((Button) nodeIn).setText(nameFromButton + "(" + presence.getMode() + ")");
									} else {
										((Button) nodeIn).setText(nameFromButton + "(" + presence.getMode() + ") " + presence.getStatus());
									}

								}
							});
						} catch (Exception e) {
							e.printStackTrace();
						}
				}
			}
		});

		try {
			xmppSession.connection.connect();
			xmppSession.connection.login(xmppSession.userAccount.getLogin(), xmppSession.userAccount.getPassword());
		} catch (XMPPException | SmackException | InterruptedException e) {
			System.out.println("something went wrong with logging user on the server");
			e.printStackTrace();
		}

		xmppSession.chatManager = ChatManager.getInstanceFor(xmppSession.connection);
		xmppSession.chatManager.addIncomingListener(new IncomingChatMessageListener() {
			public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
				String name = getNameFromJid(entityBareJid);
				mapOfConversations.get(name).appendText(name + ": " + message.getBody() + "\n");
				System.out.println("New message from: " + name + ": " + message.getBody() + "\n");
				System.out.println("his presence: " + xmppSession.roster.getPresence(entityBareJid));
			}
		});
		System.out.println("started listening incoming messages");
		System.out.println("Roster entries: ");
		xmppSession.presence = new Presence(Presence.Type.available);
		xmppSession.roster = Roster.getInstanceFor(xmppSession.connection);
		clearFriendList();
		fillFriendList();
		presenceComboBox.getItems().addAll(
				"available",
				"chat",
				"away",
				"xa",
				"dnd"
		);

	}

	private void clearFriendList() {
		VBoxFriendList.getChildren().clear();
	}

	private void fillFriendList() {
		Collection<RosterEntry> entries = xmppSession.roster.getEntries();
		for (RosterEntry entry : entries) {
			Button button = new Button();
			button.setOnAction(new EventHandler<ActionEvent>() {
				public void handle(ActionEvent e) {
					nameFromSelectedButton = getNameFromFriendList(button.getText());
					TextArea conversation = mapOfConversations.get(nameFromSelectedButton);
					if (VBoxConversation.getChildren().isEmpty()) {
						VBoxConversation.getChildren().add(conversation);
					} else {
						VBoxConversation.getChildren().remove(0);
						VBoxConversation.getChildren().add(conversation);
					}
					conversation.prefWidthProperty().bind(VBoxConversation.widthProperty());
					conversation.prefHeightProperty().bind(VBoxConversation.heightProperty());
					System.out.println("clicked" + nameFromSelectedButton);
				}
			});
			Presence s = xmppSession.roster.getPresence(entry.getJid());
			String name = getNameFromJid((EntityBareJid) entry.getJid());
			String mode = s.getMode().toString();
			String status = s.getStatus();

			if (s.getStatus() == null) {
				button.setText(name + "(" + mode + ")");
			} else {
				button.setText(name + "(" + mode + ") " + status);
			}
			TextArea textArea = new TextArea();
			mapOfConversations.put(name, textArea);
			VBoxFriendList.getChildren().add(button);
		}
	}



	@FXML
	void presenceChanged(ActionEvent event) throws SmackException.NotConnectedException, InterruptedException {
		System.out.println("Presence changed!");
		System.out.println(presenceComboBox.getValue());
		xmppSession.presence.setMode(Presence.Mode.fromString(presenceComboBox.getValue()));
		xmppSession.presence.setPriority(50);
		xmppSession.presence.setStatus(status);
		System.out.println("type: " + xmppSession.presence.getType());
		System.out.println("status: " + xmppSession.presence.getStatus());
		System.out.println("priority: " + xmppSession.presence.getPriority());
		System.out.println("mode: " + xmppSession.presence.getMode());
		xmppSession.connection.sendStanza(xmppSession.presence);

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

	public String getNameFromPresenceString(String FromPresenceString) {
		String name = "";
		for (char c : FromPresenceString.toCharArray()) {
			if (c != '@') {
				name += c;
			} else {
				break;
			}
		}
		return name;
	}

	public String getNameFromFriendList(String nameWithStatus) {
		String name = "";
		for (char c : nameWithStatus.toCharArray()) {
			if (c != '(') {
				name += c;
			} else {
				break;
			}
		}
		return name;
	}


}
