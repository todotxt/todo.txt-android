/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/remote/DropboxSyncClient.java
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
package com.todotxt.todotxttouch.remote;

import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;

import com.dropbox.client.DropboxAPI;
import com.dropbox.client.DropboxAPI.Config;
import com.todotxt.todotxttouch.Constants;
import com.todotxt.todotxttouch.R;
import com.todotxt.todotxttouch.TodoApplication;
import com.todotxt.todotxttouch.util.Util;

class DropboxRemoteClient implements RemoteClient {
	private static final String TODO_TXT_REMOTE_FILE_NAME = "todo.txt";
	private static final File TODO_TXT_TMP_FILE = new File(
			Environment.getExternalStorageDirectory(),
			"data/com.todotxt.todotxttouch/tmp/todo.txt");

	private DropboxAPI dropboxApi = new DropboxAPI();
	private TodoApplication todoApplication;
	private SharedPreferences sharedPreferences;
	private Config config;

	public DropboxRemoteClient(TodoApplication todoApplication,
			SharedPreferences sharedPreferences) {
		this.todoApplication = todoApplication;
		this.sharedPreferences = sharedPreferences;
	}

	@Override
	public Client getClient() {
		return Client.DROPBOX;
	}

	/**
	 * Get the stored key - secret pair for authenticating the user
	 * 
	 * @return a string array with key and secret
	 */
	private String[] getAuthToken() {
		String[] keys = { null, null };
		keys[0] = sharedPreferences.getString(Constants.PREF_ACCESSTOKEN_KEY,
				null);
		keys[1] = sharedPreferences.getString(
				Constants.PREF_ACCESSTOKEN_SECRET, null);
		return keys;
	}

	/**
	 * Store the key - secret pair for an authenticated user.
	 * 
	 * @param accessTokenKey
	 * @param accessTokenSecret
	 */
	void storeKeys(String accessTokenKey, String accessTokenSecret) {
		Editor editor = sharedPreferences.edit();
		editor.putString(Constants.PREF_ACCESSTOKEN_KEY, accessTokenKey);
		editor.putString(Constants.PREF_ACCESSTOKEN_SECRET, accessTokenSecret);
		editor.commit();
	}

	/**
	 * Clear the stored keys, either because they are bad, or user has requested
	 * it
	 */
	private void clearAuthToken() {
		Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.commit();
	}

	@Override
	public boolean authenticate() {
		if (null == config)
			createConfig();

		String[] userAuthToken = getAuthToken();
		if (isLoggedIn()) {
			config = dropboxApi.authenticateToken(userAuthToken[0],
					userAuthToken[1], config);
			if (null != config)
				return true;
		}

		clearAuthToken();
		return false;
	}

	@Override
	public void deauthenticate() {
		clearAuthToken();
		dropboxApi.deauthenticate();
		TODO_TXT_TMP_FILE.delete();
	}

	@Override
	public boolean isAuthenticated() {
		return dropboxApi.isAuthenticated();
	}

	@Override
	public boolean isLoggedIn() {
		String[] userAuthToken = getAuthToken();
		return null != userAuthToken[0] && null != userAuthToken[1];
	}

	@Override
	public RemoteLoginTask getLoginTask() {
		return new DropboxLoginAsyncTask(this);
	}

	@Override
	public File pullTodo() {
		if (!isAvailable()) {
			Intent i = new Intent("com.todotxt.todotxttouch.GO_OFFLINE");
			sendBroadcast(i);
			return null;
		}
		DropboxAPI.FileDownload fileDownload = dropboxApi.getFileStream(
				Constants.DROPBOX_MODUS, getRemotePathAndFilename(), null);
		if (fileDownload.isError()) {
			if (404 == fileDownload.httpCode) {
				pushTodo(TODO_TXT_TMP_FILE);
				return TODO_TXT_TMP_FILE;
			} else {
				throw new DropboxFileRemoteException(
						"Error loading from dropbox", fileDownload);
			}
		}

		try {
			Util.writeFile(fileDownload.is, TODO_TXT_TMP_FILE);
			return TODO_TXT_TMP_FILE;
		} catch (IOException e) {
			throw new RemoteException("Error writing to tmp file", e);
		}
	}

	@Override
	public void pushTodo(File file) {
		try {
			if (!file.exists()) {
				Util.createParentDirectory(file);
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new RemoteException("Failed to ensure that file exists", e);
		}

		dropboxApi.putFile(Constants.DROPBOX_MODUS, getRemotePath(), file);
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
	 * @return true if authenticated, false if not authenticated.
	 */
	public boolean login(String username, String password) {
		if (null == config)
			createConfig();

		if (username != null && username.length() > 0 && password != null
				&& password.length() > 0) {
			config = dropboxApi.authenticate(config, username, password);
			if (config != null)
				return true;
		}

		return false;
	}

	void sendBroadcast(Intent intent) {
		todoApplication.sendBroadcast(intent);
	}

	void showToast(String string) {
		Util.showToastLong(todoApplication, string);
	}

	DropboxAPI getAPI() {
		return dropboxApi;
	}

	String getRemotePath() {
		return sharedPreferences.getString("todotxtpath", todoApplication
				.getResources().getString(R.string.TODOTXTPATH_defaultPath));
	}

	String getRemotePathAndFilename() {
		return getRemotePath() + "/" + TODO_TXT_REMOTE_FILE_NAME;
	}

	/**
	 * Fetching the Consumer key + secret for this app, and creates a Dropbox
	 * Config object.
	 */
	private void createConfig() {
		String consumerKey = todoApplication.getResources()
				.getText(R.string.dropbox_consumer_key).toString();
		String consumerSecret = todoApplication.getText(
				R.string.dropbox_consumer_secret).toString();

		config = dropboxApi.getConfig(null, false);
		config.consumerKey = consumerKey;
		config.consumerSecret = consumerSecret;
		config.server = "api.dropbox.com";
		config.contentServer = "api-content.dropbox.com";
		config.port = 80;
	}

	public boolean isAvailable() {
		return todoApplication.isNetworkAvailable();
	}

}
