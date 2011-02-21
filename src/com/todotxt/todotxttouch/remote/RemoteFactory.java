package com.todotxt.todotxttouch.remote;

import com.todotxt.todotxttouch.TodoApplication;
import com.todotxt.todotxttouch.remote.dropbox.DropboxRemoteTaskRepository;
import com.todotxt.todotxttouch.remote.dropbox.DropboxRemoteClient;

public class RemoteFactory {

	public static RemoteClient getRemoteClient(TodoApplication todoApplication) {
		return new DropboxRemoteClient(todoApplication);
	}

	public static RemoteTaskRepository getRemoteTaskRepository(TodoApplication todoApplication) {
		return new DropboxRemoteTaskRepository(todoApplication);
	}
}
