package com.todotxt.todotxttouch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;

import android.util.Log;

import com.dropbox.client.DropboxClient;
import com.dropbox.client.TrustedAuthenticator;

public class DropboxUtil {

	private final static String TAG = DropboxUtil.class.getSimpleName();
	
	public static class DropboxProvider {
		private String username;
		private String password;
		private DropboxClient client;
		public DropboxProvider(String consumerKey,
				String consumerSecret, String username, String password){
			this.username = username;
			this.password = password;
		}
		public DropboxClient get() throws Exception{
			synchronized (DropboxClient.class) {
				if(client == null){
					TrustedAuthenticator authenticator = new TrustedAuthenticator(getConfig());
					authenticator.retrieveTrustedAccessToken(username, password);
					client = new DropboxClient(getConfig(), authenticator);
//					client = DropboxClientHelper.newClient(consumerKey,
//							consumerSecret, username, password);
				}
			}
			return client;
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map getConfig() {
		Map config = new HashMap();
		config.put("consumer_key", Constants.CONSUMER_KEY);
		config.put("consumer_secret", Constants.CONSUMER_SECRET);
		config.put("server", "api.dropbox.com");
		config.put("content_server", "api-content.dropbox.com");
		config.put("port", 80l);
		return config;
	}
	
	public static boolean addTask(DropboxClient client, String input) {
		if (client != null) {
			ArrayList<Task> tasks = null;
			try {
				tasks = fetchTasks(client);
				Task task = TaskHelper.createTask(tasks.size(), input);
				tasks.add(task);
				TodoUtil.writeToFile(tasks, Constants.TODOFILETMP);
				client.putFile(Constants.DROPBOX_MODUS, "/", Constants.TODOFILETMP);
//				DropboxClientHelper.putFile(client, "/", Constants.TODOFILETMP);
				TodoUtil.writeToFile(tasks, Constants.TODOFILE);
				return true;
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		return false;
	}

	public static boolean updateTask(DropboxClient client, char prio, String input, Task backup) {
		if (client != null) {
			Task t = TaskHelper.createTask(backup.id, input);
			t.prio = prio;
			try {
				ArrayList<Task> tasks = fetchTasks(client);
				Task found = TaskHelper.find(tasks, backup);
				if(found != null){
					t.id = found.id;
					TaskHelper.updateById(tasks, t);
					TodoUtil.writeToFile(tasks, Constants.TODOFILETMP);
					client.putFile(Constants.DROPBOX_MODUS, "/", Constants.TODOFILETMP);
//					DropboxClientHelper.putFile(client, "/", Constants.TODOFILETMP);
					TodoUtil.writeToFile(tasks, Constants.TODOFILE);
					return true;
				}
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		} else {
			Log.e(TAG, "No client given: updateTask(client, prio, input, backup)");
		}
		return false;
	}

	public static ArrayList<Task> fetchTasks(DropboxClient client) throws Exception {
		HttpResponse file = client.getFile(Constants.DROPBOX_MODUS, Constants.REMOTE_FILE);
//		InputStream is = DropboxClientHelper.getFileStream(client, Constants.REMOTE_FILE);
		return TodoUtil.loadTasksFromStream(file.getEntity().getContent());
	}

}
