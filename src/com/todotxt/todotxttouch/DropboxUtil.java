/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/DropboxUtil.java
 *
 * Copyright (c) 2009-2011 mathias, Gina Trapani, Tormod Haugen
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
 * @author mathias <mathias[at]x2[dot](none)>
 * @author Gina Trapani <ginatrapani[at]gmail[dot]com>
 * @author Tormod Haugen <tormodh[at]gmail[dot]com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 mathias, Gina Trapani, Tormod Haugen
 */
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

	TodoApplication m_app;

	public DropboxUtil(TodoApplication todoApplication) {
		m_app = todoApplication;
	}

	private final static String TAG = DropboxUtil.class.getSimpleName();

	public boolean addTask(String input) {
		DropboxAPI api = m_app.getAPI();
		ArrayList<Task> tasks = null;
		try {
			tasks = fetchTasks(api);
			Task task = TaskHelper.createTask(tasks.size(), input);
			tasks.add(task);
			TodoUtil.writeToFile(tasks, Constants.TODOFILETMP);
			api.putFile(Constants.DROPBOX_MODUS, m_app.getRemotePath(),
					Constants.TODOFILETMP);
			TodoUtil.writeToFile(tasks, Constants.TODOFILE);
			return true;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return false;
	}

	public boolean updateTask(char prio, String input, Task backup) {
		DropboxAPI api = m_app.getAPI();
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
				api.putFile(Constants.DROPBOX_MODUS, m_app.getRemotePath(),
						Constants.TODOFILETMP);
				TodoUtil.writeToFile(tasks, Constants.TODOFILE);
				return true;
			} else {
				Log.v(TAG, "Task not found, not updated");
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return false;
	}

	private ArrayList<Task> fetchTasks(DropboxAPI api) throws Exception {
		FileDownload file = api.getFileStream(Constants.DROPBOX_MODUS,
				m_app.getRemoteFileAndPath(), null);
		return TodoUtil.loadTasksFromStream(file.is);
	}

}