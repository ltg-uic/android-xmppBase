package ltg.android_xmppbase.fragment;

import ltg.android_xmppbase.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class LoginDialog {

	public static AlertDialog createLoginDialog(Context context, DialogInterface.OnClickListener positive, DialogInterface.OnClickListener negative, OnDismissListener dismissListener) {
		
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getString(R.string.login_title));
		
		builder.setPositiveButton(R.string.login_button,positive);
		builder.setNegativeButton(R.string.cancel_button,negative);
		
		SharedPreferences settings = context.getSharedPreferences(context.getString(R.string.xmpp_prefs),
				context.MODE_PRIVATE);
		String storedUserName = settings.getString(context.getString(R.string.user_name), "");
		String storedPassword = settings.getString(
				context.getString(R.string.password), "");
		View view = inflater.inflate(R.layout.authenticator_activity, null);
		if (storedUserName != null) {
			EditText userTextView = (EditText) view
					.findViewById(R.id.usernameTextView);
			userTextView.setText(storedUserName);
		}

		if (storedPassword != null) {
			EditText passwordTextView = (EditText) view
					.findViewById(R.id.passwordTextView);
			passwordTextView.setText(storedPassword);
		}
		builder.setView(view);
	
		
		AlertDialog alertDialog;
		
		if( dismissListener != null ) {
			alertDialog = builder.create();
			alertDialog.setOnDismissListener(dismissListener);
		} else {
			alertDialog = builder.create();
		}
		return alertDialog;
	}
	
}
