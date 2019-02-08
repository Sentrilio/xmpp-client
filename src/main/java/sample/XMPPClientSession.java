package sample;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;

import java.util.*;

public class XMPPClientSession {

	protected final String serverAddress = "127.0.0.1";
	protected final int port = 5222;
	protected final String xmppDomain = "openfire-server";
	protected static XMPPTCPConnectionConfiguration config;
	protected static XMPPTCPConnection connection;
	protected static ChatManager chatManager;
	protected static Queue<String> conversation= new PriorityQueue<String>();

}
