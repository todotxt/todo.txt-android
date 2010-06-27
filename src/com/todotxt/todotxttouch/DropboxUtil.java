package com.todotxt.todotxttouch;

import java.util.List;

import android.util.Log;

import com.dropbox.client.DropboxClient;
import com.dropbox.client.DropboxClientHelper;
import com.dropbox.client.DropboxException;

public class DropboxUtil {

	private final static String TAG = DropboxUtil.class.getSimpleName();
	
	public static class DropboxProvider {
		private String consumerKey;
		private String consumerSecret;
		private String username;
		private String password;
		private DropboxClient client;
		public DropboxProvider(String consumerKey,
				String consumerSecret, String username, String password){
			this.consumerSecret = consumerSecret;
			this.consumerKey = consumerKey;
			this.username = username;
			this.password = password;
		}
		public DropboxClient get(){
			synchronized (DropboxClient.class) {
				if(client == null){
					try {
						client = DropboxClientHelper.newClient(consumerKey,
								consumerSecret, username, password);
					} catch (Exception e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
			}
			return client;
		}
	}
	
	public static boolean addTask(DropboxClient client, List<Task> tasks,
			String input) {
		if (client != null) {
			Task task = TaskHelper.createTask(tasks.size(), input);
			tasks.add(task);
			try {
				pushTasks(client, tasks);
				return true;
			} catch (DropboxException e) {
				Log.e(TAG, e.getMessage(), e);
				tasks.remove(tasks.size()-1);
				TodoUtil.writeToFile(tasks, Constants.TODOFILE);
			}
		}
		return false;
	}

	public static boolean updateTask(DropboxClient client, List<Task> tasks,
			long id, char prio, String input) {
		if (client != null) {
			Task t = TaskHelper.createTask(id, input);
			t.prio = prio;
			Task backup = TaskHelper.updateById(tasks, t);
			try {
				pushTasks(client, tasks);
				return true;
			} catch (DropboxException e) {
				Log.e(TAG, e.getMessage(), e);
				TaskHelper.updateById(tasks, backup);
				TodoUtil.writeToFile(tasks, Constants.TODOFILE);
			}
		}
		return false;
	}

	public static boolean deleteTask(DropboxClient client, List<Task> tasks,
			Task task) {
		if (client != null) {
			task.deleted = true;
			try {
				pushTasks(client, tasks);
				return true;
			} catch (DropboxException e) {
				Log.e(TAG, e.getMessage(), e);
				task.deleted = false;
				TodoUtil.writeToFile(tasks, Constants.TODOFILE);
			}
		}
		return false;
	}

	public static boolean completeTask(DropboxClient client, List<Task> tasks,
			Task task) {
		if (client != null) {
			task.completed = true;
			try {
				pushTasks(client, tasks);
				return true;
			} catch (DropboxException e) {
				Log.e(TAG, e.getMessage(), e);
				task.completed = false;
				TodoUtil.writeToFile(tasks, Constants.TODOFILE);
			}
		}
		return false;
	}

	public static void pushTasks(DropboxClient client, List<Task> tasks)
			throws DropboxException {
		TodoUtil.writeToFile(tasks, Constants.TODOFILE);
		DropboxClientHelper.putFile(client, "/", Constants.TODOFILE);
	}

}
