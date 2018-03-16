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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.SpannableString;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.todotxt.todotxttouch.task.FilterFactory;
import com.todotxt.todotxttouch.task.Priority;
import com.todotxt.todotxttouch.task.Sort;
import com.todotxt.todotxttouch.task.Task;
import com.todotxt.todotxttouch.task.TaskBag;
import com.todotxt.todotxttouch.task.TaskPersistException;
import com.todotxt.todotxttouch.util.Strings;
import com.todotxt.todotxttouch.util.Util;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

//import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshAttacher;
//import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;

//import de.timroes.swipetodismiss.SwipeDismissList;

public class TodoTxtTouch extends SherlockListActivity implements
        OnSharedPreferenceChangeListener, OnScrollListener {

    final static String TAG = TodoTxtTouch.class.getSimpleName();

    private final static int REQUEST_FILTER = 1;
    private final static int REQUEST_PREFERENCES = 2;
    private static final int SYNC_CHOICE_DIALOG = 100;
    private static final int SYNC_CONFLICT_DIALOG = 101;
    private static final int ARCHIVE_DIALOG = 103;
    private static TodoTxtTouch currentActivityPointer = null;
    ProgressDialog m_ProgressDialog = null;
    String m_DialogText = "";
    Boolean m_DialogActive = false;
    Menu options_menu;
    TodoApplication m_app;
    private TaskBag taskBag;
    private int mScrollPosition = -1;
    private int mScrollTop = -1;
    private Boolean wasOffline = false;
    private TaskAdapter m_adapter;
    private BroadcastReceiver m_broadcastReceiver;
    private ActionMode mMode;

    // Drawer variables
    private ArrayList<String> m_lists;
    private ListView m_drawerList;
    private DrawerLayout m_drawerLayout;
    private ActionBarDrawerToggle m_drawerToggle;

    private boolean mListScrolling = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentActivityPointer = this;

        setContentView(R.layout.main);

        m_app = (TodoApplication) getApplication();
        m_app.m_prefs.registerOnSharedPreferenceChangeListener(this);
        this.taskBag = m_app.getTaskBag();
        m_adapter = new TaskAdapter(this, R.layout.list_item,
                taskBag.getTasks(), getLayoutInflater());

        // listen to the ACTION_LOGOUT intent, if heard display LoginScreen
        // and finish() current activity
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.INTENT_ACTION_ARCHIVE);
        intentFilter.addAction(Constants.INTENT_SYNC_CONFLICT);
        intentFilter.addAction(Constants.INTENT_ACTION_LOGOUT);
        intentFilter.addAction(Constants.INTENT_UPDATE_UI);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        m_broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(
                        Constants.INTENT_ACTION_ARCHIVE)) {
                    // archive
                    // refresh screen to remove completed tasks
                    // push to remote
                    archiveTasks();
                } else if (intent.getAction().equalsIgnoreCase(
                        Constants.INTENT_ACTION_LOGOUT)) {
                    taskBag.clear();
                    m_app.broadcastWidgetUpdate();
                    Intent i = new Intent(context, LoginScreen.class);
                    startActivity(i);
                    finish();
                } else if (intent.getAction().equalsIgnoreCase(
                        Constants.INTENT_UPDATE_UI)) {
                    updateSyncUI(intent.getBooleanExtra("redrawList", false));
                } else if (intent.getAction().equalsIgnoreCase(
                        Constants.INTENT_SYNC_CONFLICT)) {
                    handleSyncConflict();
                } else if (intent.getAction().equalsIgnoreCase(
                        ConnectivityManager.CONNECTIVITY_ACTION)) {
                    handleConnectivityChange(context);
                }

                // Taskbag might have changed, update drawer adapter
                // to reflect new/removed contexts and projects
                updateNavigationDrawer();
            }
        };

        registerReceiver(m_broadcastReceiver, intentFilter);

        setListAdapter(this.m_adapter);

        ListView lv = getListView();

        lv.setTextFilterEnabled(true);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // Setup Navigation drawer
        m_drawerList = (ListView) findViewById(R.id.left_drawer);
        m_drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Set the adapter for the list view
        updateNavigationDrawer();


        // Delegate OnTouch calls to both libraries that want to receive them
        // Don't forward swipes when swiping on the left
        lv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // Don't listen to gestures on the left area of the list
                // to prevent interference with the DrawerLayout
                ViewConfiguration vc = ViewConfiguration.get(view.getContext());
                int deadZoneX = vc.getScaledTouchSlop();

                if (motionEvent.getX() < deadZoneX) {
                    return false;
                }


                // Only listen to item swipes if we are not scrolling the
                // listview
                if (!mListScrolling) {
                    return false;
                }

                return false;
            }
        });

        // We must set the scrollListener after the onTouchListener,
        // otherwise it will not fire
        lv.setOnScrollListener(this);

        initializeTasks(false);

        // Show search results
        Intent intent = getIntent();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            m_app.m_search = intent.getStringExtra(SearchManager.QUERY);
            Log.v(TAG, "Searched for " + m_app.m_search);
            m_app.storeFilters();
            setFilteredTasks(false);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Store the scrolling state of the listview
        Log.v(TAG, "Scrolling state: " + scrollState);

        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_IDLE:
                mListScrolling = false;

                break;
            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                // List is scrolling under the direct touch of the user
                mListScrolling = true;

                break;
            case OnScrollListener.SCROLL_STATE_FLING:
                // The user did a 'fling' on the list and it's still
                // scrolling
                mListScrolling = true;

                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (visibleItemCount > 0) {
            calculateScrollPosition();
        }
    }

    private void updateNavigationDrawer() {
        m_lists = contextsAndProjects();

        if (m_lists.size() == 0) {
            if (m_drawerLayout != null) {
                // No contexts or projects, disable navigation drawer
                m_drawerLayout.setDrawerLockMode(
                        DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.LEFT);
            } else {
                m_drawerList.setVisibility(View.GONE);
            }

            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        } else {
            if (m_drawerLayout != null) {
                m_drawerLayout.setDrawerLockMode(
                        DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.LEFT);
                m_drawerToggle = new ActionBarDrawerToggle(this, /*
                                                                 * host Activity
																 */
                        m_drawerLayout, /* DrawerLayout object */
                        R.drawable.ic_drawer, /* nav drawer icon to replace 'Up' caret */
                        R.string.quickfilter, /* "open drawer" description */
                        R.string.app_label /* "close drawer" description */
                ) {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        // Redraw menu to show or hide menu items
                        supportInvalidateOptionsMenu();
                        super.onDrawerSlide(drawerView, slideOffset);
                    }
                };

                // Set the drawer toggle as the DrawerListener
                m_drawerLayout.setDrawerListener(m_drawerToggle);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                m_drawerToggle.syncState();
            } else {
                m_drawerList.setVisibility(View.VISIBLE);
            }

            m_drawerList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            m_drawerList.setAdapter(new ArrayAdapter<String>(this,
                    R.layout.drawer_list_item, R.id.left_drawer_text, m_lists));
            setDrawerChoices();

            // Set the list's click listener
            m_drawerList.setOnItemClickListener(new DrawerItemClickListener());
        }
    }

    private void initializeTasks(boolean force) {
        boolean firstrun = m_app.m_prefs.isFirstRun();

        if (force || firstrun) {
            Log.i(TAG, "Initializing app");

            m_app.m_prefs.clearState();
            taskBag.clear();
            syncClient(true);
            m_app.m_prefs.storeFirstRun(false);
        } else {
            if (!m_app.m_prefs.isManualModeEnabled()) {
                syncClient(false);
            }

            taskBag.reload();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content
        if (m_drawerLayout != null) {
            boolean drawerOpen = m_drawerLayout.isDrawerVisible(Gravity.LEFT);
            menu.findItem(R.id.add_new).setVisible(!drawerOpen);
            menu.findItem(R.id.search).setVisible(!drawerOpen);
            menu.findItem(R.id.sort).setVisible(!drawerOpen);
            menu.findItem(R.id.share).setVisible(!drawerOpen);
        }

        menu.findItem(R.id.archive).setVisible(
                !m_app.m_prefs.isAutoArchiveEnabled());

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_app.m_prefs.unregisterOnSharedPreferenceChangeListener(this);
        unregisterReceiver(m_broadcastReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        calculateScrollPosition();
    }

    @Override
    protected void onResume() {
        super.onResume();
        m_app.getStoredSort();
        m_app.getStoredFilters();
        setFilteredTasks(true);

        // Select the specified item if one was passed in to this activity
        // e.g. from the widget
        Intent intent = this.getIntent();

        if (intent.hasExtra(Constants.EXTRA_TASK)) {
            int position = getPositionFromId(intent.getLongExtra(
                    Constants.EXTRA_TASK, 0));
            intent.removeExtra(Constants.EXTRA_TASK);
            getListView().setItemChecked(position, true);
        }

        // Show contextactionbar if there is a selection
        showContextActionBarIfNeeded();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Log.v(TAG, "onSharedPreferenceChanged key=" + key);

        if (m_app.m_prefs.getTodoPathKey().equals(key)) {
            // file location changed. delete old file, then force a pull
            initializeTasks(true);
        } else if (m_app.m_prefs.getPeriodicSyncPrefKey().equals(key)) {
            // auto sync enabled. set up alarm and force a sync now
            PeriodicSyncStarter.setupPeriodicSyncer(this);

            if (!m_app.m_prefs.isManualModeEnabled()) {
                syncClient(false);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("ScrollPosition", mScrollPosition);
        outState.putInt("ScrollTop", mScrollTop);

        outState.putBoolean("DialogActive", m_DialogActive);
        outState.putString("DialogText", m_DialogText);

        dismissProgressDialog(false);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        mScrollPosition = state.getInt("ScrollPosition", -1);
        mScrollTop = state.getInt("ScrollTop", -1);

        m_DialogActive = state.getBoolean("DialogActive");
        m_DialogText = state.getString("DialogText");

        if (m_DialogActive) {
            showProgressDialog(m_DialogText);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main, menu);
        this.options_menu = menu;

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (m_drawerToggle != null) {
            m_drawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home && m_drawerLayout != null) {

            if (m_drawerLayout.isDrawerOpen(m_drawerList)) {
                m_drawerLayout.closeDrawer(m_drawerList);
            } else {
                m_drawerLayout.openDrawer(m_drawerList);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "Caught exception in dispatchTouchEvent", e);
        }

        return true;
    }

    private void calculateScrollPosition() {
        ListView lv = getListView();
        mScrollPosition = lv.getFirstVisiblePosition();
        mScrollTop = lv.getFirstVisiblePosition();
        View v = lv.getChildAt(0);
        mScrollTop = (v == null) ? 0 : v.getTop();

        Log.v(TAG, "ListView index " + mScrollPosition + " top " + mScrollTop);
    }

    private String selectedTasksAsString(List<Task> tasks) {
        String text = "";

        for (Task t : tasks) {
            text += t.inFileFormat() + "\n";
        }

        return text;
    }

    private void shareTasks(List<Task> tasks) {
        String shareText = selectedTasksAsString(tasks);
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                R.string.share_subject);
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);

        startActivity(Intent.createChooser(shareIntent,
                getString(R.string.share_title)));
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void addToCalendar(List<Task> checkedTasks) {
        Intent intent;
        String calendarTitle = getString(R.string.calendar_title);
        String calendarDescription = "";

        if (checkedTasks.size() == 1) {
            // Set the task as title
            calendarTitle = checkedTasks.get(0).getText();
        } else {
            // Set the tasks as description
            calendarDescription = selectedTasksAsString(checkedTasks);

        }

        intent = new Intent(android.content.Intent.ACTION_EDIT)
                .setType(Constants.ANDROID_EVENT)
                .putExtra(Events.TITLE, calendarTitle)
                .putExtra(Events.DESCRIPTION, calendarDescription);
        startActivity(intent);
    }

    private void prioritizeTasks(final ArrayList<Task> tasks) {
        final String[] prioArr = Priority
                .rangeInCode(Priority.NONE, Priority.Z).toArray(new String[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_priority));
        builder.setSingleChoiceItems(prioArr, 0, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                dialog.dismiss();
                new AsyncTask<Object, Void, Boolean>() {
                    protected void onPreExecute() {
                        m_ProgressDialog = showProgressDialog(getString(R.string.progress_prioritize));
                    }

                    @Override
                    protected Boolean doInBackground(Object... params) {
                        try {
                            @SuppressWarnings("unchecked")
                            ArrayList<Task> tasks = (ArrayList<Task>) params[0];
                            String[] prioArr = (String[]) params[1];
                            int which = (Integer) params[2];

                            for (Task task : tasks) {
                                task.setPriority(Priority
                                        .toPriority(prioArr[which]));
                                taskBag.update(task);
                            }

                            m_app.broadcastWidgetUpdate();

                            return true;
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage(), e);

                            return false;
                        }
                    }

                    protected void onPostExecute(Boolean result) {
                        TodoTxtTouch.currentActivityPointer
                                .dismissProgressDialog(true);

                        if (result) {
                            sendBroadcast(new Intent(
                                    Constants.INTENT_START_SYNC_TO_REMOTE));
                        } else {
                            Util.showToastLong(TodoTxtTouch.this,
                                    getString(R.string.error_prioritize));
                        }
                    }
                }.execute(tasks, prioArr, which);
            }
        });

        builder.show();
    }

    private void undoCompleteTasks(final ArrayList<Task> tasks,
                                   final boolean showConfirm) {
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new AsyncTask<Object, Void, Boolean>() {
                    protected void onPreExecute() {
                        if (showConfirm) {
                            m_ProgressDialog = showProgressDialog(getString(R.string.progress_uncomplete));
                        }
                    }

                    @Override
                    protected Boolean doInBackground(Object... params) {
                        try {
                            @SuppressWarnings("unchecked")
                            ArrayList<Task> tasks = (ArrayList<Task>) params[0];
                            for (Task task : tasks) {
                                task.markIncomplete();
                                task.setPriority(task.getOriginalPriority());

                                try {
                                    taskBag.update(task);
                                } catch (TaskPersistException tpe) {
                                    if (m_app.m_prefs.isAutoArchiveEnabled()) {
                                        // if the task was not found, and
                                        // archiving is enabled
                                        // we need to add it to the list (in the
                                        // original position)
                                        // and remove it from the done.txt file
                                        taskBag.unarchive(task);
                                    }
                                }
                            }

                            return true;
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage(), e);

                            return false;
                        }
                    }

                    protected void onPostExecute(Boolean result) {
                        TodoTxtTouch.currentActivityPointer
                                .dismissProgressDialog(true);

                        if (result) {
                            sendBroadcast(new Intent(
                                    Constants.INTENT_START_SYNC_TO_REMOTE));
                        } else {
                            Util.showToastLong(TodoTxtTouch.this,
                                    getString(R.string.error_uncomplete));
                        }
                    }
                }.execute(tasks);
            }
        };

        if (showConfirm) {
            Util.showConfirmationDialog(this, R.string.areyousure, listener,
                    R.string.unComplete);
        } else {
            listener.onClick(null, 0);
        }
    }

    private void completeTasks(ArrayList<Task> tasks, final boolean showProgress) {
        // Log.v(TAG, "Completing task with this text: " + task.getText());
        new AsyncTask<Object, Void, Boolean>() {

            protected void onPreExecute() {
                if (showProgress) {
                    m_ProgressDialog = showProgressDialog(getString(R.string.progress_complete));
                }
            }

            @Override
            protected Boolean doInBackground(Object... params) {
                try {
                    @SuppressWarnings("unchecked")
                    ArrayList<Task> tasks = (ArrayList<Task>) params[0];

                    for (Task task : tasks) {
                        task.markComplete(new Date());
                        taskBag.update(task);
                    }

                    if (m_app.m_prefs.isAutoArchiveEnabled()) {
                        taskBag.archive();
                    }

                    m_app.broadcastWidgetUpdate();

                    return true;
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);

                    return false;
                }
            }

            protected void onPostExecute(Boolean result) {
                TodoTxtTouch.currentActivityPointer.dismissProgressDialog(true);

                if (result) {
                    sendBroadcast(new Intent(
                            Constants.INTENT_START_SYNC_TO_REMOTE));
                } else {
                    Util.showToastLong(TodoTxtTouch.this,
                            getString(R.string.error_complete));
                }
            }
        }.execute(tasks);
    }

    private void editTask(Task task) {
        Intent intent = new Intent(this, AddTask.class);
        intent.putExtra(Constants.EXTRA_TASK, (Serializable) task);
        startActivity(intent);
    }

    private void deleteTasks(final ArrayList<Task> tasks) {
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new AsyncTask<Object, Void, Boolean>() {
                    protected void onPreExecute() {
                        m_ProgressDialog = showProgressDialog(getString(R.string.progress_delete));
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    protected Boolean doInBackground(Object... params) {
                        try {
                            for (Task task : (ArrayList<Task>) params[0]) {
                                taskBag.delete(task);
                            }

                            m_app.broadcastWidgetUpdate();

                            return true;
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage(), e);

                            return false;
                        }
                    }

                    protected void onPostExecute(Boolean result) {
                        TodoTxtTouch.currentActivityPointer
                                .dismissProgressDialog(true);

                        if (result) {
                            sendBroadcast(new Intent(
                                    Constants.INTENT_START_SYNC_TO_REMOTE));
                        } else {
                            Util.showToastLong(TodoTxtTouch.this,
                                    getString(R.string.error_delete));
                        }
                    }
                }.execute(tasks);
            }
        };

        Util.showDeleteConfirmationDialog(this, listener);
    }

    private void archiveTasks() {
        new AsyncTask<Void, Void, Boolean>() {
            protected void onPreExecute() {
                m_ProgressDialog = showProgressDialog(getString(R.string.progress_archive));
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    taskBag.archive();

                    return true;
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);

                    return false;
                }
            }

            protected void onPostExecute(Boolean result) {
                TodoTxtTouch.currentActivityPointer.dismissProgressDialog(true);

                if (result) {
                    Util.showToastLong(TodoTxtTouch.this,
                            getString(R.string.confirm_archive));
                    sendBroadcast(new Intent(
                            Constants.INTENT_START_SYNC_TO_REMOTE));
                } else {
                    Util.showToastLong(TodoTxtTouch.this,
                            getString(R.string.error_archive));
                }
            }
        }.execute();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        Log.v(TAG, "onMenuItemSelected: " + item.getItemId());

        switch (item.getItemId()) {
            case R.id.add_new:
                startAddTaskActivity();

                break;
            case R.id.search:
                onSearchRequested();

                break;
            case R.id.preferences:
                startPreferencesActivity();

                break;
            case R.id.sync:
                Log.v(TAG, "onMenuItemSelected: sync");

                syncClient(false);

                break;
            case R.id.sort:
                startSortDialog();

                break;
            case R.id.share:
                shareTasks(m_adapter.getItems());

                break;
            case R.id.archive:
                showDialog(ARCHIVE_DIALOG);

                break;
            default:
                return super.onMenuItemSelected(featureId, item);
        }

        return true;
    }

    private void startAddTaskActivity() {
        Intent intent = new Intent(this, AddTask.class);
        startActivity(intent);
    }

    private ArrayList<String> contextsAndProjects() {
        final ArrayList<String> filterItems = new ArrayList<String>();
        ArrayList<String> contexts = taskBag.getContexts(false);
        Collections.sort(contexts);

        for (String item : contexts) {
            filterItems.add("@" + item);
        }

        ArrayList<String> projects = taskBag.getProjects(false);
        Collections.sort(projects);

        for (String item : projects) {
            filterItems.add("+" + item);
        }

        return filterItems;
    }

    private void startSortDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sort_dialog_header);
        builder.setSingleChoiceItems(R.array.sort, m_app.sort.getId(),
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.v(TAG, "onClick " + which);

                        m_app.sort = Sort.getById(which);
                        m_app.storeSort();
                        dialog.dismiss();
                        setFilteredTasks(false);
                    }
                });
        builder.show();
    }

    private void startPreferencesActivity() {
        Intent settingsActivity = new Intent(getBaseContext(),
                Preferences.class);
        startActivityForResult(settingsActivity, REQUEST_PREFERENCES);
    }

    private void handleConnectivityChange(Context context) {
        if (Util.isOnline(context)) {
            // This is called quite often, seemingly every
            // time there is a change in signal strength?
            // Using the wasOffline flag to limit the frequency of syncs.
            if (!m_app.m_prefs.isManualModeEnabled() && wasOffline) {
                Log.d(TAG, "Got connectivity notification. Syncing now...");

                sendBroadcast(new Intent(
                        Constants.INTENT_START_SYNC_WITH_REMOTE));
            }

            wasOffline = false;
        } else {
            wasOffline = true;
        }
    }

    /**
     * Called when we can't sync due to a merge conflict. Prompts the user to
     * force an upload or download.
     */
    @SuppressWarnings("deprecation")
    private void handleSyncConflict() {
        showDialog(SYNC_CONFLICT_DIALOG);
    }

    /**
     * Sync with remote client.
     * <ul>
     * <li>Will Pull in auto mode.
     * <li>Will ask "push or pull" in manual mode.
     * </ul>
     *
     * @param force
     *            true to force pull
     */
    @SuppressWarnings("deprecation")
    private void syncClient(boolean force) {

        if (!force && m_app.m_prefs.isManualModeEnabled()) {
            Log.v(TAG,
                    "Manual mode, choice forced; prompt user to ask which way to sync");

            showDialog(SYNC_CHOICE_DIALOG);
        } else {
            Log.i(TAG, "auto sync mode; should automatically sync; force = "
                    + force);

            Intent i = new Intent(Constants.INTENT_START_SYNC_WITH_REMOTE);

            if (force) {
                i.putExtra(Constants.EXTRA_FORCE_SYNC, true);
            }

            sendBroadcast(i);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.v(TAG, "onActivityResult: resultCode=" + resultCode + " i=" + data);

        if (requestCode == REQUEST_FILTER) {
            if (resultCode == Activity.RESULT_OK) {
                m_app.m_prios = Priority.toPriority(data
                        .getStringArrayListExtra(Constants.EXTRA_PRIORITIES));
                m_app.m_projects = data
                        .getStringArrayListExtra(Constants.EXTRA_PROJECTS);
                m_app.m_contexts = data
                        .getStringArrayListExtra(Constants.EXTRA_CONTEXTS);
                m_app.m_search = data.getStringExtra(Constants.EXTRA_SEARCH);
                m_app.m_filters = data
                        .getStringArrayListExtra(Constants.EXTRA_APPLIED_FILTERS);
                setDrawerChoices();
                m_app.storeFilters();
                setFilteredTasks(false);
            }
        } else if (requestCode == REQUEST_PREFERENCES) {
			/* Do nothing */
        }
    }

    protected void dismissProgressDialog(Boolean reload) {
        if (m_ProgressDialog != null) {
            m_ProgressDialog.dismiss();
            m_DialogActive = false;
        }

        if (reload) {
            setFilteredTasks(reload);
        }
    }

    protected ProgressDialog showProgressDialog(String message) {
        if (m_ProgressDialog != null) {
            dismissProgressDialog(false);
        }
        m_DialogText = message;
        m_DialogActive = true;

        return (m_ProgressDialog = ProgressDialog.show(TodoTxtTouch.this,
                message, getString(R.string.wait_progress), true));
    }

    @Override
    protected Dialog onCreateDialog(final int id) {

        if (id == SYNC_CHOICE_DIALOG) {
            Log.v(TAG, "Time to show the sync choice dialog");

            AlertDialog.Builder upDownChoice = new AlertDialog.Builder(this);
            upDownChoice.setTitle(R.string.sync_dialog_title);
            upDownChoice.setMessage(R.string.sync_dialog_msg);

            upDownChoice.setPositiveButton(R.string.sync_dialog_upload,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            sendBroadcast(new Intent(
                                    Constants.INTENT_START_SYNC_TO_REMOTE)
                                    .putExtra(Constants.EXTRA_FORCE_SYNC, true));
                            // backgroundPushToRemote();
                            showToast(getString(R.string.sync_upload_message));
                        }
                    });

            upDownChoice.setNegativeButton(R.string.sync_dialog_download,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            sendBroadcast(new Intent(
                                    Constants.INTENT_START_SYNC_FROM_REMOTE)
                                    .putExtra(Constants.EXTRA_FORCE_SYNC, true));
                            // backgroundPullFromRemote();
                            showToast(getString(R.string.sync_download_message));
                        }
                    });

            upDownChoice.setOnCancelListener(new OnCancelListener() {
                @SuppressWarnings("deprecation")
                @Override
                public void onCancel(DialogInterface dialog) {
                    updateSyncUI(false);
                    removeDialog(id);
                }
            });

            return upDownChoice.create();
        } else if (id == SYNC_CONFLICT_DIALOG) {
            Log.v(TAG, "Time to show the sync conflict dialog");

            AlertDialog.Builder upDownChoice = new AlertDialog.Builder(this);
            upDownChoice.setTitle(R.string.sync_conflict_dialog_title);
            upDownChoice.setMessage(R.string.sync_conflict_dialog_msg);

            upDownChoice.setPositiveButton(R.string.sync_dialog_upload,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            Log.v(TAG, "User selected PUSH");

                            sendBroadcast(new Intent(
                                    Constants.INTENT_START_SYNC_TO_REMOTE)
                                    .putExtra(Constants.EXTRA_OVERWRITE, true)
                                    .putExtra(Constants.EXTRA_FORCE_SYNC, true));
                            // backgroundPushToRemote();
                            showToast(getString(R.string.sync_upload_message));
                        }
                    });

            upDownChoice.setNegativeButton(R.string.sync_dialog_download,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            Log.v(TAG, "User selected PULL");

                            sendBroadcast(new Intent(
                                    Constants.INTENT_START_SYNC_FROM_REMOTE)
                                    .putExtra(Constants.EXTRA_FORCE_SYNC, true));
                            // backgroundPullFromRemote();
                            showToast(getString(R.string.sync_download_message));
                        }
                    });

            upDownChoice.setOnCancelListener(new OnCancelListener() {
                @SuppressWarnings("deprecation")
                @Override
                public void onCancel(DialogInterface dialog) {
                    updateSyncUI(false);
                    removeDialog(id);
                }
            });

            return upDownChoice.create();
        } else if (id == ARCHIVE_DIALOG) {
            AlertDialog.Builder archiveAlert = new AlertDialog.Builder(this);
            archiveAlert.setTitle(R.string.archive_now_title);
            archiveAlert.setMessage(R.string.archive_now_explainer);

            archiveAlert.setPositiveButton(R.string.archive_now_title,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TodoTxtTouch.this.setResult(RESULT_OK);

                            // produce a archive intent and broadcast it
                            Intent broadcastArchiveIntent = new Intent();
                            broadcastArchiveIntent
                                    .setAction("com.todotxt.todotxttouch.ACTION_ARCHIVE");
                            sendBroadcast(broadcastArchiveIntent);
                        }
                    });

            archiveAlert.setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @SuppressWarnings("deprecation")
                        public void onClick(DialogInterface arg0, int arg1) {
                            removeDialog(id);
                        }
                    });

            archiveAlert.setOnCancelListener(new OnCancelListener() {
                @SuppressWarnings("deprecation")
                @Override
                public void onCancel(DialogInterface dialog) {
                    removeDialog(id);
                }
            });

            return archiveAlert.create();
        } else {
            return null;
        }
    }

    /** Handle "add task" action. */
    public void onAddTaskClick(View v) {
        Intent i = new Intent(this, AddTask.class);

        i.putExtra(Constants.EXTRA_PRIORITIES_SELECTED, m_app.m_prios);
        i.putExtra(Constants.EXTRA_CONTEXTS_SELECTED, m_app.m_contexts);
        i.putExtra(Constants.EXTRA_PROJECTS_SELECTED, m_app.m_projects);

        startActivity(i);
    }

    /** Handle "refresh/download" action. */
    public void onSyncClick(View v) {
        Log.v(TAG, "titlebar: sync");

        syncClient(false);
    }

    /** Handle clear filter click **/
    public void onClearClick(View v) {
        clearFilter();

        // End current activity if it's search results
        Intent intent = getIntent();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            finish();
        } else { // otherwise just clear the filter in the current activity
            setFilteredTasks(false);
        }
    }

    private int getPositionFromId(long id) {
        for (int position = 0; position < m_adapter.getCount(); position++) {
            Task task = m_adapter.getItem(position);

            if (task.getId() == id) {
                return position;
            }
        }

        return 0;
    }

    private ArrayList<Task> getCheckedTasks() {
        ArrayList<Task> result = new ArrayList<Task>();
        SparseBooleanArray checkedItems = getListView()
                .getCheckedItemPositions();

        for (int i = 0; i < checkedItems.size(); i++) {
            if (checkedItems.valueAt(i)) {
                result.add(m_adapter.getItem(checkedItems.keyAt(i)));
            }
        }

        return result;
    }

    boolean inActionMode() {
        return mMode != null;
    }

    void showContextActionBarIfNeeded() {
        ArrayList<Task> checkedTasks = getCheckedTasks();
        int checkedCount = checkedTasks.size();

        if (inActionMode() && checkedCount == 0) {
            mMode.finish();

            return;
        } else if (checkedCount == 0) {
            return;
        }

        if (mMode == null) {
            mMode = startActionMode(new Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    getSupportMenuInflater().inflate(R.menu.main_long, menu);

                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode,
                                                   MenuItem item) {
                    ArrayList<Task> checkedTasks = getCheckedTasks();
                    int menuid = item.getItemId();
                    Intent intent;

                    switch (menuid) {
                        case R.id.update:
                            if (checkedTasks.size() == 1) {
                                editTask(checkedTasks.get(0));
                            } else {
                                Log.w(TAG,
                                        "More than one task was selected while handling update menu");
                            }

                            break;
                        case R.id.done:
                            completeTasks(checkedTasks, true);

                            break;
                        case R.id.priority:
                            prioritizeTasks(checkedTasks);

                            break;
                        case R.id.share:
                            shareTasks(checkedTasks);

                            break;
                        case R.id.calendar:
                            addToCalendar(checkedTasks);

                            break;
                        case R.id.uncomplete:
                            undoCompleteTasks(checkedTasks, true);

                            break;
                        case R.id.delete:
                            deleteTasks(checkedTasks);

                            break;
                        case R.id.url:
                            Log.v(TAG, "url: " + item.getTitle().toString());

                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item
                                    .getTitle().toString()));
                            startActivity(intent);

                            break;
                        case R.id.mail:
                            Log.v(TAG, "mail: " + item.getTitle().toString());

                            intent = new Intent(Intent.ACTION_SEND, Uri.parse(item
                                    .getTitle().toString()));
                            intent.putExtra(android.content.Intent.EXTRA_EMAIL,
                                    new String[]{item.getTitle().toString()});
                            intent.setType("text/plain");
                            startActivity(intent);

                            break;
                        case R.id.phone_number:
                            Log.v(TAG, "phone_number");

                            intent = new Intent(Intent.ACTION_DIAL,
                                    Uri.parse("tel:" + item.getTitle().toString()));
                            startActivity(intent);

                            break;
                        default:
                            Log.w(TAG, "unrecognized menuItem: " + menuid);
                    }

                    mMode.finish();

                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    getListView().clearChoices();
                    m_adapter.notifyDataSetChanged();
                    mMode = null;
                }
            });
        }
        mMode.setTitle(checkedCount + " " + getString(R.string.selected));
        Menu menu = mMode.getMenu();
        MenuItem updateAction = menu.findItem(R.id.update);
        MenuItem completeAction = menu.findItem(R.id.done);
        MenuItem uncompleteAction = menu.findItem(R.id.uncomplete);

        // Only show update action with a single task selected
        if (checkedCount == 1) {
            updateAction.setVisible(true);
            Task task = checkedTasks.get(0);

            if (task.isCompleted()) {
                completeAction.setVisible(false);
            } else {
                uncompleteAction.setVisible(false);
            }

            for (URL url : task.getLinks()) {
                menu.add(Menu.CATEGORY_SECONDARY, R.id.url, Menu.NONE,
                        url.toString());
            }

            for (String s1 : task.getMailAddresses()) {
                menu.add(Menu.CATEGORY_SECONDARY, R.id.mail, Menu.NONE, s1);
            }

            for (String s : task.getPhoneNumbers()) {
                menu.add(Menu.CATEGORY_SECONDARY, R.id.phone_number, Menu.NONE,
                        s);
            }
        } else {
            updateAction.setVisible(false);
            completeAction.setVisible(true);
            uncompleteAction.setVisible(true);
            menu.removeGroup(Menu.CATEGORY_SECONDARY);
        }
    }

    void clearFilter() {
        // Filter cleared, exit CAB if active
        if (inActionMode()) {
            mMode.finish();
        }

        m_app.m_prios = new ArrayList<Priority>(); // Collections.emptyList();
        m_app.m_contexts = new ArrayList<String>(); // Collections.emptyList();
        m_app.m_projects = new ArrayList<String>(); // Collections.emptyList();
        m_app.m_filters = new ArrayList<String>();
        m_app.m_search = "";
        m_app.storeFilters();
        setDrawerChoices();
    }

    void setFilteredTasks(boolean reload) {
        if (reload) {
            try {
                taskBag.reload();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        if (mScrollPosition < 0) {
            calculateScrollPosition();
        }

        m_adapter.clear();
        for (Task task : taskBag.getTasks(FilterFactory.generateAndFilter(
                m_app.m_prios, m_app.m_contexts, m_app.m_projects,
                m_app.m_search, false), m_app.sort.getComparator())) {
            m_adapter.add(task);
        }

        ListView lv = getListView();
        lv.setSelectionFromTop(mScrollPosition, mScrollTop);

        final TextView filterText = (TextView) findViewById(R.id.filter_text);
        final LinearLayout actionbar = (LinearLayout) findViewById(R.id.actionbar);
        final ImageView actionbar_icon = (ImageView) findViewById(R.id.actionbar_icon);

        if (filterText != null) {
            if (m_app.m_filters.size() > 0) {
                String filterTitle = getString(R.string.title_filter_applied)
                        + " ";
                int count = m_app.m_filters.size();

                for (int i = 0; i < count; i++) {
                    filterTitle += m_app.m_filters.get(i) + " ";
                }

                if (!Strings.isEmptyOrNull(m_app.m_search)) {
                    filterTitle += getString(R.string.filter_tab_search);
                }

                actionbar_icon.setImageResource(R.drawable.ic_actionbar_filter);

                actionbar.setVisibility(View.VISIBLE);
                filterText.setText(filterTitle);
            } else if (!Strings.isEmptyOrNull(m_app.m_search)) {
                if (filterText != null) {
                    actionbar_icon
                            .setImageResource(R.drawable.ic_actionbar_search);
                    filterText.setText(getString(R.string.title_search_results)
                            + " " + m_app.m_search);

                    actionbar.setVisibility(View.VISIBLE);
                }
            } else {
                filterText.setText("");
                actionbar.setVisibility(View.GONE);
            }
        }

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        l.setItemChecked(position, l.isItemChecked(position));
        showContextActionBarIfNeeded();
    }

    private void updateSyncUI(boolean redrawList) {
        if (redrawList) {
            // hide action bar
            findViewById(R.id.actionbar).setVisibility(View.GONE);
            setFilteredTasks(false);
        }
    }

    public void onRefreshStarted(View view) {
        syncClient(false);
    }

    public void showToast(String string) {
        Util.showToastLong(this, string);
    }

    public void showToast(int resid) {
        Util.showToastLong(this, resid);
    }

    private void setDrawerChoices() {
        m_drawerList.clearChoices();
        boolean haveContexts = false;
        boolean haveProjects = false;

        for (int i = 0; i < m_lists.size(); i++) {
            char sigil = m_lists.get(i).charAt(0);
            String item = m_lists.get(i).substring(1);

            if (sigil == '@' && m_app.m_contexts.contains(item)) {
                m_drawerList.setItemChecked(i, true);
                haveContexts = true;
            } else if (sigil == '+' && m_app.m_projects.contains(item)) {
                m_drawerList.setItemChecked(i, true);
                haveProjects = true;
            }
        }

        if (haveContexts) {
            if (!m_app.m_filters
                    .contains(getString(R.string.filter_tab_contexts))) {
                m_app.m_filters.add(getString(R.string.filter_tab_contexts));
            }
        } else {
            m_app.m_filters.remove(getString(R.string.filter_tab_contexts));
            m_app.m_contexts = new ArrayList<String>();
        }

        if (haveProjects) {
            if (!m_app.m_filters
                    .contains(getString(R.string.filter_tab_projects))) {
                m_app.m_filters.add(getString(R.string.filter_tab_projects));
            }
        } else {
            m_app.m_filters.remove(getString(R.string.filter_tab_projects));
            m_app.m_projects = new ArrayList<String>();
        }

        ArrayAdapter<?> adapter = (ArrayAdapter<?>) m_drawerList.getAdapter();

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private static class ViewHolder {
        private TextView taskprio;
        private TextView tasktext;
        private TextView taskage;
    }

    public class TaskAdapter extends ArrayAdapter<Task> {
        private LayoutInflater m_inflater;

        public TaskAdapter(Context context, int textViewResourceId,
                           List<Task> tasks, LayoutInflater inflater) {
            super(context, textViewResourceId, tasks);
            this.m_inflater = inflater;
        }

        // @Override
        // public Filter getFilter() {
        // return new Filter() {
        //
        // @Override
        // protected FilterResults performFiltering(CharSequence search) {
        // m_search = search.toString();
        // storeFilters();
        // return null;
        // }
        //
        // @Override
        // protected void publishResults(CharSequence arg0,
        // FilterResults arg1) {
        // setFilteredTasks(false);
        // }
        //
        // };
        // }

        @Override
        public void clear() {
            super.clear();
        }

        @Override
        public long getItemId(int position) {
            if (!this.isEmpty()) {
                return this.getItem(position).getId();
            } else {
                // Seemed to be an emulator only bug; having an item "selected"
                // (scroll-wheel etc) when sync'ing results in FC from index out
                // of bounds ex
                return -1;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                convertView = m_inflater.inflate(R.layout.list_item, null);
                holder = new ViewHolder();
                holder.taskprio = (TextView) convertView
                        .findViewById(R.id.taskprio);
                holder.tasktext = (TextView) convertView
                        .findViewById(R.id.tasktext);
                holder.taskage = (TextView) convertView
                        .findViewById(R.id.taskage);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Task task = m_adapter.getItem(position);// taskBag.getTasks().get(position);

            if (task != null) {
                holder.taskprio.setText(task.getPriority().inListFormat());
                SpannableString ss = new SpannableString(task.inScreenFormat());
                Util.setGray(ss, task.getProjects());
                Util.setGray(ss, task.getContexts());
                holder.tasktext.setText(ss);

                Resources res = getResources();
                holder.tasktext.setTextColor(res.getColor(R.color.black));

                switch (task.getPriority()) {
                    case A:
                        holder.taskprio.setTextColor(res.getColor(R.color.green));

                        break;
                    case B:
                        holder.taskprio.setTextColor(res.getColor(R.color.blue));

                        break;
                    case C:
                        holder.taskprio.setTextColor(res.getColor(R.color.orange));

                        break;
                    case D:
                        holder.taskprio.setTextColor(res.getColor(R.color.gold));

                        break;
                    default:
                        holder.taskprio.setTextColor(res.getColor(R.color.black));
                }

                if (task.isCompleted()) {
                    // Log.v(TAG, "Striking through " + task.getText());
                    holder.tasktext.setPaintFlags(holder.tasktext
                            .getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    holder.tasktext.setPaintFlags(holder.tasktext
                            .getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                }

                holder.taskage.setVisibility(View.GONE);

                if (m_app.m_prefs.isPrependDateEnabled()) {
                    if (!task.isCompleted()
                            && !Strings.isEmptyOrNull(task.getRelativeAge())) {
                        holder.taskage.setText(task.getRelativeAge());
                        holder.taskage.setVisibility(View.VISIBLE);
                    }
                }
            }

            return convertView;
        }

        public List<Task> getItems() {
            // Make a copy to prevent accidental modification of the adapter.
            ArrayList<Task> tasks = new ArrayList<Task>();

            for (int position = 0; position < this.getCount(); position++) {
                tasks.add(this.getItem(position));
            }

            return tasks;
        }

    }

    private class DrawerItemClickListener implements
            AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            TextView tv = (TextView) view.findViewById(R.id.left_drawer_text);
            String itemTitle = tv.getText().toString();

            Log.v(TAG, "Clicked on drawer " + itemTitle);

            if (itemTitle.substring(0, 1).equals("@")
                    && !m_app.m_contexts.remove(itemTitle.substring(1))) {
                m_app.m_contexts = new ArrayList<String>();
                m_app.m_contexts.add(itemTitle.substring(1));
            } else if (itemTitle.substring(0, 1).equals("+")
                    && !m_app.m_projects.remove(itemTitle.substring(1))) {
                m_app.m_projects = new ArrayList<String>();
                m_app.m_projects.add(itemTitle.substring(1));
            }

            setDrawerChoices();
            m_app.storeFilters();

            if (m_drawerLayout != null) {
                m_drawerLayout.closeDrawer(m_drawerList);
            }

            setFilteredTasks(false);
        }
    }
}
