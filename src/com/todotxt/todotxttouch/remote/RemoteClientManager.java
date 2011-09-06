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
 * @author Tomasz Roszko <geekonek[at]gmail[dot]com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2011 Tim Barlotta, Tomasz Roszko
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
public class RemoteClientManager implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	private static final String CURRENT_CLIENT_KEY = "com.todotxt.currentClient";
	private final static String TAG = RemoteClientManager.class.getSimpleName();

	private Client currentClientToken;
	private RemoteClient currentClient;
	private TodoApplication todoApplication;
	private SharedPreferences sharedPreferences;

	public RemoteClientManager(TodoApplication todoApplication,	SharedPreferences sharedPreferences) {
		this.todoApplication = todoApplication;
		this.sharedPreferences = sharedPreferences;
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		
		//calculate current client from shared properties
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
		
		Log.d(TAG, "Creating new "+clientToken+" remote client instance.");
		
		switch (clientToken){
			case DROPBOX: return new DropboxRemoteClient(todoApplication, sharedPreferences);
			case LOCAL: return new LocalRemoteClient(todoApplication, sharedPreferences); /* awesome name :) LocalRemoteClient*/
			default:
				throw new IllegalArgumentException("Unsuported remote client. "+clientToken);
		}
		
	}

	/**
	 * Create new remote client when configuration property changes
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,	String key) {
		if (CURRENT_CLIENT_KEY.equals(key)){
			calculateRemoteClient(sharedPreferences);
		}
	}

	private void calculateRemoteClient(SharedPreferences sharedPreferences) {

		//initialize selected client (defaults to local for first time access, will still be redirected
		//to login screen)
		currentClientToken = Client.valueOf(
				sharedPreferences.getString(CURRENT_CLIENT_KEY, Client.LOCAL.name()));
		currentClient = getRemoteClient(currentClientToken);
		
	}

	/**
	 * Sets new selected client in configuration
	 * @param client name
	 * @return true if configuration succesfully updated
	 */
	public boolean setClient(String name) {
		Editor editor = sharedPreferences.edit();
		editor.putString(CURRENT_CLIENT_KEY, name);
		return editor.commit();
	}
}