/**
 * This file is part of Todo.txt for Android, an app for managing your todo.txt file (http://todotxt.com).
 * <p>
 * Copyright (c) 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 * <p>
 * LICENSE:
 * <p>
 * Todo.txt for Android is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 * <p>
 * Todo.txt for Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with Todo.txt for Android. If not, see
 * <http://www.gnu.org/licenses/>.
 * <p>
 * Todo.txt for Android's source code is available at https://github.com/ginatrapani/todo.txt-android
 *
 * @author Todo.txt for Android contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 */

package com.todotxt.todotxttouch;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.todotxt.todotxttouch.remote.RemoteClientManager;
import com.todotxt.todotxttouch.remote.RemoteConflictException;
import com.todotxt.todotxttouch.task.Priority;
import com.todotxt.todotxttouch.task.Sort;
import com.todotxt.todotxttouch.task.TaskBag;
import com.todotxt.todotxttouch.task.TaskBagFactory;
import com.todotxt.todotxttouch.util.Util;

import java.util.ArrayList;

public class TodoApplication extends Application {
    private final static String TAG = TodoApplication.class.getSimpleName();
    private static Context appContext;
    public TodoPreferences m_prefs;
    // filter variables
    public ArrayList<Priority> m_prios = new ArrayList<Priority>();
    public ArrayList<String> m_contexts = new ArrayList<String>();
    public ArrayList<String> m_projects = new ArrayList<String>();
    public String m_search;
    public ArrayList<String> m_filters = new ArrayList<String>();
    public Sort sort = Sort.PRIORITY_DESC;
    private RemoteClientManager remoteClientManager;
    private boolean m_pulling = false;
    private boolean m_pushing = false;
    private int pushQueue = 0;
    private TaskBag taskBag;
    private BroadcastReceiver m_broadcastReceiver;

    public static Context getAppContetxt() {
        return appContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        TodoApplication.appContext = getApplicationContext();
        m_prefs = new TodoPreferences(appContext,
                PreferenceManager.getDefaultSharedPreferences(this));

        try {
            new UpgradeHandler(this).run();
        } catch (Exception e) {
            Log.e(TAG, "Failed to run Uprade Tasks", e);
        }

        remoteClientManager = new RemoteClientManager(this, m_prefs);
        this.taskBag = TaskBagFactory.getTaskBag(this, m_prefs);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.INTENT_SET_MANUAL);
        intentFilter.addAction(Constants.INTENT_START_SYNC_WITH_REMOTE);
        intentFilter.addAction(Constants.INTENT_START_SYNC_TO_REMOTE);
        intentFilter.addAction(Constants.INTENT_START_SYNC_FROM_REMOTE);
        intentFilter.addAction(Constants.INTENT_ASYNC_FAILED);

        if (null == m_broadcastReceiver) {
            m_broadcastReceiver = new BroadcastReceiverExtension();
            registerReceiver(m_broadcastReceiver, intentFilter);
        }

        // initialize tasks so widget gets tasks after application redeployment
        taskBag.reload();

        Log.d("TODO APPLICATION1", taskBag.toString());
        Log.d("TODO APPLICATION1", this.toString());
    }

    @Override
    public void onTerminate() {
        if (null != m_broadcastReceiver) {
            unregisterReceiver(m_broadcastReceiver);
        }

        super.onTerminate();
    }

    /**
     * Are we currently pushing or pulling remote data?
     *
     * @return true iff a remote operation currently in progress.
     */
    public boolean syncInProgress() {
        return m_pulling || m_pushing;
    }

    /**
     * If we previously tried to push and failed, then attempt to push again
     * now. Otherwise, pull.
     */
    private void syncWithRemote(boolean force, boolean suppressToast) {
        if (m_prefs.needToPush()) {
            Log.d(TAG, "needToPush = true; pushing.");
            pushToRemote(force, false, suppressToast);
        } else {
            Log.d(TAG, "needToPush = false; pulling.");
            pullFromRemote(force, suppressToast);
        }
    }

    /**
     * Check network status, then push.
     */
    private void pushToRemote(boolean force, boolean overwrite, boolean suppressToast) {
        pushQueue += 1;
        m_prefs.storeNeedToPush(true);

        if (!force && m_prefs.isManualModeEnabled()) {
            Log.i(TAG, "Working offline, don't push now");
        } else if (getRemoteClientManager().getRemoteClient().isAvailable() && !m_pulling) {
            Log.i(TAG, "Working online; should push if file revisions match");

            backgroundPushToRemote(overwrite, suppressToast);
        } else if (m_pulling) {
            Log.d(TAG, "app is pulling right now. don't start push.");
        } else {
            Log.i(TAG, "Not connected, don't push now");

            if (!suppressToast) {
                showToast(R.string.toast_notconnected);
                updateSyncUI(true);
            }
        }
    }

    /**
     * Check network status, then pull.
     */
    private void pullFromRemote(boolean force, boolean suppressToast) {
        if (!force && m_prefs.isManualModeEnabled()) {
            Log.i(TAG, "Working offline, don't pull now");

            return;
        }

        m_prefs.storeNeedToPush(false);

        if (getRemoteClientManager().getRemoteClient().isAvailable() && !m_pushing) {
            Log.i(TAG, "Working online; should pull file");

            backgroundPullFromRemote();
        } else if (m_pushing) {
            Log.d(TAG, "app is pushing right now. don't start pull.");
        } else {
            Log.i(TAG, "Not connected, don't pull now");

            if (!suppressToast) {
                showToast(R.string.toast_notconnected);
                updateSyncUI(true);
            }
        }
    }

    public TaskBag getTaskBag() {
        return taskBag;
    }

    public RemoteClientManager getRemoteClientManager() {
        return remoteClientManager;
    }

    public void showToast(int resid) {
        Util.showToastLong(this, resid);
    }

    public void showToast(String string) {
        Util.showToastLong(this, string);
    }

    /**
     * Do asynchronous push with gui changes. Do availability check first.
     */
    void backgroundPushToRemote(final boolean overwrite, final boolean suppressToast) {
        if (getRemoteClientManager().getRemoteClient().isAuthenticated()) {
            if (!(m_pushing || m_pulling)) {
                new AsyncPushTask(overwrite, suppressToast).execute();
            }
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
            updateSyncUI(false);

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

                    m_pulling = false;

                    if (result) {
                        Log.d(TAG, "taskBag.pullFromRemote done");

                        updateSyncUI(true);
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

    private void updateSyncUI(boolean redrawList) {
        sendBroadcast(new Intent(Constants.INTENT_UPDATE_UI).putExtra("redrawList", redrawList));

        if (redrawList) {
            broadcastWidgetUpdate();
        }
    }

    public void broadcastWidgetUpdate() {
        Log.d(TAG, "Broadcasting widget update intent");

        Intent intent = new Intent(Constants.INTENT_WIDGET_UPDATE);
        sendBroadcast(intent);
    }

    public void storeSort() {
        m_prefs.storeSort(sort);
        broadcastWidgetUpdate();
    }

    public void getStoredSort() {
        sort = m_prefs.getSort();
    }

    public void storeFilters() {
        m_prefs.storeFilters(m_prios, m_contexts, m_projects, m_search, m_filters);
        broadcastWidgetUpdate();
    }

    public void getStoredFilters() {
        m_prios = m_prefs.getFilteredPriorities();
        m_contexts = m_prefs.getFilteredContexts();
        m_projects = m_prefs.getFilteredProjects();
        m_search = m_prefs.getSearch();
        // split on tab just in case there is a space in the text
        m_filters = m_prefs.getFilterSummaries();
    }

    private class AsyncPushTask extends AsyncTask<Void, Void, Integer> {
        static final int SUCCESS = 0;
        static final int CONFLICT = 1;
        static final int ERROR = 2;

        private boolean overwrite;
        private boolean suppressToast;

        public AsyncPushTask(boolean overwrite, boolean suppressToast) {
            this.overwrite = overwrite;
            this.suppressToast = suppressToast;
        }

        @Override
        protected void onPreExecute() {
            m_pushing = true;
            pushQueue = 0;
            updateSyncUI(false);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                Log.d(TAG, "start taskBag.pushToRemote");

                taskBag.pushToRemote(true, overwrite);
            } catch (RemoteConflictException c) {
                Log.e(TAG, c.getMessage());

                return CONFLICT;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());

                return ERROR;
            }

            return SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.d(TAG, "post taskBag.pushToremote");

            m_pushing = false;

            if (result == SUCCESS) {
                if (pushQueue > 0) {
                    m_pushing = true;

                    Log.d(TAG, "pushQueue == " + pushQueue + ". Need to push again.");

                    new AsyncPushTask(false, suppressToast).execute();
                } else {
                    Log.d(TAG, "taskBag.pushToRemote done");

                    m_prefs.storeNeedToPush(false);
                    updateSyncUI(false);
                    // Push is complete. Now do a pull in case the remote
                    // done.txt has changed.
                    pullFromRemote(true, suppressToast);
                }
            } else if (result == CONFLICT) {
                // FIXME: need to know which file had conflict
                sendBroadcast(new Intent(Constants.INTENT_SYNC_CONFLICT));
            } else {
                sendBroadcast(new Intent(Constants.INTENT_ASYNC_FAILED));
            }

            super.onPostExecute(result);
        }
    }

    private final class BroadcastReceiverExtension extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean force_sync = intent.getBooleanExtra(Constants.EXTRA_FORCE_SYNC, false);
            boolean overwrite = intent.getBooleanExtra(Constants.EXTRA_OVERWRITE, false);
            boolean suppressToast = intent.getBooleanExtra(Constants.EXTRA_SUPPRESS_TOAST, false);

            if (intent.getAction().equalsIgnoreCase(Constants.INTENT_START_SYNC_WITH_REMOTE)) {
                syncWithRemote(force_sync, suppressToast);
            } else if (intent.getAction().equalsIgnoreCase(Constants.INTENT_START_SYNC_TO_REMOTE)) {
                pushToRemote(force_sync, overwrite, suppressToast);
            } else if (intent.getAction().equalsIgnoreCase(Constants.INTENT_START_SYNC_FROM_REMOTE)) {
                pullFromRemote(force_sync, suppressToast);
            } else if (intent.getAction().equalsIgnoreCase(Constants.INTENT_ASYNC_FAILED)) {
                if (!suppressToast) {
                    showToast("Synchronizing Failed");
                }

                m_pulling = false;
                m_pushing = false;
                updateSyncUI(true);
            }
        }
    }
}
