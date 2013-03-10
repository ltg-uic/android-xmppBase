package ltg.android_xmppbase.fragment;

import ltg.android_xmppbase.MainActivity;
import ltg.android_xmppbase.R;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class Tab1Fragment extends Fragment {
	private View view;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.tab1_layout, container, false);
		Button sendButton = (Button) view.findViewById(R.id.sendButton);
		
		sendButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				String text = "This is a random message: " + Math.random();
				MainActivity activity = (MainActivity) getActivity();
				activity.sendXmppMessage(text);
			}
		});
		
		return view;
	}

	public void updateUI(String stringExtra) {
		EditText et = (EditText) view.findViewById(R.id.tab1Text);
		String s = et.getText().toString();
		et.setText(s +"\n" + stringExtra);
		
	}
}
