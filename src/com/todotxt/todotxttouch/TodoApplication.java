/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/TodoApplication.java
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
 * @author Gina Trapani <ginatrapani[at]gmail[dot]com>
 * @author Tormod Haugen <tormodh[at]gmail[dot]com>
 * @author mathias <mathias[at]ws7862[dot](none)>
 * @author mathias <mathias[at]x2[dot](none)>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 mathias, Gina Trapani, Tormod Haugen
 */
package com.todotxt.todotxttouch;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.todotxt.todotxttouch.remote.RemoteClient;
import com.todotxt.todotxttouch.remote.RemoteFactory;
import com.todotxt.todotxttouch.task.TaskBag;
import com.todotxt.todotxttouch.task.TaskBagFactory;

public class TodoApplication extends Application {

	private final static String TAG = TodoApplication.class.getSimpleName();

	public SharedPreferences m_prefs;

	private RemoteClient remoteClient;

	public boolean m_pulling = false;
	public boolean m_pushing = false;

	private TaskBag taskBag;

	@Override
	public void onCreate() {
		super.onCreate();

		m_prefs = PreferenceManager.getDefaultSharedPreferences(this);

		remoteClient = RemoteFactory.getRemoteClient(this);

		authenticate();

		Log.v(TAG,
				"Work offline set to : "
						+ m_prefs.getBoolean("workofflinepref", false));

		this.taskBag = TaskBagFactory.getTaskBag(this, m_prefs, getResources()
				.getString(R.string.TODOTXTPATH_defaultPath));
	}

	public boolean authenticate() {
		return remoteClient.authenticate();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	public void unlinkRemoteClient() {
		Log.i(TAG, "Clearing current user data!");
		remoteClient.deauthenticate();
		taskBag.disconnectFromRemote();
	}

	public TaskBag getTaskBag() {
		return taskBag;
	}

	public RemoteClient getRemoteClient() {
		return remoteClient;
	}

}