/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/remote/local/LocalRemoteClient.java
 *
 * Copyright (c) 2011 Tomasz Roszko
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
 * @author Tomasz Roszko <geekonek[at]gmail[dot]com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2011 Tomasz Roszko
 */
package com.todotxt.todotxttouch.remote.local;

import java.io.File;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.todotxt.todotxttouch.TodoApplication;
import com.todotxt.todotxttouch.remote.Client;
import com.todotxt.todotxttouch.remote.RemoteClient;
import com.todotxt.todotxttouch.remote.RemoteLoginTask;

public class LocalRemoteClient implements RemoteClient {

	private static final String LOCAL_CLIENT_CONNECTED_FLAG = "com.todotxt.localClientConnected";
	
	private TodoApplication todoApplication;
	private boolean loggedIn = false;
	private SharedPreferences sharedPreferences;
	
	public LocalRemoteClient(TodoApplication todoApplication,
			SharedPreferences sharedPreferences) {
		this.todoApplication = todoApplication;
		this.sharedPreferences = sharedPreferences;
		//check if configuration says i'm connected
		if (sharedPreferences.getBoolean(LOCAL_CLIENT_CONNECTED_FLAG, false)){
			loggedIn = true;
		}
	}

	@Override
	public Client getClient() {
		return Client.LOCAL;
	}

	@Override
	public boolean authenticate() {
		return true;
	}

	@Override
	public void deauthenticate() {
		loggedIn = false;
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(LOCAL_CLIENT_CONNECTED_FLAG, false);
		editor.commit();
	}

	@Override
	public boolean isAuthenticated() {
		return loggedIn;
	}

	@Override
	public boolean isLoggedIn() {
		return loggedIn;
	}

	@Override
	public RemoteLoginTask getLoginTask() {
		return new LocalLoginTask(this);
	}

	@Override
	public File pullTodo() {
		// noop
		return null;
	}

	@Override
	public void pushTodo(File file) {
		// noop
	}

	@Override
	public boolean isAvailable() {
		return true;
	}
	
	void sendBroadcast(Intent intent) {
		todoApplication.sendBroadcast(intent);
	}

	public void login() {
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(LOCAL_CLIENT_CONNECTED_FLAG, true);
		if (editor.commit()){
			this.loggedIn = true;
		}
	}

}
