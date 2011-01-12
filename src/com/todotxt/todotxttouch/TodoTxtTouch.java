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
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.TextView;

import com.dropbox.client.DropboxAPI;
import com.dropbox.client.DropboxAPI.Config;
import com.dropbox.client.DropboxAPI.FileDownload;
import com.todotxt.todotxttouch.Util.OnMultiChoiceDialogListener;

public class TodoTxtTouch extends ListActivity implements
		OnSharedPreferenceChangeListener {

	private final static String TAG = TodoTxtTouch.class.getSimpleName();

	private final static int SORT_PRIO = 0;
	private final static int SORT_ID = 1;
	private final static int SORT_TEXT = 2;

	private ProgressDialog m_ProgressDialog = null;
	private ArrayList<Task> m_tasks = new ArrayList<Task>();
	private TaskAdapter m_adapter;
	private TodoApplication m_app;

	// filter variables
	private ArrayList<String> m_prios = new ArrayList<String>();
	private ArrayList<String> m_contexts = new ArrayList<String>();
	private ArrayList<String> m_projects = new ArrayList<String>();
	private String m_search;

	private int m_pos = Constants.INVALID_POSITION;
	private int m_sort = SORT_PRIO;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		m_app = (TodoApplication) getApplication();
		m_app.m_prefs.registerOnSharedPreferenceChangeListener(this);

		m_adapter = new TaskAdapter(this, R.layout.list_item, m_tasks, getLayoutInflater());

		setListAdapter(this.m_adapter);

		// FIXME adapter implements Filterable?
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		getListView().setOnCreateContextMenuListener(this);

		boolean firstrun = m_app.m_prefs.getBoolean(Constants.PREF_FIRSTRUN, true);
		if (firstrun) {
			Log.i(TAG, "Initializing app");
			Editor editor = m_app.m_prefs.edit();
			editor.putBoolean(Constants.PREF_FIRSTRUN, false);
			editor.commit();
			if ( getAPI().isAuthenticated() ) {
				populateFromExternal();
			} else {
				login();
			}
		} else {
			populateFromFile();
		}
	}

	private void login() {
		final DropboxAPI api = getAPI();
		if ( api.isAuthenticated() ) {
			DropboxLoginAsyncTask loginTask = new DropboxLoginAsyncTask(this, m_app.getConfig());
			loginTask.execute();
		} else {
			TodoTxtTouch.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Util.LoginDialogListener dialogListener = new Util.LoginDialogListener() {
						@Override
						public void onClick(String username, String password) {
							try {
								String consumerKey = getResources().getText(R.string.dropbox_consumer_key).toString();
								String consumerSecret = getResources().getText(R.string.dropbox_consumer_secret).toString();
								Log.i(TAG, "Using Dropbox key " + consumerKey + " and secret " + consumerSecret);
								
								DropboxLoginAsyncTask loginTask = new DropboxLoginAsyncTask(TodoTxtTouch.this, username, password, m_app.getConfig());
								loginTask.execute();
							} catch (Exception e) {
								Log.i(TAG,
										"Could not create Dropbox client! Exception details: "
												+ e.getLocalizedMessage());
							}
						}
					};
					Util.showLoginDialog(TodoTxtTouch.this, R.string.dropbox_authentication,
							R.string.login, "", dialogListener,
							R.drawable.menu_sync);
				}
			});

		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		m_app.m_prefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setFilteredTasks(true);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
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
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		m_sort = state.getInt("sort");
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
		inflater.inflate(R.menu.main_long, menu);
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
					new AsyncTask<Void, Void, Boolean>() {
						protected void onPreExecute() {
							m_ProgressDialog = ProgressDialog.show(
									TodoTxtTouch.this, "Delete",
									"Please wait...", true);
						}

						@Override
						protected Boolean doInBackground(Void... params) {
							try {
								DropboxAPI api = m_app.getAPI();
								if (api != null) {
									return DropboxUtil.updateTask(api,
											TaskHelper.NONE, "", task);
								}
							} catch (Exception e) {
								Log.e(TAG, e.getMessage(), e);
							}
							return false;
						}

						protected void onPostExecute(Boolean result) {
							m_ProgressDialog.dismiss();
							if (result) {
								Util.showToastLong(
										TodoTxtTouch.this,
										"Deleted task "
												+ TaskHelper.toFileFormat(task));
							} else {
								Util.showToastLong(
										TodoTxtTouch.this,
										"Could not delete task "
												+ TaskHelper.toFileFormat(task));
							}
							setFilteredTasks(true);
						}
					}.execute();
				}
			};
			Util.showConfirmationDialog(this, R.string.areyousure, listener);
		} else if (menuid == R.id.done) {
			Log.v(TAG, "done");
			OnClickListener listener = new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					final Task task = m_adapter.getItem(pos);
					new AsyncTask<Void, Void, Boolean>() {
						protected void onPreExecute() {
							m_ProgressDialog = ProgressDialog.show(
									TodoTxtTouch.this, "Marking Task Done",
									"Please wait...", true);
						}

						@Override
						protected Boolean doInBackground(Void... params) {
							try {
								if (task.text.startsWith(TaskHelper.COMPLETED)) {
									return true;
								} else {
									String format = TaskHelper.DATEFORMAT
											.format(new Date());
									String text = TaskHelper.COMPLETED + format
											+ task.text;
									DropboxAPI client = m_app
											.getAPI();
									Log.v(TAG, "Completing task with this text: " + text);
									return DropboxUtil.updateTask(client,
											TaskHelper.NONE, text, task);
								}
							} catch (Exception e) {
								Log.e(TAG, e.getMessage(), e);
							}
							return false;
						}

						protected void onPostExecute(Boolean result) {
							m_ProgressDialog.dismiss();
							if (result) {
								Util.showToastLong(
										TodoTxtTouch.this,
										"Completed task "
												+ TaskHelper.toFileFormat(task));
							} else {
								Util.showToastLong(
										TodoTxtTouch.this,
										"Could not complete task "
												+ TaskHelper.toFileFormat(task));
							}
							setFilteredTasks(true);
						}
					}.execute();
				}
			};
			Util.showConfirmationDialog(this, R.string.areyousure, listener);
		} else if (menuid == R.id.priority) {
			Log.v(TAG, "priority");
			final String[] prioArr = { "" + TaskHelper.NONE, "A", "B", "C" };
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Select priority");
			builder.setSingleChoiceItems(prioArr, 0, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, final int which) {
					final Task task = m_adapter.getItem(pos);
					dialog.dismiss();
					new AsyncTask<Void, Void, Boolean>() {
						protected void onPreExecute() {
							m_ProgressDialog = ProgressDialog.show(
									TodoTxtTouch.this, "Setting Priority",
									"Please wait...", true);
						}

						@Override
						protected Boolean doInBackground(Void... params) {
							try {
								DropboxAPI api = m_app
										.getAPI();
								if (api != null) {
									return DropboxUtil.updateTask(api,
											prioArr[which].charAt(0),
											task.text, task);
								}
							} catch (Exception e) {
								Log.e(TAG, e.getMessage(), e);
							}
							return false;
						}

						protected void onPostExecute(Boolean result) {
							m_ProgressDialog.dismiss();
							if (result) {
								Util.showToastLong(TodoTxtTouch.this,
										"Prioritized task " + task.text);
							} else {
								Util.showToastLong(TodoTxtTouch.this,
										"Could not prioritize task "
												+ TaskHelper.toFileFormat(task));
							}
							setFilteredTasks(true);
						}
					}.execute();
				}
			});
			builder.show();
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
		case R.id.preferences:
			Intent settingsActivity = new Intent(getBaseContext(),
					Preferences.class);
			startActivity(settingsActivity);
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

			startActivityIfNeeded(i, 0);
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
		if (resultCode == Activity.RESULT_OK) {
			m_prios = data.getStringArrayListExtra(Constants.EXTRA_PRIORITIES);
			m_projects = data.getStringArrayListExtra(Constants.EXTRA_PROJECTS);
			m_contexts = data.getStringArrayListExtra(Constants.EXTRA_CONTEXTS);
			m_search = data.getStringExtra(Constants.EXTRA_SEARCH);
			setFilteredTasks(false);
		}
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

	private void clearFilter() {
		m_prios = new ArrayList<String>(); // Collections.emptyList();
		m_contexts = new ArrayList<String>(); // Collections.emptyList();
		m_projects = new ArrayList<String>(); // Collections.emptyList();
		m_search = "";
	}

	private void setFilteredTasks(boolean reload) {
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
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		m_pos = position;
		openContextMenu(getListView());
	}

	private void populateFromFile() {
		Log.d(TAG, "populateFromFile");
		clearFilter();
		setFilteredTasks(true);
	}

	void populateFromExternal() {
		new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected void onPreExecute() {
				if ( !m_app.getAPI().isAuthenticated() ) {
					login();
				} else {
				m_ProgressDialog = ProgressDialog.show(TodoTxtTouch.this,
						"Please wait...", "Retrieving todo.txt ...", true);
				}
			}

			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					TodoApplication app = (TodoApplication) getApplication();
					DropboxAPI api = app.getAPI();
					if(api.isAuthenticated()){
						try{
							FileDownload file = api.getFileStream(Constants.DROPBOX_MODUS, Constants.REMOTE_FILE, null);
							m_tasks = TodoUtil.loadTasksFromStream(file.is);
						}catch(Exception e){
							Log.w(TAG, "Failed to fetch todo file! Initializing dropbox support!"+e.getMessage());
							if(!Constants.TODOFILE.exists()){
								Util.createParentDirectory(Constants.TODOFILE);
								Constants.TODOFILE.createNewFile();
							}
							api.putFile(Constants.DROPBOX_MODUS, Constants.PATH_TO_TODO_TXT, Constants.TODOFILE);
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Util.showToastLong(TodoTxtTouch.this,
											R.string.initialized_dropbox);
								}
							});
						}
					} else {
						Log.w(TAG, "Could not get tasks!");
						return false;
					}
					TodoUtil.writeToFile(m_tasks, Constants.TODOFILE);
					return true;
				} catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if ( m_ProgressDialog == null ) return;
				m_ProgressDialog.dismiss();
				clearFilter();
				setFilteredTasks(false);
				Log.d(TAG, "populateFromUrl size=" + m_tasks.size());
				if (!result) {
					Util.showToastLong(TodoTxtTouch.this, "Sync failed");
				} else {
					Util.showToastShort(TodoTxtTouch.this, m_tasks.size()
							+ " items");
				}
			}
		}.execute();
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
			return items.get(position).id;
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
				holder.taskid.setText(String.format("%02d", task.id + 1));
				if (TaskHelper.toString(task.prio).equalsIgnoreCase("")) {
					holder.taskprio.setText("   ");
				} else {
					holder.taskprio.setText("("
							+ TaskHelper.toString(task.prio) + ")");
				}
				SpannableString ss = new SpannableString(task.text);
				Util.setBold(ss, TaskHelper.getProjects(task.text));
				Util.setBold(ss, TaskHelper.getContexts(task.text));
				holder.tasktext.setText(ss);

				Resources res = getResources();
				switch (task.prio) {
				case 'A':
					holder.taskprio.setTextColor(res.getColor(R.color.gold));
					break;
				case 'B':
					holder.taskprio.setTextColor(res.getColor(R.color.green));
					break;
				case 'C':
					holder.taskprio.setTextColor(res.getColor(R.color.blue));
					break;
				default:
					holder.taskprio.setTextColor(res.getColor(R.color.black));
				}
				// hide ID unless it's highlighted for a cleaner interface
				holder.taskid.setTextColor(res.getColor(R.color.black));
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

}
