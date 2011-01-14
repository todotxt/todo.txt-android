/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/DropboxFetchAsyncTask.java
 *
 * Copyright (c) 2009-2011 Tormod Haugen
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
 * @author Tormod Haugen <tormodh[at]gmail[dot]com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 Tormod Haugen
 */
package com.todotxt.todotxttouch;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client.DropboxAPI;
import com.dropbox.client.DropboxAPI.FileDownload;

public class DropboxFetchAsyncTask extends AsyncTask<Void, Void, Boolean> {
	/**
	 * 
	 */
	private TodoTxtTouch m_act;

	/**
	 * @param todoTxtTouch
	 */
	DropboxFetchAsyncTask(TodoTxtTouch todoTxtTouch) {
		m_act = todoTxtTouch;
	}

	@Override
	protected void onPreExecute() {
		m_act.m_ProgressDialog = ProgressDialog.show(m_act, "Please wait...",
				"Retrieving todo.txt ...", true);
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			TodoApplication app = (TodoApplication) m_act.getApplication();
			DropboxAPI api = app.getAPI();
			if (api.isAuthenticated()) {
				try {
					FileDownload file = api.getFileStream(
							Constants.DROPBOX_MODUS, Constants.REMOTE_FILE,
							null);
					m_act.m_tasks = TodoUtil.loadTasksFromStream(file.is);
				} catch (Exception e) {
					Log.w(TodoTxtTouch.TAG,
							"Failed to fetch todo file! Initializing dropbox support!"
									+ e.getMessage());
					if (!Constants.TODOFILE.exists()) {
						Util.createParentDirectory(Constants.TODOFILE);
						Constants.TODOFILE.createNewFile();
					}
					api.putFile(Constants.DROPBOX_MODUS,
							Constants.PATH_TO_TODO_TXT, Constants.TODOFILE);
					m_act.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Util.showToastLong(m_act,
									R.string.initialized_dropbox);
						}
					});
				}
			} else {
				Log.w(TodoTxtTouch.TAG, "Could not get tasks!");
				return false;
			}
			TodoUtil.writeToFile(m_act.m_tasks, Constants.TODOFILE);
			return true;
		} catch (Exception e) {
			Log.e(TodoTxtTouch.TAG, e.getMessage(), e);
			return false;
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		m_act.m_ProgressDialog.dismiss();
		m_act.clearFilter();
		m_act.setFilteredTasks(false);
		Log.d(TodoTxtTouch.TAG, "populateFromUrl size=" + m_act.m_tasks.size());
		if (!result) {
			Util.showToastLong(m_act, "Sync failed");
		} else {
			Util.showToastShort(m_act, m_act.m_tasks.size() + " items");
		}
	}
}