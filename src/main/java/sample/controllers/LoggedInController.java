package sample.controllers;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jxmpp.jid.BareJid;
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
	public XMPPSession xmppSession;
	private String nameFromSelectedButton = "";

	@FXML
	private TextField nameFriendField;

	@FXML
	private TextField statusTextField;

	@FXML
	private ComboBox<String> presenceComboBox;

	@FXML
	private VBox VBoxConversation;

	@FXML
	private TextField sendTextField;

	@FXML
	private AnchorPane anchorPane;

	@FXML
	private VBox VBoxFriendList;

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
		refreshFriendAndConversationListSafely();
	}

	private void refreshFriendAndConversationListSafely() {
		try {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					refreshFriendLists();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	void addFriendButtonClick(ActionEvent event) throws XmppStringprepException, SmackException.NotLoggedInException, XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {
		String jidString = nameFriendField.getText();
		if (!jidString.equals("")) {
			jidString += "@" + xmppSession.xmppDomain;
			EntityBareJid jid = JidCreate.entityBareFrom(jidString);
			xmppSession.roster.createEntry(jid, nameFriendField.getText(), new String[0]);
			System.out.println("Utworzono entry: " + jid);
		} else {
			System.out.println("Wprowadź imie kumpla");
		}
		refreshFriendAndConversationListSafely();
	}

	@FXML
	void setStatusButtonClick(ActionEvent event) throws SmackException.NotConnectedException, InterruptedException {
		if (!statusTextField.getText().equals("")) {
			xmppSession.presence.setStatus(statusTextField.getText());
		}
		xmppSession.connection.sendStanza(xmppSession.presence);
	}

	@FXML
	public void initialize(URL location, ResourceBundle resources) {
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
	void logoutButtonClick(ActionEvent event) throws SmackException.NotConnectedException, InterruptedException {
		System.out.println("Logout button clicked");
		screensController.setScreen(Main.screen1ID);
		cleanControllerAndRemoveSession();
	}

	@FXML
	void sendButtonClick(ActionEvent event) throws XmppStringprepException, SmackException.NotConnectedException, InterruptedException {
		sendMessage();
	}

	private void sendMessage() throws XmppStringprepException, SmackException.NotConnectedException, InterruptedException {
		String message = sendTextField.getText();
		sendTextField.clear();
		mapOfConversations.get(nameFromSelectedButton).appendText("me: " + message + "\n");
		String jidString = getNameFromStringOnButton(nameFromSelectedButton);
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


	public void prepareControllerForDisplay() throws IOException {
		setPresenceComboBox();
		setXmppSessionFromPreviousController();
		xmppSession.connection.disconnect();
		xmppSession.roster = Roster.getInstanceFor(xmppSession.connection);
		xmppSession.roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
		createConversationLists();
		refreshFriendLists();
		addRosterListener();
		loginUser();
		initChatManagerAndListener();
		xmppSession.presence = new Presence(Presence.Type.available);
		System.out.println("My presence:  " + xmppSession.presence.getType() + "  mode: " + xmppSession.presence.getMode() + " status:" + xmppSession.presence.getStatus());
		refreshFriendAndConversationListSafely();
	}

	private void initChatManagerAndListener() {
		xmppSession.chatManager = ChatManager.getInstanceFor(xmppSession.connection);
		xmppSession.chatManager.addIncomingListener(new IncomingChatMessageListener() {
			public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
				String name = getNameFromJid(entityBareJid);
				if (!mapOfConversations.containsKey(name)) {
					TextArea textArea = new TextArea();
					textArea.appendText(name + ": " + message.getBody() + "\n");
					mapOfConversations.put(name, textArea);
				} else {
					mapOfConversations.get(name).appendText(name + ": " + message.getBody() + "\n");
				}
				System.out.println("New message from: " + name + ": " + message.getBody() + "his presence: " +
						xmppSession.roster.getPresence(entityBareJid));
			}
		});
		System.out.println("started listening incoming messages");
	}

	private void loginUser() throws IOException {
		try {
			xmppSession.connection.connect();
			xmppSession.connection.login(xmppSession.userAccount.getLogin(), xmppSession.userAccount.getPassword());
		} catch (XMPPException | SmackException | InterruptedException e) {
			System.out.println("something went wrong with logging user on the server");
			e.printStackTrace();
		}
	}

	private void addRosterListener() {
		xmppSession.roster.addRosterListener(new RosterListener() {

			@Override
			public void entriesAdded(Collection<Jid> collection) {
				for (Jid jid : collection) {
					RosterEntry entry = xmppSession.roster.getEntry((BareJid) jid);
					//When the entry is only from the other user, then send a subscription request
					if (entry != null && entry.getType() == RosterPacket.ItemType.from) {
						try {
							System.out.println("Creating entry to: " + entry.getJid());
							xmppSession.roster.createEntry(entry.getJid(), entry.getName(), new String[0]);
						} catch (XMPPException | SmackException.NotLoggedInException |
								SmackException.NoResponseException |
								SmackException.NotConnectedException |
								InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				refreshFriendAndConversationListSafely();
				System.out.println("presence changed1");
			}

			@Override
			public void entriesUpdated(Collection<Jid> collection) {
				refreshFriendAndConversationListSafely();
				System.out.println("presence changed2");


			}

			@Override
			public void entriesDeleted(Collection<Jid> collection) {
				System.out.println("deleted");
				for (Jid jid : collection) {
					RosterEntry entry = xmppSession.roster.getEntry((BareJid) jid);
					//When the entry is only from the other user, then send a subscription request
					if (entry != null && entry.getType() == RosterPacket.ItemType.remove) {
						try {
							System.out.println("Removing entry : " + entry.getJid());
							xmppSession.roster.removeEntry(entry);
						} catch (XMPPException | SmackException.NotLoggedInException |
								SmackException.NoResponseException |
								SmackException.NotConnectedException |
								InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				refreshFriendAndConversationListSafely();
				System.out.println("presence changed3");
			}

			public void presenceChanged(Presence presence) {
				//sie wywołuje też po dodaniu entry (widocznie aktualizuje się jego presence state)
				System.out.println("presence from: " + presence.getFrom() + " mode: " + presence.getMode() + " type: " + presence.getType() + " status: " + presence.getStatus());
				refreshFriendAndConversationListSafely();
				System.out.println("presence changed4");
			}
		});
	}

	private void createConversationLists() {
//		VBoxConversation.getChildren().clear();
		Collection<RosterEntry> entries = xmppSession.roster.getEntries();
		for (RosterEntry entry : entries) {
			String name = getNameFromJid((EntityBareJid) entry.getJid());
			TextArea textArea = new TextArea();
			mapOfConversations.put(name, textArea);
		}
	}

	private void refreshFriendLists() {
		VBoxFriendList.getChildren().clear();
		if (xmppSession.roster != null) {
			Collection<RosterEntry> entries = xmppSession.roster.getEntries();
			for (RosterEntry entry : entries) {
				Presence presence = xmppSession.roster.getPresence(entry.getJid());
				String name = getNameFromJid((EntityBareJid) entry.getJid());
				String mode = presence.getMode().toString();
				String status = presence.getStatus();
				Button button = new Button();
				if (status == null) {
					mapOfFriends.put(name, "(" + mode + ")");
				} else {
					mapOfFriends.put(name, "(" + mode + ") " + status);
				}
				setButtonOnAction(button);
				button.setText(name + mapOfFriends.get(name));
				VBoxFriendList.getChildren().add(button);
			}
		}
	}

	private void setButtonOnAction(Button button) {
		button.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				nameFromSelectedButton = getNameFromStringOnButton(button.getText());
				TextArea conversation = mapOfConversations.get(nameFromSelectedButton);
				if (VBoxConversation.getChildren().isEmpty()) {
					VBoxConversation.getChildren().add(conversation);
				} else {
					VBoxConversation.getChildren().remove(0);
					VBoxConversation.getChildren().add(conversation);
				}
				conversation.prefWidthProperty().bind(VBoxConversation.widthProperty());
				conversation.prefHeightProperty().bind(VBoxConversation.heightProperty());
				System.out.println("clicked: " + nameFromSelectedButton);
			}
		});
	}

	private void setXmppSessionFromPreviousController() {
		for (ControlledScreen controlledScreen : Main.listOfControllers) {
			if (controlledScreen instanceof LoginController) {
				this.xmppSession = ((LoginController) controlledScreen).xmppSession;
			}
		}
	}

	private void setPresenceComboBox() {
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
		if (!presenceComboBox.getItems().isEmpty()) {
			xmppSession.presence.setMode(Presence.Mode.fromString(presenceComboBox.getValue()));
			xmppSession.presence.setPriority(50);
			System.out.println("type: " + xmppSession.presence.getType());
			System.out.println("status: " + xmppSession.presence.getStatus());
			System.out.println("priority: " + xmppSession.presence.getPriority());
			System.out.println("mode: " + xmppSession.presence.getMode());
			xmppSession.connection.sendStanza(xmppSession.presence);
			System.out.println("Presence changed!");
			System.out.println(presenceComboBox.getValue());
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

	public String getNameFromStringOnButton(String nameWithStatus) {
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

	public void cleanControllerAndRemoveSession() throws SmackException.NotConnectedException {
		this.nameFromSelectedButton = "";
		this.nameFriendField.clear();
		this.statusTextField.clear();
		this.presenceComboBox.getItems().clear();
		this.VBoxConversation.getChildren().clear();
		this.VBoxFriendList.getChildren().clear();
		this.sendTextField.clear();

		this.xmppSession.presence.setMode(Presence.Mode.away);
		this.xmppSession.presence.setType(Presence.Type.unavailable);
		this.xmppSession.connection.disconnect(xmppSession.presence);
		this.xmppSession.chatManager = null;
		this.xmppSession.roster = null;
		this.xmppSession.presence = null;
		this.xmppSession.config = null;
		this.xmppSession.connection = null;
		this.xmppSession.userAccount = null;

	}

}
