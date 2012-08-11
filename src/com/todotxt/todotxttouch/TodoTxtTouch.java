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

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.todotxt.todotxttouch.task.FilterFactory;
import com.todotxt.todotxttouch.task.Priority;
import com.todotxt.todotxttouch.task.Sort;
import com.todotxt.todotxttouch.task.Task;
import com.todotxt.todotxttouch.task.TaskBag;
import com.todotxt.todotxttouch.util.Strings;
import com.todotxt.todotxttouch.util.Util;
import com.todotxt.todotxttouch.util.Util.OnMultiChoiceDialogListener;

public class TodoTxtTouch extends ListActivity implements
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

	private int m_pos = Constants.INVALID_POSITION;
	private Sort sort = Sort.PRIORITY_DESC;
	private BroadcastReceiver m_broadcastReceiver;

	private ArrayList<String> m_filters = new ArrayList<String>();

	private static final int SYNC_CHOICE_DIALOG = 100;
	private static final int SYNC_CONFLICT_DIALOG = 101;

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
					updateSyncUI();
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

		// FIXME adapter implements Filterable?
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		getListView().setOnCreateContextMenuListener(this);

		initializeTasks();

		// Show search results
		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			m_search = intent.getStringExtra(SearchManager.QUERY);
			Log.v(TAG, "Searched for " + m_search);
			setFilteredTasks(false);
		}
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
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		this.options_menu = menu;
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuInflater inflater = getMenuInflater();
		AdapterView.AdapterContextMenuInfo menuInfoAdap = (AdapterView.AdapterContextMenuInfo) menuInfo;
		final int pos;
		if (m_pos != Constants.INVALID_POSITION) {
			pos = m_pos;
		} else {
			pos = menuInfoAdap.position;
		}
		final Task task = m_adapter.getItem(pos);
		if (task.isCompleted()) {
			inflater.inflate(R.menu.context_completed, menu);
		} else {
			addVariableMenuItems(menu, task);
			inflater.inflate(R.menu.main_long, menu);
		}
	}

	/**
	 * Adds menu items to the context menu depending on the tasks content
	 * 
	 * @param menu
	 *            The menu to add items to
	 * @param task
	 *            The task this menu is created for
	 */
	private void addVariableMenuItems(ContextMenu menu, final Task task) {
		ListIterator<URL> i = task.getLinks().listIterator();
		while (i.hasNext()) {
			menu.add(Menu.NONE, R.id.url, Menu.NONE, i.next().toString());
		}
		ListIterator<String> j = task.getMailAddresses().listIterator();
		while (j.hasNext()) {
			menu.add(Menu.NONE, R.id.mail, Menu.NONE, j.next());
		}
		ListIterator<String> i3 = task.getPhoneNumbers().listIterator();
		while (i3.hasNext()) {
			menu.add(Menu.NONE, R.id.phone_number, Menu.NONE, i3.next());
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Log.v(TAG, "onContextItemSelected");
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		int menuid = item.getItemId();
		final int pos;
		if (m_pos != Constants.INVALID_POSITION) {
			pos = m_pos;
			m_pos = Constants.INVALID_POSITION;
		} else {
			pos = menuInfo.position;
		}
		if (menuid == R.id.update) {
			Log.v(TAG, "update");
			editTaskAt(pos);
		} else if (menuid == R.id.delete) {
			Log.v(TAG, "delete");
			deleteTaskAt(pos);
		} else if (menuid == R.id.done) {
			Log.v(TAG, "done");
			completeTaskAt(pos);
		} else if (menuid == R.id.unComplete) {
			Log.v(TAG, "undo Complete");
			undoCompleteTaskAt(pos);
		} else if (menuid == R.id.priority) {
			Log.v(TAG, "priority");
			prioritizeTaskAt(pos);
		} else if (menuid == R.id.share) {
			Log.v(TAG, "share");
			shareTaskAt(pos);
		} else if (menuid == R.id.url) {
			Log.v(TAG, "url: " + item.getTitle().toString());
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getTitle()
					.toString()));
			startActivity(i);
		} else if (menuid == R.id.mail) {
			Log.v(TAG, "mail: " + item.getTitle().toString());
			Intent i = new Intent(Intent.ACTION_SEND, Uri.parse(item.getTitle()
					.toString()));
			i.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { item
					.getTitle().toString() });
			i.setType("text/plain");
		} else if (menuid == R.id.phone_number) {
			Log.v(TAG, "phone_number");
			Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
					+ item.getTitle().toString()));
			startActivity(i);
		}

		return super.onContextItemSelected(item);
	}

	private void shareTaskAt(final int pos) {
		final Task task = m_adapter.getItem(pos);

		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				"Todo.txt task");
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, task.getText());

		startActivity(Intent.createChooser(shareIntent, "Share"));
	}

	private void prioritizeTaskAt(final int pos) {
		final String[] prioArr = Priority
				.rangeInCode(Priority.NONE, Priority.E).toArray(new String[0]);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select priority");
		builder.setSingleChoiceItems(prioArr, 0, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, final int which) {
				final Task task = m_adapter.getItem(pos);
				dialog.dismiss();
				new AsyncTask<Object, Void, Boolean>() {
					protected void onPreExecute() {
						m_ProgressDialog = showProgressDialog("Prioritizing Task");
					}

					@Override
					protected Boolean doInBackground(Object... params) {
						try {
							Task task = (Task) params[0];
							String[] prioArr = (String[]) params[1];
							int which = (Integer) params[2];
							task.setPriority(Priority
									.toPriority(prioArr[which]));
							taskBag.update(task);
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
							Util.showToastLong(TodoTxtTouch.this,
									"Prioritized task " + task.getText());
							sendBroadcast(new Intent(
									Constants.INTENT_START_SYNC_TO_REMOTE));
						} else {
							Util.showToastLong(
									TodoTxtTouch.this,
									"Could not prioritize task "
											+ task.inFileFormat());
						}
					}
				}.execute(task, prioArr, which);
			}
		});
		builder.show();
	}

	private void undoCompleteTaskAt(final int pos) {
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final Task task = m_adapter.getItem(pos);
				new AsyncTask<Object, Void, Boolean>() {
					protected void onPreExecute() {
						m_ProgressDialog = showProgressDialog("Marking Task Not Complete");
					}

					@Override
					protected Boolean doInBackground(Object... params) {
						try {
							Task task = (Task) params[0];
							task.markIncomplete();
							taskBag.update(task);
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
							Util.showToastLong(TodoTxtTouch.this,
									"Task marked as not completed");
							sendBroadcast(new Intent(
									Constants.INTENT_START_SYNC_TO_REMOTE));
						} else {
							Util.showToastLong(TodoTxtTouch.this,
									"Could not mark task as not completed");
						}
					}
				}.execute(task);
			}
		};
		Util.showConfirmationDialog(this, R.string.areyousure, listener);
	}

	private void completeTaskAt(final int pos) {
		final Task task = m_adapter.getItem(pos);
		if (task.isCompleted()) {
			Util.showToastLong(TodoTxtTouch.this, "Task already complete");
		} else {
			task.markComplete(new Date());
			// Log.v(TAG, "Completing task with this text: " + task.getText());
			new AsyncTask<Object, Void, Boolean>() {

				protected void onPreExecute() {
					m_ProgressDialog = showProgressDialog("Marking Task Complete");
				}

				@Override
				protected Boolean doInBackground(Object... params) {
					try {
						Task task = (Task) params[0];
						task.markComplete(new Date());
						taskBag.update(task);
						if (m_app.m_prefs.getBoolean("todotxtautoarchive",
								false)) {
							taskBag.archive();
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
						Util.showToastLong(TodoTxtTouch.this, "Completed task "
								+ task.inFileFormat());
						sendBroadcast(new Intent(
								Constants.INTENT_START_SYNC_TO_REMOTE));
					} else {
						Util.showToastLong(
								TodoTxtTouch.this,
								"Could not complete task "
										+ task.inFileFormat());
					}
				}
			}.execute(task);
		}
	}

	private void editTaskAt(final int pos) {
		final Task backup = m_adapter.getItem(pos);
		Intent intent = new Intent(this, AddTask.class);
		intent.putExtra(Constants.EXTRA_TASK, (Serializable) backup);
		startActivity(intent);
	}

	private void deleteTaskAt(final int pos) {
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final Task task = m_adapter.getItem(pos);

				new AsyncTask<Object, Void, Boolean>() {

					protected void onPreExecute() {
						m_ProgressDialog = showProgressDialog("Deleting");
					}

					@Override
					protected Boolean doInBackground(Object... params) {
						try {
							taskBag.delete((Task) params[0]);
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
							Util.showToastLong(TodoTxtTouch.this,
									"Deleted task " + task.inFileFormat());
							sendBroadcast(new Intent(
									Constants.INTENT_START_SYNC_TO_REMOTE));
						} else {
							Util.showToastLong(
									TodoTxtTouch.this,
									"Could not delete task "
											+ task.inFileFormat());
						}
					}
				}.execute(task);
			}
		};
		Util.showDeleteConfirmationDialog(this, listener);
	}

	private void archiveTasks() {
		new AsyncTask<Void, Void, Boolean>() {

			protected void onPreExecute() {
				m_ProgressDialog = showProgressDialog("Archiving Tasks");
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
							"Archived completed tasks");
					sendBroadcast(new Intent(
							Constants.INTENT_START_SYNC_TO_REMOTE));
				} else {
					Util.showToastLong(TodoTxtTouch.this,
							"Could not archive tasks");
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
		default:
			return super.onMenuItemSelected(featureId, item);
		}
		return true;
	}

	private void startAddTaskActivity() {
		Intent intent = new Intent(this, AddTask.class);
		startActivity(intent);
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
	private void handleSyncConflict() {
		m_app.m_pushing = false;
		m_app.m_pulling = false;
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
				message, "Please wait...", true));
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

	/** Handle refine filter click **/
	public void onRefineClick(View v) {
		startFilterActivity();
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

	void clearFilter() {
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
		m_pos = position;
		boolean completeOnShort = m_app.m_prefs.getBoolean(
				getString(R.string.short_touch_pref_key), false);
		if (completeOnShort) {
			completeTaskAt(position);
		} else {
			openContextMenu(getListView());
		}
	}

	private void updateSyncUI() {
		// hide action bar
		findViewById(R.id.actionbar).setVisibility(View.GONE);
		// hide refresh button
		findViewById(R.id.btn_title_refresh).setVisibility(
				m_app.m_pulling || m_app.m_pushing ? View.GONE : View.VISIBLE);
		// show moving refresh indicator
		findViewById(R.id.title_refresh_progress).setVisibility(
				m_app.m_pulling || m_app.m_pushing ? View.VISIBLE : View.GONE);
		setFilteredTasks(false);
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

				if (m_app.m_prefs.getBoolean("todotxtprependdate", true)) {
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
		Intent i = new Intent(this, Filter.class);

		i.putStringArrayListExtra(Constants.EXTRA_PRIORITIES,
				Priority.inCode(taskBag.getPriorities()));
		i.putStringArrayListExtra(Constants.EXTRA_PROJECTS,
				taskBag.getProjects());
		i.putStringArrayListExtra(Constants.EXTRA_CONTEXTS,
				taskBag.getContexts());

		i.putStringArrayListExtra(Constants.EXTRA_PRIORITIES_SELECTED,
				Priority.inCode(m_prios));
		i.putStringArrayListExtra(Constants.EXTRA_PROJECTS_SELECTED, m_projects);
		i.putStringArrayListExtra(Constants.EXTRA_CONTEXTS_SELECTED, m_contexts);
		i.putExtra(Constants.EXTRA_SEARCH, m_search);

		startActivityIfNeeded(i, REQUEST_FILTER);
	}

}
