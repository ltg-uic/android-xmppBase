package ltg.android_xmppbase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class XmppServiceBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		 String stringExtra = intent.getStringExtra(XmppService.XMPP_MESSAGE);
		 MainActivity mainActivity = (MainActivity) context;
		 mainActivity.receiveIntent(intent);
	}

}
