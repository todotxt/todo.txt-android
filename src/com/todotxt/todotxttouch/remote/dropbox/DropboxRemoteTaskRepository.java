package com.todotxt.todotxttouch.remote.dropbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.dropbox.client.DropboxAPI;
import com.todotxt.todotxttouch.Constants;
import com.todotxt.todotxttouch.TodoApplication;
import com.todotxt.todotxttouch.remote.RemoteException;
import com.todotxt.todotxttouch.remote.RemoteTaskRepository;
import com.todotxt.todotxttouch.task.Task;
import com.todotxt.todotxttouch.util.TaskIo;

public class DropboxRemoteTaskRepository implements RemoteTaskRepository {
	private static final File TODO_TXT_TMP_FILE = new File(
			Environment.getExternalStorageDirectory(),
			"data/com.todotxt.todotxttouch/tmp/todo.txt");

	private SharedPreferences preferences;
	private DropboxRemoteClient client;
	
	public DropboxRemoteTaskRepository(TodoApplication app) {
		this.client = (DropboxRemoteClient) app.getRemoteClient();
		preferences = PreferenceManager.getDefaultSharedPreferences(app);
	}
	
	@Override
	public void init(File withLocalFile) {
		if (withLocalFile == null) {
			store(new ArrayList<Task>());
		} else {
			try {
				client.getAPI().putFile(Constants.DROPBOX_MODUS, client.getRemotePath(),
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
			DropboxAPI.FileDownload file = client.getAPI().getFileStream(
					Constants.DROPBOX_MODUS, client.getRemotePathAndFilename(), null);
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
		client.getAPI().putFile(Constants.DROPBOX_MODUS, client.getRemotePath(), TODO_TXT_TMP_FILE);
	}

}
