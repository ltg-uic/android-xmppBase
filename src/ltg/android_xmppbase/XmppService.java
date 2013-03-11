package ltg.android_xmppbase;

import java.util.ArrayList;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.pubsub.PresenceState;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class XmppService extends IntentService {

	public static final String ERROR = "ERROR";
	public static final String DO_LOGIN = "DO_LOGIN";
	public static final String SEND_MESSAGE_CHAT = "SEND_MESSAGE";
	public static final String ACTIVITY_MESSAGER = "ACTIVITY_MESSAGER";
	public static final String CONNECT = "CONNECT";
	public static final String XMPP_MESSAGE = "XMPP_MESSAGE";
	private final IBinder xmppBinder = new XmppBinder();

	private static final String TAG = "XmppService";
	private static final String DOMAIN = "54.243.60.48";
	private static final String USERNAME = "android";
	private static final String PASSWORD = "android";
	public static final String CHAT_ACTION_RECEIVED_MESSAGE = "CHAT_ACTION";

	public static final String MESSAGE_TEXT_CHAT = "MESSAGE_TEXT";

	public static final String GROUP_CHAT = "GROUP_CHAT";
	public static final String SINGLE_CHAT = "SINGLE_CHAT";
	public static final String CHAT_TYPE = "CHAT_TYPE";
	public static final String GROUP_CHAT_NAME = "GROUP_CHAT_NAME";
	private static final String RECONNECT = "RECONNECT";
	public static final String SHOW_LOGIN = "SHOW_LOGIN";
	public static final String DISCONNECT = "DISCONNECT";
	public static final String STARTUP = "STARTUP";
	public static final String DESTORY = "DESTORY";

	private static volatile Looper serviceLooper;
	private static volatile ServiceHandler serviceHandler;
	private Messenger activityMessenger;
	private XMPPConnection xmppConnection;
	private long handlerThreadId;
	private MultiUserChat groupChat;
	private String chatType = null;
	private String groupChatRoom = null;

	//listeners
	private ConnectionCreationListener connectionCreationListener;
	private ConnectionListener connectionGeneralListener;
	private ArrayList<PacketListener> packetListeners = new ArrayList<PacketListener>();
	
	public XmppService() {
		super("XMPP SERVICE");
	}

	// Handler of incoming messages from clients.
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(android.os.Message msg) {
			onHandleIntent((Intent) msg.obj);
		}
	}

	Handler connectionHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

		};
	};
	

	@Override
	protected void onHandleIntent(Intent intent) {

		Log.i(TAG, "onHandleIntent");

		String action = intent.getAction();
		Bundle extras = intent.getExtras();
		if (action.equals(STARTUP)) {

			Object messExtra = extras.get(ACTIVITY_MESSAGER);
			
			if (messExtra != null) {
				activityMessenger = (Messenger) messExtra;
			}
			
			chatType = (String) extras.get(CHAT_TYPE);
			groupChatRoom = (String) extras.get(GROUP_CHAT_NAME);
			
			
		} else if (action.equals(SEND_MESSAGE_CHAT)) {
			Object extra = extras.get(MESSAGE_TEXT_CHAT);
			if (extra != null) {
				String text = (String) extra;

				Log.i("XMPPChatDemoActivity", "Sending text " + text + " to "
						+ "aperritano");
				Message msg = new Message("aperritano@"
						+ xmppConnection.getHost(), Message.Type.chat);
				msg.setBody(text);
				if (xmppConnection != null) {
					xmppConnection.sendPacket(msg);
				}
			}
		} else if (action.equals(DO_LOGIN)) {
			doLogin();
			
//			if( groupChatRoom != null)
//				doGroupChat(groupChatRoom);
			
		} else if (action.equals(CONNECT)) {
			doConnection();
		} else if (action.equals(RECONNECT)) {
			doConnection();
		} else if(action.equals(DISCONNECT)) {
			groupChat.leave();
			xmppConnection.disconnect();
			sendErrorToUI("Disconnecting....from connection and group chat");
		} else if( action.equals(GROUP_CHAT)) {
			doGroupChat(groupChatRoom);
			sendErrorToUI("Joined Group chat!!");
		} else if( action.equals(DESTORY)) {
			removeListeners();
			groupChat = null;
			xmppConnection = null;
		}

	}

	public void doConnection() {
		
		if (xmppConnection == null ) {

			ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(
					DOMAIN, 5222);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				connectionConfiguration.setTruststoreType("BKS");
				connectionConfiguration.setTruststorePassword(null);
				connectionConfiguration.setTruststorePath(null);
			}
			connectionConfiguration.setSASLAuthenticationEnabled(false);
			//SASLAuthentication.supportSASLMechanism("PLAIN", 0);
			Connection.DEBUG_ENABLED = true;
			xmppConnection = new XMPPConnection(connectionConfiguration);
			
			

			Connection.addConnectionCreationListener(createConnectionListener());
			
			try {
				xmppConnection.connect();
				xmppConnection.addConnectionListener(createGeneralConnectionListener());

			} catch (final XMPPException e) {
				sendErrorToUI("There was a problem connecting to " + DOMAIN);
				Log.e(TAG, "Could not connect to Xmpp server.", e);
			}
		} 
	}

	public ConnectionCreationListener createConnectionListener() {
		connectionCreationListener = new ConnectionCreationListener() {
			
			@Override
			public void connectionCreated(final Connection connection) {
				final Intent i = new Intent(DO_LOGIN);
				final android.os.Message newMessage = android.os.Message.obtain();
				newMessage.obj = i;
				sendToServiceHandler(i);
				sendErrorToUI("Connection Successful!!");
			}
		};
		
		return connectionCreationListener;
	}
	
	public ConnectionListener createGeneralConnectionListener() {
		connectionGeneralListener = new ConnectionListener() {
			
			@Override
			public void connectionClosed() {
				final Intent i = new Intent(DESTORY);
				final android.os.Message newMessage = android.os.Message.obtain();
				newMessage.obj = i;
				sendToServiceHandler(i);
				sendErrorToUI("Connection is being destroyed normally");
				
			}

			@Override
			public void connectionClosedOnError(Exception e) {
				final Intent i = new Intent(DESTORY);
				final android.os.Message newMessage = android.os.Message.obtain();
				newMessage.obj = i;
				sendToServiceHandler(i);
				sendErrorToUI("Connection is being destroyed do to an error");
				
			}

			@Override
			public void reconnectingIn(int seconds) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void reconnectionSuccessful() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void reconnectionFailed(Exception e) {
				// TODO Auto-generated method stub
				
			}
		};
		
		return connectionGeneralListener;
	}
	
	public void removeListeners() {
		Connection.removeConnectionCreationListener(connectionCreationListener);
		xmppConnection.removeConnectionListener(connectionGeneralListener);
		for (PacketListener packetListener : packetListeners) {
			groupChat.removeMessageListener(packetListener);
		}
	}
	public void doLogin() {
		
		

		if (xmppConnection != null) {
			
			SharedPreferences settings = getSharedPreferences(
					getString(R.string.xmpp_prefs), MODE_PRIVATE);
			String storedUserName = settings.getString(
					getString(R.string.user_name), null);
			String storedPassword = settings.getString(
					getString(R.string.password), null);

			String error = null;
			try {

				
			xmppConnection.login(storedUserName, storedPassword);

			final Intent i = new Intent(GROUP_CHAT);
			final android.os.Message newMessage = android.os.Message.obtain();
			newMessage.obj = i;
			sendToServiceHandler(i);
			} catch (XMPPException e) {
				Log.e(TAG, "Could not login into xmpp server.", e);
				showToast("There was a problem logging in with l: "
						+ storedUserName + " p: " + storedPassword);
			} catch(Exception e) {
				Log.e(TAG, "too many logins", e);
			
				showToast("Problem in DoLogin");
			}
		} 
	}

	@Override
	public void onCreate() {
		super.onCreate();
		HandlerThread thread = new HandlerThread("XMPP SERVICE MOTHA FUCKA");
		thread.start();
		handlerThreadId = thread.getId();
		serviceLooper = thread.getLooper();
		serviceHandler = new ServiceHandler(serviceLooper);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		sendToServiceHandler(startId, intent);

		return START_REDELIVER_INTENT;
	}

	public void doGroupChat(String chatroom) {
		if( xmppConnection != null) {
		if (xmppConnection.isAuthenticated() && chatroom != null) {
			// Initialize and join chatRoom
			if( groupChat == null)
				groupChat = new MultiUserChat(xmppConnection, chatroom);
			
			try {
				groupChat.join(xmppConnection.getUser());
			} catch (XMPPException e) {
				Log.e(TAG, "ERROR CONNECTING TO GROUP CHAT", e);
			}

			PacketListener pl = new PacketListener() {
				@Override
				public void processPacket(Packet packet) {
					Message message = (Message) packet;
					processMessage(message);
				}
			};
			
			packetListeners.add(pl);
			
			xmppConnection.addPacketListener(pl, new PacketTypeFilter(
					Message.class));

		}
		}
	}

	public void doSingleChat() {
		PacketListener pl = new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				Message message = (Message) packet;
				processMessage(message);
			}
		};

		packetListeners.add(pl);
		xmppConnection.addPacketListener(pl,
				new PacketTypeFilter(Message.class));

	}

	protected void processMessage(Message message) {
		if (message.getFrom() != null) {
			String fromName = StringUtils.parseBareAddress(message.getFrom());
			String currentUser = StringUtils.parseBareAddress(xmppConnection
					.getUser());
			if (currentUser.equals(fromName)) {
				return;
			}
		}
		if (message.getBody() != null) {
			String fromName = StringUtils.parseBareAddress(message.getFrom());
			Log.i(TAG, "Got text [" + message.getBody() + "] from [" + fromName
					+ "]");

			Intent i = new Intent(CHAT_ACTION_RECEIVED_MESSAGE);
			i.putExtra(XMPP_MESSAGE, fromName + " says: " + message.getBody());

			android.os.Message newMessage = android.os.Message.obtain();
			newMessage.obj = i;
			if (activityMessenger != null) {
				try {
					activityMessenger.send(newMessage);
				} catch (RemoteException e) {
					Log.e(TAG,
							"PACKET RECEIVED SENDING PROBLEM SENDING MESSAGE BACK TO ACTIVITY",
							e);
				}
			} else {
				throw new NullPointerException(
						"ACTIVITY MESSAGER WENT AWAY -> NULL");
			}
		} else {
			// Log.i(TAG, packet.toXML());
		}
	}

	protected void sendIntentToUI(Intent intent) {
		android.os.Message newMessage = android.os.Message.obtain();
		newMessage.obj = intent;
		try {
			activityMessenger.send(newMessage);
		} catch (RemoteException e) {
			Log.e(TAG, "Could not create message to UI to show login", e);
		}

	}
	
	protected void sendErrorToUI(String text) {
		Intent i = new Intent(ERROR);
		i.putExtra(XMPP_MESSAGE, text);

		android.os.Message newMessage = android.os.Message.obtain();
		newMessage.obj = i;
		try {
			activityMessenger.send(newMessage);
		} catch (RemoteException e) {
			Log.e(TAG, "Could not create message to UI to show login", e);
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		serviceLooper.quit();
		xmppConnection.disconnect();
	}

	public static boolean sendToServiceHandler(int i, Intent intent) {
		if (serviceHandler != null) {
			android.os.Message msg = serviceHandler.obtainMessage();
			msg.arg1 = i;
			msg.obj = intent;
			serviceHandler.sendMessage(msg);
			return true;
		} else {
			Log.i(TAG,
					"sendToServiceHandler() called with " + intent.getAction()
							+ " when service handler is null");
			return false;
		}
	}

	public static boolean sendToServiceHandler(Intent intent) {
		return sendToServiceHandler(0, intent);
	}

	public static Looper getServiceLooper() {
		return serviceLooper;
	}

	public static String getThreadSignature() {
		final Thread t = Thread.currentThread();
		return new StringBuilder(t.getName()).append("[id=").append(t.getId())
				.append(", priority=").append(t.getPriority()).append("]")
				.toString();
	}

	private void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class XmppBinder extends Binder {
		public XmppService getService() {
			return XmppService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return xmppBinder;
	}

}
