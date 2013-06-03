/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2013 Todo.txt contributors (http://todotxt.com)
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
 * @copyright 2009-2013 Todo.txt contributors (http://todotxt.com)
 */
package com.todotxt.todotxttouch;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
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
import com.todotxt.todotxttouch.util.Strings;
import com.todotxt.todotxttouch.util.Util;
import com.todotxt.todotxttouch.util.Util.OnMultiChoiceDialogListener;

public class TodoTxtTouch extends SherlockListActivity implements
		OnSharedPreferenceChangeListener {

	final static String TAG = TodoTxtTouch.class.getSimpleName();

	private final static int REQUEST_FILTER = 1;
	private final static int REQUEST_PREFERENCES = 2;
	private final static int REQUEST_LOGIN = 3;

	private static TodoTxtTouch currentActivityPointer = null;

	private TaskBag taskBag;
	ProgressDialog m_ProgressDialog = null;
	String m_DialogText = "";
	Boolean m_DialogActive = false;
	Menu options_menu;
	private Boolean wasOffline = false;

	private TaskAdapter m_adapter;
	TodoApplication m_app;

	// filter variables
	private ArrayList<Priority> m_prios = new ArrayList<Priority>();
	private ArrayList<String> m_contexts = new ArrayList<String>();
	private ArrayList<String> m_projects = new ArrayList<String>();
	private String m_search;

	private Sort sort = Sort.PRIORITY_DESC;
	private BroadcastReceiver m_broadcastReceiver;

	private ArrayList<String> m_filters = new ArrayList<String>();

	private static final int SYNC_CHOICE_DIALOG = 100;
	private static final int SYNC_CONFLICT_DIALOG = 101;

	private GestureDetector gestureDetector;
	private View.OnTouchListener gestureListener;

	private ActionMode mMode;

	@SuppressWarnings("deprecation")
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
			}
		};
		registerReceiver(m_broadcastReceiver, intentFilter);

		setListAdapter(this.m_adapter);

		ListView lv = getListView();

		lv.setTextFilterEnabled(true);
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		initializeTasks();

		// Show search results
		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			m_search = intent.getStringExtra(SearchManager.QUERY);
			Log.v(TAG, "Searched for " + m_search);
			setFilteredTasks(false);
		}

		gestureDetector = new GestureDetector(new TodoTxtGestureDetector());
		gestureListener = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event)) {
					MotionEvent cancelEvent = MotionEvent.obtain(event);
					cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
					v.onTouchEvent(cancelEvent);
					cancelEvent.recycle();
					return true;
				}
				return false;
			}
		};

		getListView().setOnTouchListener(gestureListener);

	}

	private void initializeTasks() {
		boolean firstrun = m_app.m_prefs.getBoolean(Constants.PREF_FIRSTRUN,
				true);

		if (firstrun) {
			Log.i(TAG, "Initializing app");
			syncClient(true);
			Editor editor = m_app.m_prefs.edit();
			editor.putBoolean(Constants.PREF_FIRSTRUN, false);
			editor.commit();
		} else {
			if (!isManualMode()) {
				syncClient(false);
			}
			taskBag.reload();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		m_app.m_prefs.unregisterOnSharedPreferenceChangeListener(this);
		unregisterReceiver(m_broadcastReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();

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
		if (Constants.PREF_ACCESSTOKEN_SECRET.equals(key)) {
			Log.i(TAG, "New access token secret. Syncing!");
			syncClient(false);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("sort", sort.getId());
		outState.putBoolean("DialogActive", m_DialogActive);
		outState.putString("DialogText", m_DialogText);

		outState.putStringArrayList("m_prios", Priority.inCode(m_prios));
		outState.putStringArrayList("m_contexts", m_contexts);
		outState.putStringArrayList("m_projects", m_projects);
		outState.putStringArrayList("m_filters", m_filters);
		outState.putString("m_search", m_search);

		dismissProgressDialog(false);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		sort = Sort.getById(state.getInt("sort"));
		m_DialogActive = state.getBoolean("DialogActive");
		m_DialogText = state.getString("DialogText");
		if (m_DialogActive) {
			showProgressDialog(m_DialogText);
		}

		m_prios = Priority.toPriority(state.getStringArrayList("m_prios"));
		m_contexts = state.getStringArrayList("m_contexts");
		m_projects = state.getStringArrayList("m_projects");
		m_filters = state.getStringArrayList("m_filters");
		m_search = state.getString("m_search");
		setFilteredTasks(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main, menu);
		this.options_menu = menu;
		return super.onCreateOptionsMenu(menu);
	}

	private void shareTasks(List<Task> tasks) {
		String shareText = "";
		for (Task t : tasks) {
			shareText += t.inFileFormat() + "\n";
		}
		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				"Todo.txt task");
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);

		startActivity(Intent.createChooser(shareIntent, "Share"));
	}

	private void prioritizeTasks(final ArrayList<Task> tasks) {
		final String[] prioArr = Priority
				.rangeInCode(Priority.NONE, Priority.E).toArray(new String[0]);
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

	private void undoCompleteTasks(final ArrayList<Task> tasks) {
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				new AsyncTask<Object, Void, Boolean>() {
					protected void onPreExecute() {
						m_ProgressDialog = showProgressDialog(getString(R.string.progress_uncomplete));
					}

					@Override
					protected Boolean doInBackground(Object... params) {
						try {
							@SuppressWarnings("unchecked")
							ArrayList<Task> tasks = (ArrayList<Task>) params[0];
							for (Task task : tasks) {
								task.markIncomplete();
								taskBag.update(task);
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
		Util.showConfirmationDialog(this, R.string.areyousure, listener,
				R.string.unComplete);
	}

	private void completeTasks(ArrayList<Task> tasks) {
		// Log.v(TAG, "Completing task with this text: " + task.getText());
		new AsyncTask<Object, Void, Boolean>() {

			protected void onPreExecute() {
				m_ProgressDialog = showProgressDialog(getString(R.string.progress_complete));
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
					if (m_app.m_prefs.getBoolean("todotxtautoarchive", false)) {
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

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.v(TAG, "onMenuItemSelected: " + item.getItemId());
		switch (item.getItemId()) {
		case R.id.add_new:
			startAddTaskActivity();
			break;
		case R.id.sync:
			Log.v(TAG, "onMenuItemSelected: sync");
			syncClient(false);
			break;
		case R.id.search:
			onSearchRequested();
			break;
		case R.id.preferences:
			startPreferencesActivity();
			break;
		case R.id.filter:
			startFilterActivity();
			break;
		case R.id.sort:
			startSortDialog();
			break;
		case R.id.share:
			shareTasks(m_adapter.getItems());
			break;
		case R.id.quickfilter:
			quickFilter();
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

	private void quickFilter() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
		if (filterItems.size() == 0) {
			showToast(R.string.nocontextsprojectsfilter);
			return;
		}

		builder.setItems(filterItems.toArray(new String[0]),
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int which) {
						// filtering on context or project?
						String itemTitle = filterItems.get(which);
						if (itemTitle.substring(0, 1).equals("@")) {
							m_contexts = new ArrayList<String>();
							m_contexts.add(itemTitle.substring(1));
							if (!m_filters
									.contains(getString(R.string.filter_tab_contexts))) {
								m_filters
										.add(getString(R.string.filter_tab_contexts));
							}
						} else {
							m_projects = new ArrayList<String>();
							m_projects.add(itemTitle.substring(1));
							if (!m_filters
									.contains(getString(R.string.filter_tab_projects))) {
								m_filters
										.add(getString(R.string.filter_tab_projects));
							}
						}

						setFilteredTasks(false);
					}
				});

		// Create the AlertDialog
		AlertDialog dialog = builder.create();
		dialog.setTitle(R.string.filterbycontextproject);
		dialog.show();
	}

	private void startSortDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setSingleChoiceItems(R.array.sort, sort.getId(),
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.v(TAG, "onClick " + which);
						sort = Sort.getById(which);
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
			if (!isManualMode() && wasOffline) {
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
	 * 
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
		if (isManualMode()) {
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

	private boolean isManualMode() {
		return m_app.isManualMode();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.v(TAG, "onActivityResult: resultCode=" + resultCode + " i=" + data);
		if (requestCode == REQUEST_FILTER) {
			if (resultCode == Activity.RESULT_OK) {
				m_prios = Priority.toPriority(data
						.getStringArrayListExtra(Constants.EXTRA_PRIORITIES));
				m_projects = data
						.getStringArrayListExtra(Constants.EXTRA_PROJECTS);
				m_contexts = data
						.getStringArrayListExtra(Constants.EXTRA_CONTEXTS);
				m_search = data.getStringExtra(Constants.EXTRA_SEARCH);
				m_filters = data
						.getStringArrayListExtra(Constants.EXTRA_APPLIED_FILTERS);
				setFilteredTasks(false);
			}
		} else if (requestCode == REQUEST_PREFERENCES) {
			if (resultCode == Preferences.RESULT_SYNC_LIST) {
				initializeTasks();
			}
		} else if (requestCode == REQUEST_LOGIN) {

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
	protected Dialog onCreateDialog(int id) {
		final Dialog d;

		if (R.id.priority == id) {
			final List<Priority> pStrs = taskBag.getPriorities();
			int size = pStrs.size();
			boolean[] values = new boolean[size];
			for (Priority prio : m_prios) {
				int index = pStrs.indexOf(prio);
				if (index != -1) {
					values[index] = true;
				}
			}
			d = Util.createMultiChoiceDialog(this,
					pStrs.toArray(new String[size]), values, null, null,
					new OnMultiChoiceDialogListener() {
						@SuppressWarnings("deprecation")
						@Override
						public void onClick(boolean[] selected) {
							m_prios.clear();
							for (int i = 0; i < selected.length; i++) {
								if (selected[i]) {
									m_prios.add(pStrs.get(i));
								}
							}
							setFilteredTasks(false);
							removeDialog(R.id.priority);
						}
					});
			d.setOnCancelListener(new OnCancelListener() {
				@SuppressWarnings("deprecation")
				@Override
				public void onCancel(DialogInterface dialog) {
					removeDialog(R.id.priority);
				}
			});
			return d;
		} else if (id == SYNC_CHOICE_DIALOG) {
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
			return upDownChoice.show();
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
			return upDownChoice.show();
		} else {
			return null;
		}
	}

	/** Handle "add task" action. */
	public void onAddTaskClick(View v) {
		Intent i = new Intent(this, AddTask.class);

		i.putExtra(Constants.EXTRA_PRIORITIES_SELECTED, m_prios);
		i.putExtra(Constants.EXTRA_CONTEXTS_SELECTED, m_contexts);
		i.putExtra(Constants.EXTRA_PROJECTS_SELECTED, m_projects);

		startActivity(i);
	}

	/** Handle "refresh/download" action. */
	public void onSyncClick(View v) {
		Log.v(TAG, "titlebar: sync");
		syncClient(false);
	}

	/** Handle clear filter click **/
	public void onClearClick(View v) {
		// End current activity if it's search results
		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			finish();
		} else { // otherwise just clear the filter in the current activity
			clearFilter();
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

	void showContextActionBarIfNeeded() {
		ArrayList<Task> checkedTasks = getCheckedTasks();
		int checkedCount = checkedTasks.size();
		if (mMode != null && checkedCount == 0) {
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
						completeTasks(checkedTasks);
						break;
					case R.id.priority:
						prioritizeTasks(checkedTasks);
						break;
					case R.id.share:
						shareTasks(checkedTasks);
						break;
					case R.id.uncomplete:
						undoCompleteTasks(checkedTasks);
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
								new String[] { item.getTitle().toString() });
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
		if (mMode != null) {
			mMode.finish();
		}
		m_prios = new ArrayList<Priority>(); // Collections.emptyList();
		m_contexts = new ArrayList<String>(); // Collections.emptyList();
		m_projects = new ArrayList<String>(); // Collections.emptyList();
		m_filters = new ArrayList<String>();
		m_search = "";
	}

	void setFilteredTasks(boolean reload) {
		if (reload) {
			try {
				taskBag.reload();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}

		ListView lv = getListView();
		int index = lv.getFirstVisiblePosition();
		View v = lv.getChildAt(0);
		int top = (v == null) ? 0 : v.getTop();
		Log.v(TAG, "ListView index " + index + " top " + top);

		m_adapter.clear();
		for (Task task : taskBag.getTasks(FilterFactory.generateAndFilter(
				m_prios, m_contexts, m_projects, m_search, false), sort
				.getComparator())) {
			m_adapter.add(task);
		}

		lv.setSelectionFromTop(index, top);

		final TextView filterText = (TextView) findViewById(R.id.filter_text);
		final LinearLayout actionbar = (LinearLayout) findViewById(R.id.actionbar);
		final ImageView actionbar_icon = (ImageView) findViewById(R.id.actionbar_icon);

		if (filterText != null) {
			if (m_filters.size() > 0) {
				String filterTitle = getString(R.string.title_filter_applied)
						+ " ";
				int count = m_filters.size();
				for (int i = 0; i < count; i++) {
					filterTitle += m_filters.get(i) + " ";
				}
				if (!Strings.isEmptyOrNull(m_search)) {
					filterTitle += "Keyword";
				}
				actionbar_icon.setImageResource(R.drawable.ic_actionbar_filter);

				actionbar.setVisibility(View.VISIBLE);
				filterText.setText(filterTitle);

			} else if (!Strings.isEmptyOrNull(m_search)) {
				if (filterText != null) {

					actionbar_icon
							.setImageResource(R.drawable.ic_actionbar_search);
					filterText.setText(getString(R.string.title_search_results)
							+ " " + m_search);

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

		View progress = getLayoutInflater().inflate(R.layout.main_progress,
				null);

		// options_menu can be null here because we can sync before the menu
		// has been drawn
		if (progress != null && options_menu != null) {
			MenuItem refreshMenu = options_menu.findItem(R.id.sync);
			int currentapiVersion = android.os.Build.VERSION.SDK_INT;
			// Using indeterminate progress looks bad on Gingerbread and below
			if (currentapiVersion >= android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
				if (m_app.syncInProgress()) {
					refreshMenu.setActionView(progress);
				} else {
					refreshMenu.setActionView(null);
				}
			} else {
				if (m_app.syncInProgress()) {
					refreshMenu.setEnabled(false);
				} else {
					refreshMenu.setEnabled(true);
				}
			}

		}

		if (redrawList) {
			// hide action bar
			findViewById(R.id.actionbar).setVisibility(View.GONE);
			setFilteredTasks(false);
		}
	}

	public class TaskAdapter extends ArrayAdapter<Task> {
		private List<Task> items;
		private LayoutInflater m_inflater;

		public TaskAdapter(Context context, int textViewResourceId,
				List<Task> tasks, LayoutInflater inflater) {
			super(context, textViewResourceId, tasks);
			this.items = tasks;
			this.m_inflater = inflater;
		}

		@Override
		public Filter getFilter() {
			return new Filter() {

				@Override
				protected FilterResults performFiltering(CharSequence search) {
					m_search = search.toString();
					return null;
				}

				@Override
				protected void publishResults(CharSequence arg0,
						FilterResults arg1) {
					setFilteredTasks(false);
				}

			};
		}

		@Override
		public void clear() {
			super.clear();
			items.clear();
		}

		@Override
		public long getItemId(int position) {
			if (!items.isEmpty()) {
				return items.get(position).getId();
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
				if (m_app.m_prefs.getBoolean("todotxtprependdate", false)) {
					if (!task.isCompleted()
							&& !Strings.isEmptyOrNull(task.getRelativeAge())) {
						holder.taskage.setText(task.getRelativeAge());
						holder.taskage.setVisibility(View.VISIBLE);
					} else {
						holder.tasktext.setPadding(
								holder.tasktext.getPaddingLeft(),
								holder.tasktext.getPaddingTop(),
								holder.tasktext.getPaddingRight(), 4);
					}
				} else {
					holder.tasktext.setPadding(
							holder.tasktext.getPaddingLeft(),
							holder.tasktext.getPaddingTop(),
							holder.tasktext.getPaddingRight(), 4);
				}
			}
			return convertView;
		}

		public List<Task> getItems() {
			// Make a copy to prevent accidental modification of the adapter.
			ArrayList<Task> tasks = new ArrayList<Task>();
			tasks.addAll(items);
			return tasks;
		}
	}

	private static class ViewHolder {
		private TextView taskprio;
		private TextView tasktext;
		private TextView taskage;
	}

	public void storeKeys(String accessTokenKey, String accessTokenSecret) {
		Editor editor = m_app.m_prefs.edit();
		editor.putString(Constants.PREF_ACCESSTOKEN_KEY, accessTokenKey);
		editor.putString(Constants.PREF_ACCESSTOKEN_SECRET, accessTokenSecret);
		editor.commit();
	}

	public void showToast(String string) {
		Util.showToastLong(this, string);
	}

	public void showToast(int resid) {
		Util.showToastLong(this, resid);
	}

	public void startFilterActivity() {
		Intent i = new Intent(this, FilterActivity.class);

		i.putStringArrayListExtra(Constants.EXTRA_PRIORITIES,
				Priority.inCode(taskBag.getPriorities()));
		i.putStringArrayListExtra(Constants.EXTRA_PROJECTS,
				taskBag.getProjects(true));
		i.putStringArrayListExtra(Constants.EXTRA_CONTEXTS,
				taskBag.getContexts(true));

		i.putStringArrayListExtra(Constants.EXTRA_PRIORITIES_SELECTED,
				Priority.inCode(m_prios));
		i.putStringArrayListExtra(Constants.EXTRA_PROJECTS_SELECTED, m_projects);
		i.putStringArrayListExtra(Constants.EXTRA_CONTEXTS_SELECTED, m_contexts);
		i.putExtra(Constants.EXTRA_SEARCH, m_search);

		startActivityIfNeeded(i, REQUEST_FILTER);
	}

	class TodoTxtGestureDetector extends SimpleOnGestureListener {
		private static final int SWIPE_MIN_DISTANCE = 120;
		private static final int SWIPE_MAX_OFF_PATH = 250;
		private static final int SWIPE_THRESHOLD_VELOCITY = 200;

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				return false;

			ListView lv = getListView();
			int pos = lv.pointToPosition((int) e1.getX(), (int) e1.getY());

			// right to left swipe - mark task complete
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				Log.v(TAG, "Fling left");
				// if task is complete, undo complete
				final Task task = m_adapter.getItem(pos);
				if (task.isCompleted()) {
					ArrayList<Task> tasks = new ArrayList<Task>();
					tasks.add(task);
					undoCompleteTasks(tasks);
				}
				return true;
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				// left to right swipe - uncomplete task
				Log.v(TAG, "Fling right");
				// if task is incomplete, mark as complete
				final Task task = m_adapter.getItem(pos);
				if (!task.isCompleted()) {
					ArrayList<Task> tasks = new ArrayList<Task>();
					tasks.add(task);
					completeTasks(tasks);
				}
				return true;
			}
			return false;
		}
	}
}
