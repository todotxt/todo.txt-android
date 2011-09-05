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

	private static final String LOCAL_REMOTE_CLIENT_CONNECTED = "LOCAL_REMOTE_CLIENT_CONNECTED";
	private TodoApplication todoApplication;
	private boolean loggedIn = false;
	private SharedPreferences sharedPreferences;
	
	
	public LocalRemoteClient(TodoApplication todoApplication,
			SharedPreferences sharedPreferences) {
		this.todoApplication = todoApplication;
		this.sharedPreferences = sharedPreferences;
		if (sharedPreferences.contains(LOCAL_REMOTE_CLIENT_CONNECTED) &&
				sharedPreferences.getBoolean(LOCAL_REMOTE_CLIENT_CONNECTED, false)){
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
		editor.putBoolean(LOCAL_REMOTE_CLIENT_CONNECTED, false);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void pushTodo(File file) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isAvailable() {
		return true;
	}
	
	void sendBroadcast(Intent intent) {
		todoApplication.sendBroadcast(intent);
	}

	public void login() {
		this.loggedIn = true;
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(LOCAL_REMOTE_CLIENT_CONNECTED, true);
		editor.commit();
	}

}
