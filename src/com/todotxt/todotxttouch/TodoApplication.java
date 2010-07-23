package com.todotxt.todotxttouch;

import com.todotxt.todotxttouch.DropboxUtil.DropboxProvider;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class TodoApplication extends Application implements OnSharedPreferenceChangeListener {
	
	private final static String TAG = TodoApplication.class.getSimpleName();

	private SharedPreferences m_prefs;
	public DropboxProvider m_client;
	public String m_fileUrl;

	@Override
	public void onCreate() {
		super.onCreate();

		// Get the xml/preferences.xml preferences
		m_prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		m_prefs.registerOnSharedPreferenceChangeListener(this);
		String defValue = getString(R.string.todourl_default);
		m_fileUrl = m_prefs.getString(getString(R.string.todourl_key), defValue);

		//dropbox initialization
		initDropbox();
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		m_prefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.v(TAG, "onSharedPreferenceChanged key="+key);
		if(getString(R.string.todourl_key).equals(key)) {
			String defValue = getString(R.string.todourl_default);
			m_fileUrl = sharedPreferences.getString(key, defValue);
			//TODO
//			populateFromExternal();
		} else if (getString(R.string.username_key).equals(key)
				|| getString(R.string.password_key).equals(key)) {
			initDropbox();
		}
	}

	private void initDropbox(){
		String key = getString(R.string.username_key);
		String username = m_prefs.getString(key, null);
		key = getString(R.string.password_key);
		String password = m_prefs.getString(key, null);
		if(!Util.isEmpty(username) && !Util.isEmpty(password)){
			m_client = new DropboxProvider(Constants.CONSUMER_KEY,
					Constants.CONSUMER_SECRET, username, password);
		}
	}

}
