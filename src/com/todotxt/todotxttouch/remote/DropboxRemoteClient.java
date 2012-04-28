/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2012 Todo.txt contributors (http://todotxt.com)
 *
 * LICENSE:
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
 * @author Todo.txt contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2012 Todo.txt contributors (http://todotxt.com)
 */
package com.todotxt.todotxttouch.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.todotxt.todotxttouch.Constants;
import com.todotxt.todotxttouch.R;
import com.todotxt.todotxttouch.TodoApplication;
import com.todotxt.todotxttouch.util.Util;

class DropboxRemoteClient implements RemoteClient {
	private static final String TODO_TXT_REMOTE_FILE_NAME = "todo.txt";
	private static final AccessType ACCESS_TYPE = AccessType.DROPBOX;
	private static final File TODO_TXT_TMP_FILE = new File(
			Environment.getExternalStorageDirectory(),
			"data/com.todotxt.todotxttouch/tmp/todo.txt");

	private DropboxAPI<AndroidAuthSession> dropboxApi;
	private TodoApplication todoApplication;
	private SharedPreferences sharedPreferences;

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
	private AccessTokenPair getStoredKeys() {
		String key = null;
		String secret = null;

		key = sharedPreferences.getString(Constants.PREF_ACCESSTOKEN_KEY, null);
		secret = sharedPreferences.getString(Constants.PREF_ACCESSTOKEN_SECRET,
				null);
		if (key != null && secret != null) {
			return new AccessTokenPair(key, secret);
		}
		return null;
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
		String consumerKey = todoApplication.getResources()
				.getText(R.string.dropbox_consumer_key).toString();
		String consumerSecret = todoApplication.getText(
				R.string.dropbox_consumer_secret).toString();

		AppKeyPair appKeys = new AppKeyPair(consumerKey, consumerSecret);
		AndroidAuthSession session = new AndroidAuthSession(appKeys,
				ACCESS_TYPE);
		dropboxApi = new DropboxAPI<AndroidAuthSession>(session);

		AccessTokenPair access = getStoredKeys();
		if (access != null) {
			dropboxApi.getSession().setAccessTokenPair(access);
		}
		return true;
	}

	@Override
	public void deauthenticate() {
		clearAuthToken();
		dropboxApi.getSession().unlink();
		TODO_TXT_TMP_FILE.delete();
	}

	@Override
	public boolean isAuthenticated() {
		return dropboxApi.getSession().isLinked();
	}

	@Override
	public boolean isLoggedIn() {
		return dropboxApi.getSession().isLinked();
	}

	@Override
	public File pullTodo() {
		if (!isAvailable()) {
			Intent i = new Intent(Constants.INTENT_SET_MANUAL);
			sendBroadcast(i);
			return null;
		}
		try {
			if (!TODO_TXT_TMP_FILE.exists()) {
				Util.createParentDirectory(TODO_TXT_TMP_FILE);
				TODO_TXT_TMP_FILE.createNewFile();
			}
		} catch (IOException e) {
			throw new RemoteException("Failed to ensure that file exists", e);
		}

		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(TODO_TXT_TMP_FILE);
		} catch (FileNotFoundException e1) {
			throw new RemoteException("Failed to find file", e1);
		}

		try {
			dropboxApi.getFile(getRemotePathAndFilename(), null, outputStream,
					null);
			outputStream.flush();
			outputStream.close();
		} catch (DropboxException e) {
			throw new RemoteException("Cannot get file from Dropbox");
		} catch (IOException e) {
			throw new RemoteException("Failed to find file", e);
		}
		return TODO_TXT_TMP_FILE;

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

		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			throw new RemoteException("File " + file.getAbsolutePath()
					+ " not found", e1);
		}
		try {
			dropboxApi.putFileOverwrite(getRemotePathAndFilename(),
					inputStream, file.length(), null);
			inputStream.close();

		} catch (DropboxUnlinkedException e) {
			throw new RemoteException("User has unlinked.", e);
		} catch (DropboxException e) {
			e.printStackTrace();
			throw new RemoteException("Something went wrong while uploading.",
					e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RemoteException("Problem with IO", e);
		}
	}

	@Override
	public boolean startLogin() {
		dropboxApi.getSession().startAuthentication(
				todoApplication.getApplicationContext());
		return true;
	}

	@Override
	public boolean finishLogin() {
		if (dropboxApi.getSession().authenticationSuccessful()) {
			try {
				dropboxApi.getSession().finishAuthentication();

				AccessTokenPair tokens = dropboxApi.getSession()
						.getAccessTokenPair();

				storeKeys(tokens.key, tokens.secret);
			} catch (IllegalStateException e) {
				Log.i("DbAuthLog", "Error authenticating", e);
				return false;
			}
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

	DropboxAPI<AndroidAuthSession> getAPI() {
		return dropboxApi;
	}

	String getRemotePath() {
		return sharedPreferences.getString("todotxtpath", todoApplication
				.getResources().getString(R.string.TODOTXTPATH_defaultPath));
	}

	String getRemotePathAndFilename() {
		return getRemotePath() + "/" + TODO_TXT_REMOTE_FILE_NAME;
	}

	@Override
	public boolean isAvailable() {
		return Util.isOnline(todoApplication.getApplicationContext());
	}

	@Override
	public String getRevisionString() {
		try {
			Entry file = dropboxApi.metadata(getRemotePathAndFilename(), 1, null, false, null);
			return file.rev;
		} catch (DropboxException e) {			
			e.printStackTrace();
			throw new RemoteException("Couldn't retrieve file metadata for " + getRemotePathAndFilename(),
					e);
		}
	}

}
