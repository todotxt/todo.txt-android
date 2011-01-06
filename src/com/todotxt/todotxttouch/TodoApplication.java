package com.todotxt.todotxttouch;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dropbox.client.DropboxClient;
import com.dropbox.client.DropboxClientHelper;

public class TodoApplication extends Application implements
		OnSharedPreferenceChangeListener {

	private final static String TAG = TodoApplication.class.getSimpleName();

	public SharedPreferences m_prefs;
	private DropboxClient m_client;

	@Override
	public void onCreate() {
		super.onCreate();

		m_prefs = PreferenceManager.getDefaultSharedPreferences(this);

		m_prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		m_prefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if (getString(R.string.PREFCLEAR_key).equals(key)) {
			Log.i(TAG, "Clearing current user data!");
			Editor editor = prefs.edit();
			editor.clear();
			editor.commit();
			Constants.TODOFILE.delete();
			Constants.TODOFILETMP.delete();
			Util.showToastShort(this, "Logged out!");
		}
	}

	public DropboxClient getClient(Activity cxt) {
		if (m_client == null) {
			initDropboxClient(cxt);
		}
		return m_client;
	}

	private void initDropboxClient(final Activity act) {
		String accessToken = m_prefs.getString(Constants.PREF_ACCESSTOKEN_KEY,
				null);
		String accessTokenSecret = m_prefs.getString(
				Constants.PREF_ACCESSTOKEN_SECRET, null);
		if (!Util.isEmpty(accessToken) && !Util.isEmpty(accessTokenSecret)) {
			String consumerKey = getResources().getText(
					R.string.dropbox_consumer_key).toString();
			String consumerSecret = getText(R.string.dropbox_consumer_secret)
					.toString();
			Log.i(TAG, "Using Dropbox key " + consumerKey + " and secret "
					+ consumerSecret);
			DropboxClient tempClient = DropboxClientHelper
					.newAuthenticatedClient(consumerKey, consumerSecret,
							accessToken, accessTokenSecret);
			boolean valid = DropboxClientHelper.isValidClient(tempClient);
			if (valid) {
				m_client = tempClient;
				return;
			}
		}
		// Show login dialog
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Util.LoginDialogListener dialogListener = new Util.LoginDialogListener() {
					@Override
					public void onClick(String username, String password) {
						try {
							String consumerKey = getResources().getText(
									R.string.dropbox_consumer_key).toString();
							String consumerSecret = getResources().getText(
									R.string.dropbox_consumer_secret)
									.toString();
							Log.i(TAG, "Using Dropbox key " + consumerKey
									+ " and secret " + consumerSecret);
							DropboxClient tempClient = DropboxClientHelper
									.newClient(consumerKey, consumerSecret,
											username, password);
							boolean valid = DropboxClientHelper
									.isValidClient(tempClient);
							if (valid) {
								Editor editor = m_prefs.edit();
								editor.putString(
										Constants.PREF_ACCESSTOKEN_KEY,
										tempClient.getAccessToken());
								editor.putString(
										Constants.PREF_ACCESSTOKEN_SECRET,
										tempClient.getAccessTokenSecret());
								editor.commit();
								m_client = tempClient;
							}
						} catch (Exception e) {
							Util.showToastLong(TodoApplication.this,
									"Could not create Dropbox client!");
							Log.i(TAG,
									"Could not create Dropbox client! Exception details: "
											+ e.getLocalizedMessage());
						}
					}
				};
				Util.showLoginDialog(act, R.string.dropbox_authentication,
						R.string.login, "", dialogListener,
						R.drawable.menu_sync);
			}
		});
	}
}
