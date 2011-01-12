package com.todotxt.todotxttouch;

import com.dropbox.client.DropboxAPI;
import com.dropbox.client.DropboxAPI.Config;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class TodoApplication extends Application implements OnSharedPreferenceChangeListener {

	private final static String TAG = TodoApplication.class.getSimpleName();

	public SharedPreferences m_prefs;
	private DropboxAPI m_api = new DropboxAPI();
	private Config m_config;
	public boolean m_loggedIn = false;

	@Override
	public void onCreate() {
		super.onCreate();

		m_prefs = PreferenceManager.getDefaultSharedPreferences(this);
		m_prefs.registerOnSharedPreferenceChangeListener(this);
		
		String[] user_auth_keys = getAuthKeys();
		if ( user_auth_keys[0] == null || user_auth_keys[1] == null ) {
			m_loggedIn = false;
		} else {
			m_loggedIn = true;
		}
		
		authenticate();
	}

	private boolean authenticate() {
		if ( m_config == null ) m_config = getConfig();
		
		String[] user_auth_keys = getAuthKeys();
		if ( user_auth_keys[0] != null && user_auth_keys[1] != null ) {
			m_config = m_api.authenticateToken(user_auth_keys[0], user_auth_keys[1], m_config);
			if ( m_config != null ) return true;
		}
		clearKeys();
		m_loggedIn = false;
		return false;
	}

	private void clearKeys() {
		// TODO Auto-generated method stub
		
	}

	private String[] getAuthKeys() {
		String[] keys = {null, null};
		keys[0] = m_prefs.getString(Constants.PREF_ACCESSTOKEN_KEY, null);
		keys[1] = m_prefs.getString(Constants.PREF_ACCESSTOKEN_SECRET, null);
		return keys;
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
	
	protected Config getConfig() {
    	if (m_config == null) {
			String consumerKey = getResources().getText(R.string.dropbox_consumer_key).toString();
			String consumerSecret = getText(R.string.dropbox_consumer_secret).toString();

	    	m_config = m_api.getConfig(null, false);
	    	m_config.consumerKey=consumerKey;
	    	m_config.consumerSecret=consumerSecret;
	    	m_config.server="api.dropbox.com";
	    	m_config.contentServer="api-content.dropbox.com";
	    	m_config.port=80;

    	}
    	return m_config;
	}

	public DropboxAPI getAPI() {
		return m_api;
	}

	public void setConfig(Config authenticate) {
		m_config = authenticate;
	}
}
