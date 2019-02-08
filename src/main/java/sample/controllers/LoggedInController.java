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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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

//	@FXML
//	public TextArea conversationField;

	@FXML
	private TextField sendTextField;

	@FXML
	private Button adminButton;

	@FXML
	private Button loginButton;

	HashMap<String, TextField> allConversations = new HashMap<>();

	@FXML
	void logoutButtonClick(ActionEvent event) {
		System.out.println("Logout button clicked");
		disconnect();
	}

	@FXML
	void sendButtonClick(ActionEvent event) throws XmppStringprepException, SmackException.NotConnectedException, InterruptedException {
		String message = sendTextField.getText();
//		sendTextField.clear();

//		conversationField.appendText("me: " + message + "\n");
		String jidString = destName.getText();
		for(Map.Entry<String, TextField> entry : allConversations.entrySet()) {
			String key = entry.getKey();
			TextField value = entry.getValue();
			if(key.equals(jidString)){
				value.appendText("me: " + message + "\n");
			}
		}
		jidString += "@" + xmppDomain;
		EntityBareJid jid = JidCreate.entityBareFrom(jidString);
		Chat chat = chatManager.chatWith(jid);
		chat.send(message);
//		refreshChat();
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
		allConversations.put("admin",new TextField());
		allConversations.put("login",new TextField());

		chatManager.addIncomingListener(new IncomingChatMessageListener() {
			public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
				String name = getNameFromJid(entityBareJid);
				TextField conversationWithUser = allConversations.get(name);
				conversationWithUser.appendText(name + ": " + message.getBody() + "\n");
				allConversations.replace(name,conversationWithUser);
//				conversationField.appendText(name + ": " + message.getBody() + "\n");// dodać do konkretnej historii
			}
		});
//		refreshChat();
//		refreshButton.setDisable(true);
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


	@FXML
	void adminConversationButtonClick(ActionEvent event) {
//		allConversations.get("admin").setVisible(true);
		for(Map.Entry<String, TextField> entry : allConversations.entrySet()) {
			String key = entry.getKey();
			TextField value = entry.getValue();
			if(key.equals("admin")){
				value.setVisible(true);
			}else{
				value.setVisible(false);
			}
		}
	}

	@FXML
	void loginConversationButtonClick(ActionEvent event) {
		for(Map.Entry<String, TextField> entry : allConversations.entrySet()) {
			String key = entry.getKey();
			TextField value = entry.getValue();
			if(key.equals("login")){
				value.setVisible(true);
			}else{
				value.setVisible(false);
			}
		}
	}
	@FXML
	public void initialize(URL location, ResourceBundle resources) {

//		Timeline fiveSecondsWonder = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
//
//			public void handle(ActionEvent event) {
//				System.out.println("this is called every 1 second on UI thread");
//				if(!XMPPClientSession.conversation.isEmpty()){
//					System.out.println(XMPPClientSession.conversation.remove());
//				}
//			}
//		}));
//		fiveSecondsWonder.setCycleCount(Timeline.INDEFINITE);
//		fiveSecondsWonder.play();

//		Task task = new Task<Void>() {
//			protected Void call() throws Exception {
//				updateMessage("cos sie zadziało");
////				updateMessage(XMPPClientSession.conversation.get(0));
//
////				if(XMPPClientSession.conversation.isEmpty()){
////					System.out.println("empty");
////				}else{
////					conversationField.appendText();
////					System.out.println("something happened");
////				}
//				return null;
//			}
//		};
//		conversationField.textProperty().bind(task.messageProperty());
//		new Thread(task).start();

//		Timer timer = new Timer();
//		timer.scheduleAtFixedRate(new TimerTask() {
//			@Override
//			public void run() {
//				System.out.print("I would be called every 1 seconds\n");
//				if(!XMPPClientSession.conversation.isEmpty()){
//					conversationField.appendText(XMPPClientSession.conversation.remove());
//				}
//			}
//		}, 0, 1000);


//		conversationField.textProperty().bind()
		//		final TimerTask timerTask = new TimerTask() {
//			@Override
//			public void run() {
////				System.out.println("hello world");
//				System.out.println(TimeUnit.SECONDS.toMillis(1));
////				if(XMPPClientSession.conversation.isEmpty())
//			}
//		};
//		conversationField.textProperty().bind(timerTask.messageProperty());
//
//		Timer timer = new Timer();
//		timer.schedule(timerTask,100, TimeUnit.SECONDS.toMillis(1));

//		Service service = new Service() {
//			@Override
//			protected Task createTask() {
//				System.out.println("elo");
//				return null;
//			}
//		};
//		service.start();
//		service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//			public void handle(WorkerStateEvent t) {
////				service.setPeriod(Duration.seconds(1 + Math.random()*100));
//			}
//		});


//		System.out.println("after");
	}
}
