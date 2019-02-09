package sample.model;

import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

public class XMPPSession {
	public final String serverAddress = "127.0.0.1";
	public final int port = 5222;
	public final String xmppDomain = "openfire-server";
	public XMPPTCPConnectionConfiguration config;
	public XMPPTCPConnection connection;
	public ChatManager chatManager;
	public String login;
	public String password;
	public Roster roster;
}
