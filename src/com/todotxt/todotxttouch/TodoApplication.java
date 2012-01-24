/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2012 Todo.txt contributors (http://todotxt.com)
 *
 * LICENSE:
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
 * @author Todo.txt contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2012 Todo.txt contributors (http://todotxt.com)
 */
package com.todotxt.todotxttouch;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.todotxt.todotxttouch.remote.RemoteClientManager;
import com.todotxt.todotxttouch.task.TaskBag;
import com.todotxt.todotxttouch.task.TaskBagFactory;
import com.todotxt.todotxttouch.util.Util;

public class TodoApplication extends Application {
	private final static String TAG = TodoApplication.class.getSimpleName();
	public SharedPreferences m_prefs;
	private RemoteClientManager remoteClientManager;
	public boolean m_pulling = false;
	public boolean m_pushing = false;
	private TaskBag taskBag;
	private BroadcastReceiver m_broadcastReceiver;

	@Override
	public void onCreate() {
		super.onCreate();
		m_prefs = PreferenceManager.getDefaultSharedPreferences(this);
		remoteClientManager = new RemoteClientManager(this, m_prefs);
		this.taskBag = TaskBagFactory.getTaskBag(this, m_prefs);

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.INTENT_GO_OFFLINE);
		intentFilter.addAction(Constants.INTENT_START_SYNC_TO_REMOTE);
		intentFilter.addAction(Constants.INTENT_START_SYNC_FROM_REMOTE);
		intentFilter.addAction(Constants.INTENT_ASYNC_FAILED);

		if (null == m_broadcastReceiver) {
			m_broadcastReceiver = new BroadcastReceiverExtension();
			registerReceiver(m_broadcastReceiver, intentFilter);
		}

		// initialize tasks so widget gets tasks after application redeployment
		taskBag.reload();
		Log.d("\n\n\n TODO APPLICATION1\n\n\n", taskBag.toString());

	}

	@Override
	public void onTerminate() {
		unregisterReceiver(m_broadcastReceiver);
		super.onTerminate();
	}

	/**
	 * Check network status, then push.
	 */
	private void pushToRemote() {
		if (isOfflineMode()) {
			Log.d(TAG, "Working offline, don't push now");
		} else {
			if (!getRemoteClientManager().getRemoteClient().isAvailable()) {
				Log.d(TAG, "Pushing while online w/o network; go offline");
				sendBroadcast(new Intent(Constants.INTENT_GO_OFFLINE));
			} else {
				Log.i(TAG, "Working online; should push after change");
				backgroundPushToRemote();
			}
		}
	}

	/**
	 * Check network status, then pull.
	 */
	private void pullFromRemote() {
		if (isOfflineMode()) {
			Log.d(TAG, "Working offline, don't pull now");
		} else {
			if (!getRemoteClientManager().getRemoteClient().isAvailable()) {
				Log.d(TAG, "Pushing while online w/o network; go offline");
				sendBroadcast(new Intent(Constants.INTENT_GO_OFFLINE));
			} else {
				Log.i(TAG, "Working online; should puull after change");
				backgroundPullFromRemote();
			}
		}
	}

	public TaskBag getTaskBag() {
		return taskBag;
	}

	public RemoteClientManager getRemoteClientManager() {
		return remoteClientManager;
	}

	public boolean isNetworkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean networkAvailable = cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isConnected();
		Log.d(TAG, "Checking network availabilty. Network is "
				+ (networkAvailable ? "" : "not ") + "available.");
		return networkAvailable;
	}

	public boolean isOfflineMode() {
		return m_prefs.getBoolean("workofflinepref", false);
	}

	public void setOfflineMode() {
		Editor editor = m_prefs.edit();
		editor.putBoolean("workofflinepref", true);
		editor.commit();
	}

	public void showToast(String string) {
		Util.showToastLong(this, string);
	}

	/**
	 * Do asynchronous push with gui changes. Do availability check first.
	 */
	void backgroundPushToRemote() {
		if (getRemoteClientManager().getRemoteClient().isAuthenticated()) {
			m_pushing = true;
			m_pulling = false;
			updateSyncUI();

			new AsyncTask<Void, Void, Boolean>() {

				@Override
				protected Boolean doInBackground(Void... params) {
					try {
						Log.d(TAG, "start taskBag.pushToRemote");
						taskBag.pushToRemote(true);
					} catch (Exception e) {
						Log.e(TAG, e.getMessage());
						return false;
					}
					return true;
				}

				@Override
				protected void onPostExecute(Boolean result) {
					Log.d(TAG, "post taskBag.pushToremote");
					if (result) {
						Log.d(TAG, "taskBag.pushToRemote done");
						m_pushing = false;
						updateSyncUI();
					} else {
						sendBroadcast(new Intent(Constants.INTENT_ASYNC_FAILED));
					}
					super.onPostExecute(result);
				}

			}.execute();
		} else {
			Log.e(TAG, "NOT AUTHENTICATED!");
			showToast("NOT AUTHENTICATED!");
		}
	}

	/**
	 * Do an asynchronous pull from remote. Check network availability before
	 * calling this.
	 */
	private void backgroundPullFromRemote() {
		if (getRemoteClientManager().getRemoteClient().isAuthenticated()) {
			m_pulling = true;
			updateSyncUI();

			new AsyncTask<Void, Void, Boolean>() {

				@Override
				protected Boolean doInBackground(Void... params) {
					try {
						Log.d(TAG, "start taskBag.pullFromRemote");
						taskBag.pullFromRemote(true);
					} catch (Exception e) {
						Log.e(TAG, e.getMessage());
						return false;
					}
					return true;
				}

				@Override
				protected void onPostExecute(Boolean result) {
					Log.d(TAG, "post taskBag.pullFromRemote");
					if (result) {
						Log.d(TAG, "taskBag.pullFromRemote done");
						m_pulling = false;
						updateSyncUI();
					} else {
						sendBroadcast(new Intent(Constants.INTENT_ASYNC_FAILED));
					}
					super.onPostExecute(result);
				}

			}.execute();
		} else {
			Log.e(TAG, "NOT AUTHENTICATED!");
			showToast("NOT AUTHENTICATED!");
		}
	}

	private void updateSyncUI() {
		sendBroadcast(new Intent(Constants.INTENT_UPDATE_UI));
	}

	private final class BroadcastReceiverExtension extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equalsIgnoreCase(
					Constants.INTENT_START_SYNC_TO_REMOTE)) {
				pushToRemote();
			} else if (intent.getAction().equalsIgnoreCase(
					Constants.INTENT_START_SYNC_FROM_REMOTE)) {
				pullFromRemote();
			} else if (intent.getAction().equalsIgnoreCase(
					Constants.INTENT_ASYNC_FAILED)) {
				showToast("Synchronizing Failed");
				m_pulling = false;
				m_pushing = false;
				updateSyncUI();
			} else if (intent.getAction().equalsIgnoreCase(
					Constants.INTENT_GO_OFFLINE)) {
				if (isOfflineMode()) {
					showToast(getString(R.string.toast_notconnected));
				} else {
					setOfflineMode();
					showToast(getString(R.string.toast_notconnected_switch_to_offline));
				}

			}
		}
	}

	public void broadcastWidgetUpdate() {
		Log.d(TAG, "Broadcasting widget update intent");
		Intent intent = new Intent(Constants.INTENT_WIDGET_UPDATE);
		sendBroadcast(intent);
	}

}