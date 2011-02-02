/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/TodoTxtTouch.java
 *
 * Copyright (c) 2009-2011 Gina Trapani, mathias, Stephen Henderson, Tormod Haugen, shanest
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
 * @author mathias <mathias[at]x2[dot](none)>
 * @author Stephen Henderson <me[at]steveh[dot]ca>
 * @author mathias <mathias[at]ws7862[dot](none)>
 * @author Tormod Haugen <tormodh[at]gmail[dot]com>
 * @author shanest <ssshanest[at]gmail[dot]com>
 * @author Adam Zaloudek <AdamZaloudek[at]hotmail[dot]com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 Gina Trapani, mathias, Stephen Henderson, Tormod Haugen, shanest, Adam Zaloudek
 */
package com.todotxt.todotxttouch;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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

import com.dropbox.client.DropboxAPI;
import com.dropbox.client.DropboxAPI.Config;
import com.todotxt.todotxttouch.Util.OnMultiChoiceDialogListener;
import com.todotxt.todotxttouch.task.Task;

public class TodoTxtTouch extends ListActivity implements
		OnSharedPreferenceChangeListener {

	final static String TAG = TodoTxtTouch.class.getSimpleName();

	private final static String INTENT_ACTION_LOGOUT = "com.todotxt.todotxttouch.ACTION_LOGOUT";
	private final static String INTENT_ASYNC_SUCCESS = "com.todotxt.todotxttouch.ASYNC_SUCCESS";
	private final static String INTENT_ASYNC_FAILED = "com.todotxt.todotxttouch.ASYNC_FAILED";

	private final static int SORT_PRIO = 0;
	private final static int SORT_ID = 1;
	private final static int SORT_ID_REVERSE = 2;
	private final static int SORT_TEXT = 3;

	private final static int REQUEST_FILTER = 1;
	private final static int REQUEST_PREFERENCES = 2;
	private final static int REQUEST_LOGIN = 3;

	private static TodoTxtTouch currentActivityPointer = null;

	ProgressDialog m_ProgressDialog = null;
	String m_DialogText = "";
	Boolean m_DialogActive = false;

	ArrayList<Task> m_tasks = new ArrayList<Task>();
	private TaskAdapter m_adapter;
	TodoApplication m_app;

	// filter variables
	private ArrayList<String> m_prios = new ArrayList<String>();
	private ArrayList<String> m_contexts = new ArrayList<String>();
	private ArrayList<String> m_projects = new ArrayList<String>();
	private String m_search;

	private int m_pos = Constants.INVALID_POSITION;
	private int m_sort = SORT_PRIO;
	private BroadcastReceiver m_broadcastReceiver;

	private ArrayList<String> m_filters = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentActivityPointer = this;

		// final boolean customTitleSupported =
		// requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.main);
		/*
		 * if (customTitleSupported) {
		 * getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
		 * R.layout.title_bar); }
		 */
		m_app = (TodoApplication) getApplication();
		m_app.m_prefs.registerOnSharedPreferenceChangeListener(this);
		m_adapter = new TaskAdapter(this, R.layout.list_item, m_tasks,
				getLayoutInflater());

		// listen to the ACTION_LOGOUT intent, if heard display LoginScreen
		// and finish() current activity
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(INTENT_ACTION_LOGOUT);
		intentFilter.addAction(INTENT_ASYNC_SUCCESS);
		intentFilter.addAction(INTENT_ASYNC_FAILED);

		m_broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equalsIgnoreCase(INTENT_ACTION_LOGOUT)) {
					Intent i = new Intent(context, LoginScreen.class);
					startActivity(i);
					finish();
				} else if (intent.getAction().equalsIgnoreCase(
						INTENT_ASYNC_SUCCESS)
						|| intent.getAction().equalsIgnoreCase(
								INTENT_ASYNC_FAILED)) {

					m_app.m_syncing = false;
					updateRefreshStatus();
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
			Editor editor = m_app.m_prefs.edit();
			editor.putBoolean(Constants.PREF_FIRSTRUN, false);
			editor.commit();
			try {
				if (!Constants.TODOFILE.exists()) {
					Util.createParentDirectory(Constants.TODOFILE);
					Constants.TODOFILE.createNewFile();
				}
			} catch (Exception e) {
				Log.e(TAG, "Error creating local files", e);
			}

			populateFromExternal();
		} else {
			populateFromFile();
		}
	}

	void showLogin() {
		// Intent i = new Intent(getBaseContext(), LoginScreen.class);
		// startActivityForResult(i, REQUEST_LOGIN);
		Intent settingsActivity = new Intent(getBaseContext(),
				Preferences.class);
		startActivityForResult(settingsActivity, REQUEST_PREFERENCES);
	}

	void login() {

		final DropboxAPI api = getAPI();
		if (api.isAuthenticated() && !m_app.m_loggedIn) {
			DropboxLoginAsyncTask loginTask = new DropboxLoginAsyncTask(m_app,
					m_app.getConfig());
			loginTask.execute();
		} else {
			DropboxLoginAsyncTask loginTask = new DropboxLoginAsyncTask(m_app,
					m_app.getConfig());
			loginTask.showLoginDialog(this);
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
			populateFromExternal();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("sort", m_sort);
		outState.putBoolean("DialogActive", m_DialogActive);
		outState.putString("DialogText", m_DialogText);
		dismissProgressDialog(false);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		m_sort = state.getInt("sort");
		m_DialogActive = state.getBoolean("DialogActive");
		m_DialogText = state.getString("DialogText");
		if (m_DialogActive) {
			showProgressDialog(m_DialogText);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
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
			inflater.inflate(R.menu.main_long, menu);
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
			final Task backup = m_adapter.getItem(pos);
			Intent intent = new Intent(this, AddTask.class);
			intent.putExtra(Constants.EXTRA_TASK, (Serializable) backup);
			startActivity(intent);
		} else if (menuid == R.id.delete) {
			Log.v(TAG, "delete");
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
								TodoApplication m_app = (TodoApplication) params[0];
								Task task = (Task) params[1];
								DropboxAPI api = m_app.getAPI();
								if (api != null) {
									task.delete();
									return m_app.m_util.updateTask(task);
								}
							} catch (Exception e) {
								Log.e(TAG, e.getMessage(), e);
							}
							return false;
						}

						protected void onPostExecute(Boolean result) {
							TodoTxtTouch.currentActivityPointer
									.dismissProgressDialog(true);
							if (result) {
								Util.showToastLong(TodoTxtTouch.this,
										"Deleted task " + task.inFileFormat());
							} else {
								Util.showToastLong(
										TodoTxtTouch.this,
										"Could not delete task "
												+ task.inFileFormat());
							}
						}
					}.execute(m_app, task);
				}
			};
			Util.showDeleteConfirmationDialog(this, listener);
		} else if (menuid == R.id.done) {
			Log.v(TAG, "done");

			final Task task = m_adapter.getItem(pos);
			if (task.isCompleted()) {
				Util.showToastLong(TodoTxtTouch.this, "Task already complete");
			} else {
				task.markComplete(new Date());
				Log.v(TAG, "Completing task with this text: " + task.getText());
				new AsyncTask<Object, Void, Boolean>() {

					protected void onPreExecute() {
						m_ProgressDialog = showProgressDialog("Marking Task Complete");
					}

					@Override
					protected Boolean doInBackground(Object... params) {

						try {
							TodoApplication m_app = (TodoApplication) params[0];
							Task task = (Task) params[1];
							task.markComplete(new Date());
							return m_app.m_util.updateTask(task);

						} catch (Exception e) {
							Log.e(TAG, e.getMessage(), e);
						}
						return false;
					}

					protected void onPostExecute(Boolean result) {
						TodoTxtTouch.currentActivityPointer
								.dismissProgressDialog(true);
						if (result) {
							Util.showToastLong(TodoTxtTouch.this,
									"Completed task " + task.inFileFormat());
						} else {
							Util.showToastLong(
									TodoTxtTouch.this,
									"Could not complete task "
											+ task.inFileFormat());
						}
					}
				}.execute(m_app, task);
			}
		} else if (menuid == R.id.unComplete) {
			Log.v(TAG, "undo Complete");
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
								if (!task.isCompleted()) {
									return true;
								} else {
									TodoApplication m_app = (TodoApplication) params[0];
									Task task = (Task) params[1];
									Log.i(TAG, "pre-mark incomplete{" + task
											+ "}");
									task.markIncomplete();
									Log.i(TAG, "marked incomplete {" + task
											+ "}");
									return m_app.m_util.updateTask(task);
								}
							} catch (Exception e) {
								Log.e(TAG, e.getMessage(), e);
							}
							return false;
						}

						protected void onPostExecute(Boolean result) {
							TodoTxtTouch.currentActivityPointer
									.dismissProgressDialog(true);
							if (result) {
								Util.showToastLong(TodoTxtTouch.this,
										"Task marked as not completed");
							} else {
								Util.showToastLong(TodoTxtTouch.this,
										"Could not mark task as not completed");
							}
						}
					}.execute(m_app, task);
				}
			};
			Util.showConfirmationDialog(this, R.string.areyousure, listener);
		} else if (menuid == R.id.priority) {
			Log.v(TAG, "priority");
			final String[] prioArr = { "" + Task.NO_PRIORITY, "A", "B", "C",
					"D", "E" };
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
								TodoApplication m_app = (TodoApplication) params[0];
								Task task = (Task) params[1];
								String[] prioArr = (String[]) params[2];
								int which = (Integer) params[3];
								task.setPriority(prioArr[which].charAt(0));
								DropboxAPI api = m_app.getAPI();
								if (api != null) {
									return m_app.m_util.updateTask(task);
								}
							} catch (Exception e) {
								Log.e(TAG, e.getMessage(), e);
							}
							return false;
						}

						protected void onPostExecute(Boolean result) {
							TodoTxtTouch.currentActivityPointer
									.dismissProgressDialog(true);
							if (result) {
								Util.showToastLong(TodoTxtTouch.this,
										"Prioritized task " + task.getText());
							} else {
								Util.showToastLong(
										TodoTxtTouch.this,
										"Could not prioritize task "
												+ task.inFileFormat());
							}
						}
					}.execute(m_app, task, prioArr, which);
				}
			});
			builder.show();
		} else if (menuid == R.id.share) {
			Log.v(TAG, "share");
			final Task task = m_adapter.getItem(pos);

			Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					"Todo.txt task");
			shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
					task.getText());

			startActivity(Intent.createChooser(shareIntent, "Share"));
		}

		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.v(TAG, "onMenuItemSelected: " + item.getItemId());
		switch (item.getItemId()) {
		case R.id.add_new:
			Intent intent = new Intent(this, AddTask.class);
			startActivity(intent);
			break;
		case R.id.sync:
			Log.v(TAG, "onMenuItemSelected: sync");
			populateFromExternal();
			break;
		case R.id.search:
			onSearchRequested();
			break;
		case R.id.preferences:
			Intent settingsActivity = new Intent(getBaseContext(),
					Preferences.class);
			startActivityForResult(settingsActivity, REQUEST_PREFERENCES);
			break;
		case R.id.filter:
			Intent i = new Intent(this, Filter.class);

			i.putStringArrayListExtra(Constants.EXTRA_PRIORITIES,
					TaskHelper.getPrios(m_tasks));
			i.putStringArrayListExtra(Constants.EXTRA_PROJECTS,
					TaskHelper.getProjects(m_tasks));
			i.putStringArrayListExtra(Constants.EXTRA_CONTEXTS,
					TaskHelper.getContexts(m_tasks));

			i.putStringArrayListExtra(Constants.EXTRA_PRIORITIES_SELECTED,
					m_prios);
			i.putStringArrayListExtra(Constants.EXTRA_PROJECTS_SELECTED,
					m_projects);
			i.putStringArrayListExtra(Constants.EXTRA_CONTEXTS_SELECTED,
					m_contexts);
			i.putExtra(Constants.EXTRA_SEARCH, m_search);

			startActivityIfNeeded(i, REQUEST_FILTER);
			break;
		case R.id.sort:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setSingleChoiceItems(R.array.sort, m_sort,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.v(TAG, "onClick " + which);
							m_sort = which;
							dialog.dismiss();
							setFilteredTasks(false);
						}
					});
			builder.show();
			break;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.v(TAG, "onActivityResult: resultCode=" + resultCode + " i=" + data);
		if (requestCode == REQUEST_FILTER) {
			if (resultCode == Activity.RESULT_OK) {
				m_prios = data
						.getStringArrayListExtra(Constants.EXTRA_PRIORITIES);
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
			final List<String> pStrs = TaskHelper.getPrios(m_tasks);
			int size = pStrs.size();
			boolean[] values = new boolean[size];
			for (String prio : m_prios) {
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
		} else {
			return null;
		}
	}

	/** Handle "add task" action. */
	public void onAddTaskClick(View v) {
		startActivity(new Intent(this, AddTask.class));
	}

	/** Handle "sync" action. */
	public void onSyncClick(View v) {
		Log.v(TAG, "titlebar: sync");

		m_app.m_syncing = true;
		updateRefreshStatus();
		populateFromExternal();
	}

	void clearFilter() {
		m_prios = new ArrayList<String>(); // Collections.emptyList();
		m_contexts = new ArrayList<String>(); // Collections.emptyList();
		m_projects = new ArrayList<String>(); // Collections.emptyList();
		m_search = "";
	}

	void setFilteredTasks(boolean reload) {

		if (reload) {

			try {
				m_tasks = TodoUtil.loadTasksFromFile();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		List<Task> tasks = m_tasks;
		if (m_prios.size() > 0) {
			tasks = TaskHelper.getByPrio(tasks, m_prios);
		}
		if (m_contexts.size() > 0) {
			tasks = TaskHelper.getByContext(tasks, m_contexts);
		}
		if (m_projects.size() > 0) {
			tasks = TaskHelper.getByProject(tasks, m_projects);
		}
		if (!Util.isEmpty(m_search)) {
			tasks = TaskHelper.getByTextIgnoreCase(tasks, m_search);
		}
		if (tasks != null) {
			if (m_sort == SORT_PRIO) {
				Collections.sort(tasks, TaskHelper.byPrio);
			} else if (m_sort == SORT_ID) {
				Collections.sort(tasks, TaskHelper.byId);
			} else if (m_sort == SORT_ID_REVERSE) {
				Collections.sort(tasks, TaskHelper.byIdReverse);
			} else if (m_sort == SORT_TEXT) {
				Collections.sort(tasks, TaskHelper.byText);
			}
			m_adapter.clear();
			int size = tasks.size();
			for (int i = 0; i < size; i++) {
				m_adapter.add(tasks.get(i));
			}
			m_adapter.notifyDataSetChanged();
		}

		final TextView filterText = (TextView) findViewById(R.id.filter_text);
		final LinearLayout subtitle_bar = (LinearLayout) findViewById(R.id.subtitle_bar);
		final ImageView subtitle_icon = (ImageView) findViewById(R.id.subtitle_icon);

		if (filterText != null) {
			if (m_filters.size() > 0) {
				String filterTitle = getString(R.string.title_filter_applied)
						+ " ";
				int count = m_filters.size();
				for (int i = 0; i < count; i++) {
					filterTitle += m_filters.get(i) + " ";
				}
				if (!Util.isEmpty(m_search)) {
					filterTitle += "Keyword";
				}
				subtitle_icon.setImageResource(R.drawable.ic_subtitle_filter);

				subtitle_bar.setVisibility(View.VISIBLE);
				filterText.setText(filterTitle);

			} else if (!Util.isEmpty(m_search)) {
				if (filterText != null) {

					subtitle_icon
							.setImageResource(R.drawable.ic_subtitle_search);
					filterText.setText(getString(R.string.title_search_results)
							+ " " + m_search);

					subtitle_bar.setVisibility(View.VISIBLE);
				}
			} else {
				filterText.setText("");
				subtitle_bar.setVisibility(View.GONE);
			}
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		m_pos = position;
		openContextMenu(getListView());
	}

	private void updateRefreshStatus() {
		findViewById(R.id.btn_title_refresh).setVisibility(
				m_app.m_syncing ? View.GONE : View.VISIBLE);
		findViewById(R.id.title_refresh_progress).setVisibility(
				m_app.m_syncing ? View.VISIBLE : View.GONE);
		findViewById(R.id.subtitle_bar).setVisibility(View.GONE);

	}

	private void populateFromFile() {
		Log.d(TAG, "populateFromFile");
		clearFilter();
		setFilteredTasks(true);
	}

	void populateFromExternal() {
		if (m_app.m_loggedIn && getAPI().isAuthenticated()) {
			m_app.m_syncing = true;
			updateRefreshStatus();
			new DropboxFetchAsyncTask(this).execute();
		} else {
			login();
		}
	}

	public class TaskAdapter extends ArrayAdapter<Task> {

		private ArrayList<Task> items;

		private LayoutInflater m_inflater;

		public TaskAdapter(Context context, int textViewResourceId,
				ArrayList<Task> items, LayoutInflater inflater) {
			super(context, textViewResourceId, items);
			this.items = items;
			this.m_inflater = inflater;
		}

		@Override
		public long getItemId(int position) {
			return items.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				convertView = m_inflater.inflate(R.layout.list_item, null);
				holder = new ViewHolder();
				holder.taskid = (TextView) convertView
						.findViewById(R.id.taskid);
				holder.taskprio = (TextView) convertView
						.findViewById(R.id.taskprio);
				holder.tasktext = (TextView) convertView
						.findViewById(R.id.tasktext);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Task task = items.get(position);
			if (task != null) {
				holder.taskid.setText(String.format("%02d", task.getId() + 1));
				if (TaskHelper.toString(task.getPriority())
						.equalsIgnoreCase("")) {
					holder.taskprio.setText("   ");
				} else {
					holder.taskprio.setText("("
							+ TaskHelper.toString(task.getPriority()) + ")");
				}
				SpannableString ss = new SpannableString(task.getText());
				Util.setGray(ss, task.getProjects());
				Util.setGray(ss, task.getContexts());
				holder.tasktext.setText(ss);

				Resources res = getResources();
				holder.tasktext.setTextColor(res.getColor(R.color.black));

				switch (task.getPriority()) {
				case 'A':
					holder.taskprio.setTextColor(res.getColor(R.color.green));
					break;
				case 'B':
					holder.taskprio.setTextColor(res.getColor(R.color.blue));
					break;
				case 'C':
					holder.taskprio.setTextColor(res.getColor(R.color.orange));
					break;
				case 'D':
					holder.taskprio.setTextColor(res.getColor(R.color.gold));
					break;
				case 'E':
					holder.taskprio.setTextColor(res.getColor(R.color.black));
					break;
				default:
					holder.taskprio.setTextColor(res.getColor(R.color.black));
				}
				// hide line numbers unless show preference is checked
				if (!m_app.m_prefs.getBoolean("showlinenumberspref", false)) {
					holder.taskid.setTextColor(res.getColor(R.color.white));
				}

			}
			return convertView;
		}
	}

	private static class ViewHolder {
		private TextView taskid;
		private TextView taskprio;
		private TextView tasktext;
	}

	public DropboxAPI getAPI() {
		return m_app.getAPI();
	}

	public void setConfig(Config config) {
		m_app.setConfig(config);
	}

	public void storeKeys(String accessTokenKey, String accessTokenSecret) {
		Editor editor = m_app.m_prefs.edit();
		editor.putString(Constants.PREF_ACCESSTOKEN_KEY, accessTokenKey);
		editor.putString(Constants.PREF_ACCESSTOKEN_SECRET, accessTokenSecret);
		editor.commit();
	}

	public void setLoggedIn(boolean b) {
		m_app.m_loggedIn = b;
	}

	public void showToast(String string) {
		Util.showToastLong(this, string);
	}

}
