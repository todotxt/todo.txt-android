package com.todotxt.todotxttouch;

import java.util.ArrayList;

import android.util.Log;

import com.dropbox.client.DropboxAPI;
import com.dropbox.client.DropboxAPI.FileDownload;
import com.todotxt.todotxttouch.Constants;
import com.todotxt.todotxttouch.Task;
import com.todotxt.todotxttouch.TaskHelper;
import com.todotxt.todotxttouch.TodoUtil;

public class DropboxUtil {

	private final static String TAG = DropboxUtil.class.getSimpleName();

	public static boolean addTask(DropboxAPI api, String input) {
		if (api != null) {
			ArrayList<Task> tasks = null;
			try {
				tasks = fetchTasks(api);
				Task task = TaskHelper.createTask(tasks.size(), input);
				tasks.add(task);
				TodoUtil.writeToFile(tasks, Constants.TODOFILETMP);
				api.putFile(Constants.DROPBOX_MODUS,
						Constants.PATH_TO_TODO_TXT, Constants.TODOFILETMP);
				TodoUtil.writeToFile(tasks, Constants.TODOFILE);
				return true;
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		return false;
	}

	public static boolean updateTask(DropboxAPI api, char prio, String input,
			Task backup) {
		if (api != null) {
			Task t = TaskHelper.createTask(backup.id, backup.text);
			t.prio = prio;
			t.text = input;
			try {
				ArrayList<Task> tasks = fetchTasks(api);
				Task found = TaskHelper.find(tasks, backup);
				if (found != null) {
					t.id = found.id;
					TaskHelper.updateById(tasks, t);
					TodoUtil.writeToFile(tasks, Constants.TODOFILETMP);
					api.putFile(Constants.DROPBOX_MODUS,
							Constants.PATH_TO_TODO_TXT, Constants.TODOFILETMP);
					TodoUtil.writeToFile(tasks, Constants.TODOFILE);
					return true;
				} else {
					Log.v(TAG, "Task not found, not updated");
				}
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		} else {
			Log.v(TAG, "Task not updated, client is null");
		}
		return false;
	}

	public static ArrayList<Task> fetchTasks(DropboxAPI api) throws Exception {
		FileDownload file = api.getFileStream(Constants.DROPBOX_MODUS,
				Constants.REMOTE_FILE, null);
		return TodoUtil.loadTasksFromStream(file.is);
	}

}