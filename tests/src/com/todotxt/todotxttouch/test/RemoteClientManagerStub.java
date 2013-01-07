package com.todotxt.todotxttouch.test;

import java.io.File;

import android.preference.PreferenceManager;

import com.todotxt.todotxttouch.TodoApplication;
import com.todotxt.todotxttouch.remote.Client;
import com.todotxt.todotxttouch.remote.PullTodoResult;
import com.todotxt.todotxttouch.remote.RemoteClient;
import com.todotxt.todotxttouch.remote.RemoteClientManager;

public class RemoteClientManagerStub extends RemoteClientManager {

	private RemoteClient remoteClient = new RemoteClientStub();
	
	public RemoteClientManagerStub(TodoApplication todoApplication) {
		super(todoApplication, PreferenceManager.getDefaultSharedPreferences(todoApplication));
	}

	@Override
	public RemoteClient getRemoteClient() {
		return remoteClient;
	}
	
	public class RemoteClientStub implements RemoteClient {

		@Override
		public Client getClient() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean authenticate() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public boolean startLogin() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean finishLogin() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void deauthenticate() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isAuthenticated() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public boolean isLoggedIn() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public PullTodoResult pullTodo() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void pushTodo(File todoFile, File doneFile, boolean overwrite) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isAvailable() {
			// TODO Auto-generated method stub
			return true;
		}
	}
}
