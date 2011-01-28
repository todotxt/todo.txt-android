/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/TodoApplication.java
 *
 * Copyright (c) 2009-2011 mathias, Gina Trapani, Tormod Haugen
 *
 * LICENSE:
 *
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Todo.txt Touch is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt Touch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt Touch.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * @author Gina Trapani <ginatrapani[at]gmail[dot]com>
 * @author Tormod Haugen <tormodh[at]gmail[dot]com>
 * @author mathias <mathias[at]ws7862[dot](none)>
 * @author mathias <mathias[at]x2[dot](none)>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 mathias, Gina Trapani, Tormod Haugen
 */
package com.todotxt.todotxttouch;

import com.dropbox.client.DropboxAPI;
import com.dropbox.client.DropboxAPI.Config;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class TodoApplication extends Application {

	private final static String TAG = TodoApplication.class.getSimpleName();

	public SharedPreferences m_prefs;
	private DropboxAPI m_api = new DropboxAPI();
	public DropboxUtil m_util;
	private Config m_config;
	public boolean m_loggedIn = false;
	public boolean m_syncing = false;

	@Override
	public void onCreate() {
		super.onCreate();

		m_prefs = PreferenceManager.getDefaultSharedPreferences(this);

		String[] user_auth_keys = getAuthKeys();
		if (user_auth_keys[0] == null || user_auth_keys[1] == null) {
			m_loggedIn = false;
		} else {
			m_loggedIn = true;
		}

		authenticate();
		m_util = new DropboxUtil(this);
	}

	private boolean authenticate() {
		if (m_config == null)
			m_config = getConfig();

		String[] user_auth_keys = getAuthKeys();
		if (user_auth_keys[0] != null && user_auth_keys[1] != null) {
			m_config = m_api.authenticateToken(user_auth_keys[0],
					user_auth_keys[1], m_config);
			if (m_config != null)
				return true;
		}
		clearKeys();
		return false;
	}

	private void clearKeys() {
		Editor editor = m_prefs.edit();
		editor.clear();
		editor.commit();
		m_loggedIn = false;
	}

	private String[] getAuthKeys() {
		String[] keys = { null, null };
		keys[0] = m_prefs.getString(Constants.PREF_ACCESSTOKEN_KEY, null);
		keys[1] = m_prefs.getString(Constants.PREF_ACCESSTOKEN_SECRET, null);
		return keys;
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	public void unlinkDropbox() {
		Log.i(TAG, "Clearing current user data!");
		getAPI().deauthenticate();
		clearKeys();
		Constants.TODOFILE.delete();
		Constants.TODOFILETMP.delete();
	}

	protected Config getConfig() {
		if (m_config == null) {
			String consumerKey = getResources().getText(
					R.string.dropbox_consumer_key).toString();
			String consumerSecret = getText(R.string.dropbox_consumer_secret)
					.toString();

			m_config = m_api.getConfig(null, false);
			m_config.consumerKey = consumerKey;
			m_config.consumerSecret = consumerSecret;
			m_config.server = "api.dropbox.com";
			m_config.contentServer = "api-content.dropbox.com";
			m_config.port = 80;

		}
		return m_config;
	}

	public DropboxAPI getAPI() {
		return m_api;
	}

	public void setConfig(Config authenticate) {
		m_config = authenticate;
	}

	public String getRemotePath() {
		return m_prefs.getString("todotxtpath",
				getResources().getString(R.string.TODOTXTPATH_defaultPath));
	}

	public String getRemoteFileAndPath() {
		return getRemotePath() + "/" + Constants.REMOTE_FILE;
	}
}