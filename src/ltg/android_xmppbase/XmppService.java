package ltg.android_xmppbase;

import java.util.Date;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class XmppService extends IntentService {

	public static final String XMPP_MESSAGE = "XMPP_MESSAGE";

	private final IBinder xmppBinder = new XmppBinder();

	private static final String TAG = "XmppService";
	private static final String DOMAIN = "54.243.60.48";
	private static final String USERNAME = "android";
	private static final String PASSWORD = "android";
	public  static final String CHAT_ACTION_INTENT = "CHAT_ACTION";
	private static volatile Looper serviceLooper;
	private static volatile ServiceHandler serviceHandler;

	private XMPPConnection xmppConnection;
	private long handlerThreadId;

	public XmppService() {
		super("XMPP SERVICE");
	}

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(android.os.Message msg) {
			onHandleIntent((Intent) msg.obj);
			System.out.println("hey there");

		}
	}

	Handler connectionHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			doLogin();
		};
	};

	@Override
	protected void onHandleIntent(Intent intent) {

		Log.i(TAG, "onHandleIntent");

		if (Thread.currentThread().getId() != handlerThreadId) {
			throw new IllegalThreadStateException();
		}

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		HandlerThread thread = new HandlerThread("XMPP SERVICE MOTHA FUCKA");
		thread.start();
		handlerThreadId = thread.getId();
		serviceLooper = thread.getLooper();
		serviceHandler = new ServiceHandler(serviceLooper);

		ConnectionTask task = new ConnectionTask();
		task.execute();

		return START_STICKY;
	}

	private class ConnectionTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			Log.i(TAG, "Connection in doInBackground connecting...");
			doConnection();
			return null;
		}
	}

	public void doConnection() {
		if (xmppConnection == null || xmppConnection.isConnected()) {

			ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(
					DOMAIN, 5222, "TABLET");
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				connectionConfiguration.setTruststoreType("BKS");
				connectionConfiguration.setTruststorePassword(null);
				connectionConfiguration.setTruststorePath(null);
			}
			xmppConnection = new XMPPConnection(connectionConfiguration);

			PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
			xmppConnection.addPacketListener(new PacketListener() {
				public void processPacket(Packet packet) {
					Message message = (Message) packet;
					if (message.getBody() != null) {
						String fromName = StringUtils.parseBareAddress(message
								.getFrom());
						Log.i(TAG, "Got text [" + message.getBody()
								+ "] from [" + fromName + "]");
						
						
						 Intent i = new Intent(CHAT_ACTION_INTENT);
						 i.putExtra(XMPP_MESSAGE, fromName + " says: " + message.getBody());
					     sendBroadcast(i);
					        
					        
//						showToast(fromName + " Says: " + message.getBody());
//						intent.putExtra("time", new Date().toLocaleString());
//				    	intent.putExtra("counter", String.valueOf(++counter));
//				    	
//				    	sendBroadcast(intent);
						
						
						android.os.Message packetMessage = serviceHandler.obtainMessage();
						
						
					} else {
						Log.i(TAG, packet.toXML());
					}
				}
			}, filter);

			try {
				xmppConnection.connect();
				android.os.Message obtainMessage = connectionHandler
						.obtainMessage();

				connectionHandler.sendMessage(obtainMessage);
			} catch (final XMPPException e) {
				Log.e(TAG, "Could not connect to Xmpp server.", e);
				showToast("CONNECTION FAILED XMPP SERVICE");
				return;
			}
		}
	}

	public void doLogin() {
		if (xmppConnection != null && !xmppConnection.isAuthenticated()) {
			try {
				xmppConnection.login(USERNAME, PASSWORD);
			} catch (XMPPException e) {
				showToast("LOGIN FAILED: " + USERNAME + " " + PASSWORD);
				Log.e(TAG, "Could not login into xmpp server.", e);
				e.printStackTrace();
			}
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
			Log.i(TAG,"sendToServiceHandler() called with " + intent.getAction()
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
