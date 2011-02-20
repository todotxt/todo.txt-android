/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/remote/dropbox/DropboxSyncClient.java
 *
 * Copyright (c) 2009-2011 mathias, Gina Trapani, Tormod Haugen, Tim Barlotta
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
 * @author mathias <mathias[at]x2[dot](none)>
 * @author Tim Barlotta <tim[at]barlotta[dot]net>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 mathias, Gina Trapani, Tormod Haugen, Tim Barlotta
 */
package com.todotxt.todotxttouch.remote.dropbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.dropbox.client.DropboxAPI;
import com.dropbox.client.DropboxAPI.Config;
import com.todotxt.todotxttouch.Constants;
import com.todotxt.todotxttouch.R;
import com.todotxt.todotxttouch.TodoApplication;
import com.todotxt.todotxttouch.remote.RemoteException;
import com.todotxt.todotxttouch.remote.RemoteTaskRepository;
import com.todotxt.todotxttouch.remote.RemoteClient;
import com.todotxt.todotxttouch.task.Task;
import com.todotxt.todotxttouch.task.TaskIo;

public class DropboxSyncClient implements RemoteClient, RemoteTaskRepository {
	private static final File TODO_TXT_TMP_FILE = new File(
			Environment.getExternalStorageDirectory(),
			"data/com.todotxt.todotxttouch/tmp/todo.txt");
	private static final String TODO_TXT_REMOTE_FILE_NAME = "todo.txt";

	private DropboxAPI api = new DropboxAPI();
	private Config config = null;

	private TodoApplication app;

	private SharedPreferences preferences;

	public DropboxSyncClient(TodoApplication todoApplication) {
		app = todoApplication;
		preferences = PreferenceManager.getDefaultSharedPreferences(app);
	}

	/**
	 * Get the stored key - secret pair for authenticating the user
	 * 
	 * @return a string array with key and secret
	 */
	private String[] getAuthToken() {
		String[] keys = { null, null };
		keys[0] = preferences.getString(Constants.PREF_ACCESSTOKEN_KEY, null);
		keys[1] = preferences
				.getString(Constants.PREF_ACCESSTOKEN_SECRET, null);
		return keys;
	}

	/**
	 * Clear the stored keys, either because they are bad, or user has requested
	 * it
	 */
	private void clearAuthToken() {
		Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
	}

	@Override
	public boolean authenticate() {
		if (null == config)
			createConfig();

		String[] userAuthToken = getAuthToken();
		if (isLoggedIn()) {
			config = api.authenticateToken(userAuthToken[0], userAuthToken[1],
					config);
			if (null != config)
				return true;
		}

		clearAuthToken();
		return false;
	}

	@Override
	public void deauthenticate() {
		clearAuthToken();
		api.deauthenticate();
	}

	@Override
	public boolean isAuthenticated() {
		return api.isAuthenticated();
	}

	/**
	 * Fetching the Consumer key + secret for this app, and creates a Dropbox
	 * Config object.
	 */
	private void createConfig() {
		String consumerKey = app.getResources()
				.getText(R.string.dropbox_consumer_key).toString();
		String consumerSecret = app.getText(R.string.dropbox_consumer_secret)
				.toString();

		config = api.getConfig(null, false);
		config.consumerKey = consumerKey;
		config.consumerSecret = consumerSecret;
		config.server = "api.dropbox.com";
		config.contentServer = "api-content.dropbox.com";
		config.port = 80;
	}

	@Override
	public boolean isLoggedIn() {
		String[] userAuthToken = getAuthToken();
		return null != userAuthToken[0] && null != userAuthToken[1];
	}

	Config getConfig() {
		if (null == config)
			createConfig();
		return config;
	}

	/**
	 * Method enabling logging in with a username and password. Do not store the
	 * username or password. Config object <code>config</code> will contain user
	 * key and secret to authenticate w/o username/password later.
	 * 
	 * @param username
	 *            - email registered with dropbox
	 * @param password
	 *            - password to authenticate with dropbox
	 * @param consumer_auth_keys
	 *            - the key and secret for this application.
	 * @return true if authenticated, false if not authenticated.
	 */
	public boolean login(String username, String password) {
		if (null == config)
			createConfig();

		if (username != null && username.length() > 0 && password != null
				&& password.length() > 0) {
			config = api.authenticate(config, username, password);
			if (config != null)
				return true;
		}

		return false;
	}

	DropboxAPI getAPI() {
		return api;
	}

	private String getRemotePathAndFilename() {
		return getRemotePath() + "/" + TODO_TXT_REMOTE_FILE_NAME;
	}

	private String getRemotePath() {
		return preferences.getString("todotxtpath", app.getResources()
				.getString(R.string.TODOTXTPATH_defaultPath));
	}

	@Override
	public RemoteTaskRepository getRemoteTaskRepository() {
		return this;
	}

	// RemoteTaskRepository

	@Override
	public void init(File withLocalFile) {
		if (withLocalFile == null) {
			store(new ArrayList<Task>());
		} else {
			try {
				api.putFile(Constants.DROPBOX_MODUS, getRemotePath(),
						withLocalFile);
			} catch (Exception e) {
				throw new RemoteException("error creating dropbox file", e);
			}
		}
	}

	@Override
	public void purge() {
		TODO_TXT_TMP_FILE.delete();
	}

	@Override
	public ArrayList<Task> load() {
		try {
			DropboxAPI.FileDownload file = api.getFileStream(
					Constants.DROPBOX_MODUS, getRemotePathAndFilename(), null);
			if (file.isError()) {
				if (404 == file.httpCode) {
					init(null);
					return new ArrayList<Task>();
				} else {
					throw new DropboxFileRemoteException(
							"Error loading from dropbox", file);
				}
			}

			return TaskIo.loadTasksFromStream(file.is);
		} catch (IOException e) {
			throw new RemoteException("I/O error trying to load from dropbox",
					e);
		}
	}

	@Override
	public void store(ArrayList<Task> tasks) {
		boolean useWindowsLineBreaks = preferences.getBoolean("linebreakspref",
				false);

		TaskIo.writeToFile(tasks, TODO_TXT_TMP_FILE, useWindowsLineBreaks);
		api.putFile(Constants.DROPBOX_MODUS, getRemotePath(), TODO_TXT_TMP_FILE);
	}

}
