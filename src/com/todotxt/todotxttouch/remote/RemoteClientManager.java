/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/remote/RemoteClientManager.java
 *
 * Copyright (c) 2011 Tim Barlotta
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
 * @author Tim Barlotta <tim[at]barlotta[dot]net>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2011 Tim Barlotta
 */

package com.todotxt.todotxttouch.remote;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.todotxt.todotxttouch.TodoApplication;
import com.todotxt.todotxttouch.remote.local.LocalRemoteClient;

/**
 * Manager for obtaining, switching, etc. remote clients
 * 
 * @author Tim Barlotta
 */
public class RemoteClientManager implements
		SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String CURRENT_REMOTE_CLIENT = "CURRENT_REMOTE_CLIENT";
	// private final static String TAG =
	// RemoteClientManager.class.getSimpleName();
	@SuppressWarnings("unused")
	private Client currentClientToken;
	private RemoteClient currentClient;
	private TodoApplication todoApplication;
	private SharedPreferences sharedPreferences;

	public RemoteClientManager(TodoApplication todoApplication,
			SharedPreferences sharedPreferences) {
		this.todoApplication = todoApplication;
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		this.sharedPreferences = sharedPreferences;
		calculateRemoteClient(sharedPreferences);
		currentClient.authenticate();
	}

	public RemoteClient getRemoteClient() {
		return currentClient;
	}

	/**
	 * Returns the client associated with the passed in token does not switch
	 * the client
	 * 
	 * @param clientToken
	 * @return
	 */
	private RemoteClient getRemoteClient(Client clientToken) {
		
		switch (clientToken){
			case DROPBOX: return new DropboxRemoteClient(todoApplication, sharedPreferences);
			default:
			case LOCAL: return new LocalRemoteClient(todoApplication, sharedPreferences); /* awesome name :) LocalRemoteClient*/
		}
		
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (CURRENT_REMOTE_CLIENT.equals(key)){
			calculateRemoteClient(sharedPreferences);
		}
	}

	private void calculateRemoteClient(SharedPreferences sharedPreferences) {

		currentClientToken = Client.valueOf(sharedPreferences.getString(CURRENT_REMOTE_CLIENT, "DROPBOX"));
		currentClient = getRemoteClient(currentClientToken);
		
	}

	public boolean setClient(String name) {
		Editor editor = sharedPreferences.edit();
		editor.putString(CURRENT_REMOTE_CLIENT, name);
		return editor.commit();
	}
}