package com.todotxt.todotxttouch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.dropbox.client.DropboxClient;
import com.dropbox.client.DropboxClientHelper;
import com.todotxt.todotxttouch.DropboxUtil.DropboxProvider;
import com.todotxt.todotxttouch.Util.InputDialogListener;
import com.todotxt.todotxttouch.Util.OnMultiChoiceDialogListener;

public class TodoTxtTouch extends ListActivity implements OnSharedPreferenceChangeListener {

	private final static String TAG = TodoTxtTouch.class.getSimpleName();

	private SharedPreferences m_prefs;
	private ProgressDialog m_ProgressDialog = null;
	private ArrayList<Task> m_tasks = new ArrayList<Task>();
	private TaskAdapter m_adapter;
	private String m_fileUrl;
	private DropboxProvider m_client;

	//filter variables
	private ArrayList<String> m_prios = new ArrayList<String>();
	private ArrayList<String> m_contexts = new ArrayList<String>();
	private ArrayList<String> m_projects = new ArrayList<String>();
	private ArrayList<String> m_tags = new ArrayList<String>();
	private String m_search;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);

		m_adapter = new TaskAdapter(this, R.layout.list_item, m_tasks,
				getLayoutInflater());

		setListAdapter(this.m_adapter);

		//FIXME ?
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		getListView().setOnCreateContextMenuListener(this);

		// Get the xml/preferences.xml preferences
		m_prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		m_prefs.registerOnSharedPreferenceChangeListener(this);
		String defValue = getString(R.string.todourl_default);
		m_fileUrl = m_prefs.getString(getString(R.string.todourl_key), defValue);

		//dropbox initialization
		initDropbox();

		populateFromFile();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		m_prefs.unregisterOnSharedPreferenceChangeListener(this);
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
		final int pos = menuInfo.position;
		if(menuid == R.id.update){
			Log.v(TAG, "update");
			final Task backup = m_adapter.getItem(pos);
			InputDialogListener listener = new InputDialogListener() {
				@Override
				public void onClick(final String input) {
					new AsyncTask<Void, Void, Boolean>(){
						protected void onPreExecute() {
			    			m_ProgressDialog = ProgressDialog.show(TodoTxtTouch.this,
			    					"Update", "Please wait...", true);
						}
						@Override
						protected Boolean doInBackground(Void... params) {
							try {
								return DropboxUtil.updateTask(m_client.get(),
										backup.prio, input, backup);
							} catch (Exception e) {
								Log.e(TAG, e.getMessage(), e);
								return false;
							}
						}
						protected void onPostExecute(Boolean result) {
							m_ProgressDialog.dismiss();
							if (result) {
								Util.showToastLong(TodoTxtTouch.this, "Updated task "+input);
							}else{
								Util.showToastLong(TodoTxtTouch.this, "Coult not update task "+input);
							}
							setFilteredTasks(true);
						}
					}.execute();
				}
			};
			Util.showInputDialog(this, R.string.update, R.string.update,
					TaskHelper.toFileFormat(backup), 4, listener,
					R.drawable.menu_add);
		}else if(menuid == R.id.delete){
			Log.v(TAG, "delete");
			OnClickListener listener = new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					final Task task = m_adapter.getItem(pos);
					new AsyncTask<Void, Void, Boolean>(){
						protected void onPreExecute() {
			    			m_ProgressDialog = ProgressDialog.show(TodoTxtTouch.this,
			    					"Delete", "Please wait...", true);
						}
						@Override
						protected Boolean doInBackground(Void... params) {
							try {
								return DropboxUtil.updateTask(m_client.get(),
										TaskHelper.NONE, "", task);
							} catch (Exception e) {
								Log.e(TAG, e.getMessage(), e);
								return false;
							}
						}
						protected void onPostExecute(Boolean result) {
							m_ProgressDialog.dismiss();
							if(result){
								Util.showToastLong(TodoTxtTouch.this, "Deleted task "
										+ TaskHelper.toFileFormat(task));
							}else{
								Util.showToastLong(TodoTxtTouch.this, "Coult not delete task "
										+ TaskHelper.toFileFormat(task));
							}
							setFilteredTasks(true);
						}
					}.execute();
				}
			};
			Util.showConfirmationDialog(this, R.string.areyousure, listener);
		}else if(menuid == R.id.done){
			Log.v(TAG, "done");
			OnClickListener listener = new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					final Task task = m_adapter.getItem(pos);
					new AsyncTask<Void, Void, Boolean>(){
						protected void onPreExecute() {
			    			m_ProgressDialog = ProgressDialog.show(TodoTxtTouch.this,
			    					"Done", "Please wait...", true);
						}
						@Override
						protected Boolean doInBackground(Void... params) {
							try {
								String text = task.text
										.startsWith(TaskHelper.COMPLETED) ? task.text
										: TaskHelper.COMPLETED + task.text;
								return DropboxUtil.updateTask(m_client.get(), TaskHelper.NONE, text, task);
							} catch (Exception e) {
								Log.e(TAG, e.getMessage(), e);
								return false;
							}
						}
						protected void onPostExecute(Boolean result) {
							m_ProgressDialog.dismiss();
							if(result){
								Util.showToastLong(TodoTxtTouch.this, "Completed task "
										+ TaskHelper.toFileFormat(task));
							}else{
								Util.showToastLong(TodoTxtTouch.this, "Coult not complete task "
										+ TaskHelper.toFileFormat(task));
							}
							setFilteredTasks(true);
						}
					}.execute();
				}
			};
			Util.showConfirmationDialog(this, R.string.areyousure, listener);
		}else if(menuid == R.id.priority){
			Log.v(TAG, "priority");
			//TODO priority logic
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.v(TAG, "onMenuItemSelected: "+item.getItemId());
        switch(item.getItemId())
        {
        case R.id.add_new:
			InputDialogListener listener = new InputDialogListener() {
				@Override
				public void onClick(final String input) {
					new AsyncTask<Void, Void, Boolean>(){
						protected void onPreExecute() {
			    			m_ProgressDialog = ProgressDialog.show(TodoTxtTouch.this,
			    					"Add", "Please wait...", true);
						}
						@Override
						protected Boolean doInBackground(Void... params) {
							try {
								return DropboxUtil.addTask(m_client.get(), input);
							} catch (Exception e) {
								Log.e(TAG, e.getMessage(), e);
								return false;
							}
						}
						protected void onPostExecute(Boolean result) {
							m_ProgressDialog.dismiss();
							if(result){
								Util.showToastLong(TodoTxtTouch.this, "Added task "
										+ input);
							}else{
								Util.showToastLong(TodoTxtTouch.this,
										"Coult not add task " + input);
							}
							setFilteredTasks(true);
						}
					}.execute();
				}
			};
			Util.showInputDialog(this, R.string.addtask, R.string.addtask,
					null, 4, listener, R.drawable.menu_add);
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
					new ArrayList<String>(TaskHelper.getPrios(m_tasks)));
			i.putStringArrayListExtra(Constants.EXTRA_PROJECTS,
					new ArrayList<String>(TaskHelper.getProjects(m_tasks)));
			i.putStringArrayListExtra(Constants.EXTRA_CONTEXTS,
					new ArrayList<String>(TaskHelper.getContexts(m_tasks)));
			i.putStringArrayListExtra(Constants.EXTRA_TAGS,
					new ArrayList<String>(TaskHelper.getTags(m_tasks)));

			i.putStringArrayListExtra(Constants.EXTRA_PRIORITIES_SELECTED,
					new ArrayList<String>(m_prios));
			i.putStringArrayListExtra(Constants.EXTRA_PROJECTS_SELECTED,
					new ArrayList<String>(m_projects));
			i.putStringArrayListExtra(Constants.EXTRA_CONTEXTS_SELECTED,
					new ArrayList<String>(m_contexts));
			i.putStringArrayListExtra(Constants.EXTRA_TAGS_SELECTED,
					new ArrayList<String>(m_tags));
			i.putExtra(Constants.EXTRA_SEARCH, m_search);

			startActivityIfNeeded(i, 0);
			break;
		case R.id.sort:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setSingleChoiceItems(R.array.sort, -1, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.v(TAG, "onClick "+which);
					dialog.dismiss();
					Util.showToastLong(TodoTxtTouch.this, "Not implemented");
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
		Log.v(TAG, "onActivityResult: resultCode="+resultCode+" i="+data);
		if(resultCode == Activity.RESULT_OK){
			m_prios = data.getStringArrayListExtra(Constants.EXTRA_PRIORITIES);
			m_projects = data.getStringArrayListExtra(Constants.EXTRA_PROJECTS);
			m_contexts = data.getStringArrayListExtra(Constants.EXTRA_CONTEXTS);
			m_tags = data.getStringArrayListExtra(Constants.EXTRA_TAGS);
			m_search = data.getStringExtra(Constants.EXTRA_SEARCH);
			setFilteredTasks(false);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		final Dialog d;
		if(R.id.priority == id){
			Set<String> prios = TaskHelper.getPrios(m_tasks);
			final List<String> pStrs = new ArrayList<String>(prios);
			Collections.sort(pStrs);
			int size = prios.size();
			boolean[] values = new boolean[size];
			for (String prio : m_prios) {
				int index = pStrs.indexOf(prio);
				if(index != -1){
					values[index] = true;
				}
			}
			d = Util.createMultiChoiceDialog(this, pStrs
					.toArray(new String[size]), values, null, null,
					new OnMultiChoiceDialogListener() {
						@Override
						public void onClick(boolean[] selected) {
							m_prios.clear();
							for (int i = 0; i < selected.length; i++) {
								if(selected[i]){
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
		}else{
			return null;
		}
	}

	private void clearFilter(){
		m_prios = new ArrayList<String>(); //Collections.emptyList();
		m_contexts = new ArrayList<String>(); //Collections.emptyList();
		m_projects = new ArrayList<String>(); //Collections.emptyList();
		m_tags = new ArrayList<String>(); //Collections.emptyList();
		m_search = "";
	}
	
	private void setFilteredTasks(boolean reload){
		if(reload){
			try {
				m_tasks = TodoUtil.loadTasksFromFile();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		List<Task> tasks = m_tasks;
		if(m_prios.size() > 0){
			tasks = TaskHelper.getByPrio(tasks, m_prios);
		}
		if(m_contexts.size() > 0){
			tasks = TaskHelper.getByContext(tasks, m_contexts);
		}
		if(m_projects.size() > 0){
			tasks = TaskHelper.getByProject(tasks, m_projects);
		}
		if(m_tags.size() > 0){
			tasks = TaskHelper.getByTag(tasks, m_tags);
		}
		if(!Util.isEmpty(m_search)){
			tasks = TaskHelper.getByTextIgnoreCase(tasks, m_search);
		}
		if (tasks != null) {
			Collections.sort(tasks, TaskHelper.byPrio);
			m_adapter.clear();
			int size = tasks.size();
			for (int i = 0; i < size; i++){
				m_adapter.add(tasks.get(i));
			}
			m_adapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Task item = m_adapter.items.get(position);
		Util.showDialog(this, R.string.app_name, item.toString());
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.v(TAG, "onSharedPreferenceChanged key="+key);
		if(getString(R.string.todourl_key).equals(key)) {
			String defValue = getString(R.string.todourl_default);
			m_fileUrl = sharedPreferences.getString(key, defValue);
			populateFromExternal();
		} else if (getString(R.string.username_key).equals(key)
				|| getString(R.string.password_key).equals(key)) {
			initDropbox();
		}
	}

	private void populateFromFile(){
		Log.d(TAG, "populateFromFile");
		clearFilter();
		setFilteredTasks(true);
	}

	private void populateFromExternal(){
    	new AsyncTask<Void, Void, Boolean>() {
    		@Override
    		protected void onPreExecute() {
    			m_ProgressDialog = ProgressDialog.show(TodoTxtTouch.this,
    					"Please wait...", "Retrieving todo.txt ...", true);
    		}
			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					DropboxClient client = m_client != null ? m_client.get() : null;
					if(client != null){
						try{
							InputStream is = DropboxClientHelper.getFileStream(client, Constants.REMOTE_FILE);
							m_tasks = TodoUtil.loadTasksFromStream(is);
						}catch(Exception e){
							Log.w(TAG, "Failed to fetch todo file! Initializing dropbox support!"+e.getMessage());
							if(!Constants.TODOFILE.exists()){
								Constants.TODOFILE.createNewFile();
							}
							DropboxClientHelper.putFile(client, "/", Constants.TODOFILE);
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Util.showToastLong(TodoTxtTouch.this, R.string.initialized_dropbox);
								}
							});
						}
					}else{
						m_tasks = TodoUtil.loadTasksFromUrl(m_fileUrl);
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
    			m_ProgressDialog.dismiss();
    			clearFilter();
    			setFilteredTasks(false);
    			Log.d(TAG, "populateFromUrl size=" + m_tasks.size());
    			if(!result){
    				Util.showToastLong(TodoTxtTouch.this, "Sync failed");
    			}else{
    				Util.showToastShort(TodoTxtTouch.this, m_tasks.size()+" items");
    			}
    		}
    	}.execute();
	}

	private void initDropbox(){
		String key = getString(R.string.username_key);
		String username = m_prefs.getString(key, null);
		key = getString(R.string.password_key);
		String password = m_prefs.getString(key, null);
		if(!Util.isEmpty(username) && !Util.isEmpty(password)){
			m_client = new DropboxProvider(Constants.CONSUMER_KEY,
					Constants.CONSUMER_SECRET, username, password);
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
			return items.get(position).id;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if(convertView == null){
				convertView = m_inflater.inflate(R.layout.list_item, null);
				holder = new ViewHolder();
				holder.taskid = (TextView) convertView.findViewById(R.id.taskid);
				holder.taskprio = (TextView) convertView.findViewById(R.id.taskprio);
				holder.tasktext = (TextView) convertView.findViewById(R.id.tasktext);
				holder.taskcontexts = (TextView) convertView.findViewById(R.id.taskcontexts);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			Task task = items.get(position);
			if (task != null) {
				holder.taskid.setText(String.format("%04d", task.id));
				holder.taskprio.setText("("+TaskHelper.toString(task.prio)+")");
				holder.tasktext.setText(task.text);
				String cxtStrs = TaskHelper.getContextsAsString(task);
				holder.taskcontexts.setText(cxtStrs);
				
				switch (task.prio) {
				case 'A':
					holder.taskprio.setTextColor(0xFFFF0000);
//					convertView.setBackgroundColor(0xFFFF0000);
					break;
				case 'B':
					holder.taskprio.setTextColor(0xFF00FF00);
//					convertView.setBackgroundColor(0xFF00FF00);
					break;
				case 'C':
					holder.taskprio.setTextColor(0xFF0000FF);
//					convertView.setBackgroundColor(0xFF0000FF);
					break;
				default:
					holder.taskprio.setTextColor(0xFF555555);
//					convertView.setBackgroundColor(0xFF000000);
				}
			}
			return convertView;
		}

	}
	
	private static class ViewHolder {
		private TextView taskid;
		private TextView taskprio;
		private TextView tasktext;
		private TextView taskcontexts;
	}

}
