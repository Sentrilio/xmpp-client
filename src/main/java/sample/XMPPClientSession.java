package sample;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

public class XMPPClientSession {

	protected static String serverAddress = "127.0.0.1";
	protected static int port = 5222;
	protected static String xmppDomain = "openfire-server";
	protected static XMPPTCPConnectionConfiguration config;
	protected static XMPPTCPConnection connection;
	protected static ChatManager chatManager;
	protected static MessageListener messageListener;


}
