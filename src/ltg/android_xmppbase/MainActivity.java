package ltg.android_xmppbase;

import ltg.android_xmppbase.fragment.Tab1Fragment;
import ltg.android_xmppbase.fragment.Tab2Fragment;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current tab position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	private XmppServiceBroadcastReceiver broadcastReciever = new XmppServiceBroadcastReceiver();

	private boolean mBounded;
	private XmppService mService;
	private static final String TAG = "MainActivity";

	private ActionBar actionBar;
	
	private Handler handler = new Handler() {
		public void handleMessage(Message message) {
			Object path = message.obj;
			Toast.makeText(MainActivity.this, "GOT MESSAGE FROM SERVICES",
					Toast.LENGTH_LONG).show();

		};
	};
	
	public void receiveIntent(Intent intent) {
		if( intent != null ) {
			String stringExtra = intent.getStringExtra(XmppService.XMPP_MESSAGE);
			makeToast("MESSAGE RECEIVE: :" + stringExtra);
			
			//fragments need  to be select at least once for them to be included in the fragment manager
			Tab1Fragment tab1 = (Tab1Fragment) getFragmentManager().findFragmentByTag("TAB1");
			tab1.updateUI(stringExtra);
			Tab2Fragment tab2 = (Tab2Fragment) getFragmentManager().findFragmentByTag("TAB2");
			tab2.updateUI(stringExtra);
			
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar to show tabs.
		actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		setupTabs();

		// XMPP bind
		Messenger messenger = new Messenger(handler);
		Intent intent = new Intent(this, XmppService.class);
		intent.putExtra("MESSENGER", messenger);
		intent.setData(Uri.parse("http://www.vogella.com/index.html"));
		startService(intent);

		IntentFilter intentFilter = new IntentFilter(
				XmppService.CHAT_ACTION_INTENT);
		registerReceiver(broadcastReciever, intentFilter);
	}

	private void setupTabs() {
		// TODO Auto-generated method stub

		// Theories tab

		Tab tab = actionBar
				.newTab()
				.setText("TAB 1")
				.setTabListener(
						new TabListener<Tab1Fragment>(this, "TAB1",
								Tab1Fragment.class));
		
		actionBar.addTab(tab);
		
		tab = actionBar
				.newTab()
				.setText("TAB 2")
				.setTabListener(
						new TabListener<Tab2Fragment>(this, "TAB2",
								Tab2Fragment.class));
		actionBar.addTab(tab);

	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current tab position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current tab position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	public void makeToast(String toastText) {
		Toast toast = Toast.makeText(this, toastText, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 50);
		toast.show();
	}

	public static class TabListener<T extends Fragment> implements
			ActionBar.TabListener {

		private Fragment mFragment;
		private final Activity mActivity;
		private final String mTag;
		private final Class<T> mClass;
		private Fragment currentFragment;

		/**
		 * Constructor used each time a new tab is created.
		 * 
		 * @param activity
		 *            The host Activity, used to instantiate the fragment
		 * @param tag
		 *            The identifier tag for the fragment
		 * @param clz
		 *            The fragment's Class, used to instantiate the fragment
		 */
		public TabListener(Activity activity, String tag, Class<T> clz) {
			mActivity = activity;
			mTag = tag;
			mClass = clz;
		}

		/* The following are each of the ActionBar.TabListener callbacks */

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			// Check if the fragment is already initialized
			if (mFragment == null) {
				// If not, instantiate and add it to the activity
				mFragment = Fragment.instantiate(mActivity, mClass.getName());
				ft.add(android.R.id.content, mFragment, mTag);
				// ft.commit();
			} else {

				// If it exists, simply attach it in order to show it
				ft.show(mFragment);
				// ft.commit();
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			if (mFragment != null) {
				// Detach the fragment, because another one is being attached
				// ft.detach(mFragment);
				ft.hide(mFragment);
			}
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub

		}

	}

}
