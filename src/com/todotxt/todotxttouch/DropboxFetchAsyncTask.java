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

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client.DropboxAPI.FileDownload;
import com.todotxt.todotxttouch.task.DropboxFileRemoteException;
import com.todotxt.todotxttouch.util.Util;

public class DropboxFetchAsyncTask extends AsyncTask<Void, Void, Boolean> {
	final static String TAG = DropboxFetchAsyncTask.class.getSimpleName();

	private TodoTxtTouch m_act;
	public FileDownload m_remoteFile = null;
	private TodoApplication m_app;

	/**
	 * @param todoTxtTouch
	 */
	DropboxFetchAsyncTask(TodoTxtTouch todoTxtTouch) {
		m_act = todoTxtTouch;
		m_app = (TodoApplication) todoTxtTouch.getApplication();
	}

	@Override
	protected void onPreExecute() {
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		m_app.m_syncing = true;
		try {
            m_app.getTaskBag().pullFromRemote();
			return true;
		} catch(DropboxFileRemoteException e) {
            m_remoteFile = e.getFileDownload();

			if (m_remoteFile.isError()) {
				m_app.getTaskBag().initRemote();
				return false;
			}
            return false;
        } catch (Exception e) {
			m_app.m_syncing = false;

			Log.e(TodoTxtTouch.TAG, e.getMessage(), e);
			return false;
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		m_act.clearFilter();
		m_act.setFilteredTasks(false);
		m_app.m_syncing = false;

		Log.d(TodoTxtTouch.TAG, "populateFromUrl size=" + m_app.getTaskBag().size());
		if (!result) {
			if (null != m_remoteFile && 404 == m_remoteFile.httpCode) {
				Intent broadcastLoginIntent = new Intent();
				broadcastLoginIntent
						.setAction("com.todotxt.todotxttouch.ASYNC_SUCCESS");
				m_app.sendBroadcast(broadcastLoginIntent);
				Util.showToastLong(m_act,
						"Added remote file");
			} else {
				Intent broadcastLoginIntent = new Intent();
				broadcastLoginIntent
						.setAction("com.todotxt.todotxttouch.ASYNC_FAILED");
				m_act.sendBroadcast(broadcastLoginIntent);
				Util.showToastLong(m_act, "Sync failed: "
						+ (null == m_remoteFile ? "Null remote file"
								: m_remoteFile.httpReason));
			}
		} else {
			Intent broadcastLoginIntent = new Intent();
			broadcastLoginIntent
					.setAction("com.todotxt.todotxttouch.ASYNC_SUCCESS");
			m_app.sendBroadcast(broadcastLoginIntent);
			Util.showToastShort(m_act, m_app.getTaskBag().size() + " items");
		}
	}
}