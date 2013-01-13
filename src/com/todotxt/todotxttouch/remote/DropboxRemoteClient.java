/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2013 Todo.txt contributors (http://todotxt.com)
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
 * @copyright 2009-2013 Todo.txt contributors (http://todotxt.com)
 */
package com.todotxt.todotxttouch.remote;

import java.io.File;
import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.todotxt.todotxttouch.Constants;
import com.todotxt.todotxttouch.R;
import com.todotxt.todotxttouch.TodoApplication;
import com.todotxt.todotxttouch.TodoTxtTouch;
import com.todotxt.todotxttouch.util.Util;

class DropboxRemoteClient implements RemoteClient {
	final static String TAG = TodoTxtTouch.class.getSimpleName();
	
	private static final String TODO_TXT_REMOTE_FILE_NAME = "todo.txt";
	private static final String DONE_TXT_REMOTE_FILE_NAME = "done.txt";
	private static final AccessType ACCESS_TYPE = AccessType.DROPBOX;
	private static final File TODO_TXT_TMP_FILE = new File(
			Environment.getExternalStorageDirectory(),
			"data/com.todotxt.todotxttouch/tmp/todo.txt");
	private static final File DONE_TXT_TMP_FILE = new File(
			Environment.getExternalStorageDirectory(),
			"data/com.todotxt.todotxttouch/tmp/done.txt");

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

	/**
	 * Store the current 'rev' from the metadata retrieved from Dropbox.
	 * 
	 * @param key
	 *            Name of the key in sharedPreferences under which to store the
	 *            rev value.
	 * @param rev
	 *            The value of the rev to be stored.
	 */
	private void storeRev(String key, String rev) {
		Log.d(TAG, "Storing rev. key=" + key + ". val=" + rev);
		Editor prefsEditor = sharedPreferences.edit();
		prefsEditor.putString(key, rev);
		if (!prefsEditor.commit()) {
			Log.e(TAG, "Failed to store rev key! key=" + key + ". val=" + rev);
		}
	}

	/**
	 * Load the last 'rev' stored from Dropbox.
	 * 
	 * @param key
	 *            Name of the key in sharedPreferences from which to retrieve
	 *            the rev value.
	 * @return The value of the rev to be retrieved.
	 */
	private String loadRev(String key) {
		return sharedPreferences.getString(key, null);
	}

	@Override
	public PullTodoResult pullTodo() {
		if (!isAvailable()) {
			Log.d(TAG, "Offline. Not Pulling.");
			Intent i = new Intent(Constants.INTENT_SET_MANUAL);
			sendBroadcast(i);
			return new PullTodoResult(null, null);
		}

		DropboxFile todoFile = new DropboxFile(
				getTodoFileRemotePathAndFilename(), TODO_TXT_TMP_FILE,
				loadRev(Constants.PREF_TODO_REV));
		DropboxFile doneFile = new DropboxFile(
				getDoneFileRemotePathAndFilename(), DONE_TXT_TMP_FILE,
				loadRev(Constants.PREF_DONE_REV));
		ArrayList<DropboxFile> dropboxFiles = new ArrayList<DropboxFile>(2);
		dropboxFiles.add(todoFile);
		dropboxFiles.add(doneFile);
		
		DropboxFileDownloader downloader = new DropboxFileDownloader(
				dropboxApi, dropboxFiles);
		downloader.pullFiles();

		File downloadedTodoFile = null;
		File downloadedDoneFile = null;
		if (todoFile.getStatus() == DropboxFileStatus.SUCCESS) {
			downloadedTodoFile = todoFile.getLocalFile();
			storeRev(Constants.PREF_TODO_REV, todoFile.getLoadedMetadata().rev);
		}
		if (doneFile.getStatus() == DropboxFileStatus.SUCCESS) {
			downloadedDoneFile = doneFile.getLocalFile();
			storeRev(Constants.PREF_DONE_REV, doneFile.getLoadedMetadata().rev);
		}

		return new PullTodoResult(downloadedTodoFile, downloadedDoneFile);
	}

	@Override
	public void pushTodo(File todoFile, File doneFile, boolean overwrite) {
		ArrayList<DropboxFile> dropboxFiles = new ArrayList<DropboxFile>(2);
		if (todoFile != null) {
			dropboxFiles.add(new DropboxFile(
					getTodoFileRemotePathAndFilename(), todoFile,
					loadRev(Constants.PREF_TODO_REV)));
		}

		if (doneFile != null) {
			dropboxFiles.add(new DropboxFile(
					getDoneFileRemotePathAndFilename(), doneFile,
					loadRev(Constants.PREF_DONE_REV)));
		}

		DropboxFileUploader uploader = new DropboxFileUploader(dropboxApi,
				dropboxFiles, overwrite);
		uploader.pushFiles();

		if (uploader.getStatus() == DropboxFileStatus.SUCCESS) {
			if (dropboxFiles.size() > 0) {
				DropboxFile todoDropboxFile = dropboxFiles.get(0);
				if (todoDropboxFile.getStatus() == DropboxFileStatus.SUCCESS) {
					storeRev(Constants.PREF_TODO_REV,
							todoDropboxFile.getLoadedMetadata().rev);
				}
			}
			if (dropboxFiles.size() > 1) {
				DropboxFile doneDropboxFile = dropboxFiles.get(1);
				if (doneDropboxFile.getStatus() == DropboxFileStatus.SUCCESS) {
					storeRev(Constants.PREF_DONE_REV,
							doneDropboxFile.getLoadedMetadata().rev);
				}
			}
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

	String getTodoFileRemotePathAndFilename() {
		return getRemotePath() + "/" + TODO_TXT_REMOTE_FILE_NAME;
	}

	String getDoneFileRemotePathAndFilename() {
		return getRemotePath() + "/" + DONE_TXT_REMOTE_FILE_NAME;
	}

	@Override
	public boolean isAvailable() {
		return Util.isOnline(todoApplication.getApplicationContext());
	}

}
