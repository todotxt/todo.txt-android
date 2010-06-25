package com.todotxt.todotxttouch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
import com.dropbox.client.DropboxException;
import com.todotxt.todotxttouch.Util.InputDialogListener;
import com.todotxt.todotxttouch.Util.OnMultiChoiceDialogListener;

public class TodoTxtTouch extends ListActivity implements OnSharedPreferenceChangeListener {

	private final static String TAG = TodoTxtTouch.class.getSimpleName();

	private SharedPreferences m_prefs;
	private ProgressDialog m_ProgressDialog = null;
	private ArrayList<Task> m_tasks = new ArrayList<Task>();
	private TaskAdapter m_adapter;
	private String m_fileUrl;
	private DropboxClient m_client;

	//filter variables
	private List<Integer> m_prios = Collections.emptyList();
	private List<String> m_contexts = Collections.emptyList();
	private List<String> m_projects = Collections.emptyList();
	private List<String> m_tags = Collections.emptyList();
	private String m_search;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		m_adapter = new TaskAdapter(this, R.layout.list_item, m_tasks, getLayoutInflater());

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
		String key = getString(R.string.username_key);
		String username = m_prefs.getString(key, null);
		key = getString(R.string.password_key);
		String password = m_prefs.getString(key, null);
		if(!Util.isEmpty(username) && !Util.isEmpty(password)){
			try {
				m_client = DropboxClientHelper.newClient(
						Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET,
						username, password);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}

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
			final Task task = m_tasks.get(pos);
			InputDialogListener listener = new InputDialogListener() {
				@Override
				public void onClick(String input) {
					Task t = TodoUtil.createTask(task.id, input);
					m_tasks.set(pos, t);
					try {
						TodoUtil.pushTasks(m_client, m_tasks);
						Util.showToastLong(TodoTxtTouch.this, "Added task "
								+ TaskHelper.toFileFormat(t));
					} catch (DropboxException e) {
						Log.e(TAG, e.getMessage(), e);
						m_tasks.set(pos, task);
						Util.showToastLong(TodoTxtTouch.this, "Coult not add task "
								+ TaskHelper.toFileFormat(t));
					}
				}
			};
			Util.showInputDialog(this, R.string.menu_update,
					R.string.menu_update, TaskHelper.toFileFormat(task), 4,
					listener, R.drawable.menu_add);
		}else if(menuid == R.id.delete){
			Log.v(TAG, "delete");
			OnClickListener listener = new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Task task = m_tasks.get(pos);
					task.deleted = true;
				}
			};
			Util.showConfirmationDialog(this, R.string.menu_delete, listener);
		}else if(menuid == R.id.done){
			Log.v(TAG, "done");
		}else if(menuid == R.id.priority){
			Log.v(TAG, "priority");
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
				public void onClick(String input) {
					try {
						TodoUtil.addTask(m_client, m_tasks, input);
						Util.showToastLong(TodoTxtTouch.this, "Added task "
								+ input);
					} catch (DropboxException e) {
						Log.e(TAG, e.getMessage(), e);
						Util.showToastLong(TodoTxtTouch.this,
								"Coult not add task " + input);
					}
				}
			};
			Util.showInputDialog(this, R.string.menu_add, R.string.menu_add,
					null, 4, listener, R.drawable.menu_add);
			break;
        case R.id.sync:
        	populateFromExternal();
        	break;
        case R.id.preferences:
        	Intent settingsActivity = new Intent(getBaseContext(),
					Preferences.class);
			startActivity(settingsActivity);
			break;
        case R.id.priority:
        	showDialog(R.id.priority);
        	break;
        case R.id.context:
        	showDialog(R.id.context);
        	break;
        case R.id.project:
        	showDialog(R.id.project);
        	break;
        case R.id.tag:
        	showDialog(R.id.tag);
        	break;
        case R.id.search:
        	InputDialogListener oklistener = new InputDialogListener() {
				@Override
				public void onClick(String input) {
					m_search = input;
					setFilteredTasks();
				}
			};
			Util.showInputDialog(this, R.string.menu_search,
					R.string.search_summary, m_search, 1, oklistener,
					android.R.drawable.ic_menu_search);
        	break;
        default:
        	return super.onMenuItemSelected(featureId, item);
        }
        return true;
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
			for (Integer prio : m_prios) {
				String str = TaskHelper.toString(prio);
				int index = pStrs.indexOf(str);
				if(index != -1){
					values[index] = true;
				}
			}
			d = Util.createMultiChoiceDialog(this, pStrs
					.toArray(new String[size]), values, null, null,
					new OnMultiChoiceDialogListener() {
						@Override
						public void onClick(boolean[] selected) {
							List<Integer> pInts = new ArrayList<Integer>();
							for (int i = 0; i < selected.length; i++) {
								if(selected[i]){
									pInts.add(TaskHelper.parsePrio(pStrs.get(i)));
								}
							}
							m_prios = pInts;
							setFilteredTasks();
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
		}else if(R.id.context == id){
			Set<String> contexts = TaskHelper.getContexts(m_tasks);
			return createDialog(contexts, m_contexts, R.id.context, new OnChoiceClickListener() {
				@Override
				public void onClick(List<String> strs) {
					m_contexts = strs;
				}
			});
		}else if(R.id.project == id){
			Set<String> projects = TaskHelper.getProjects(m_tasks);
			return createDialog(projects, m_projects, R.id.project, new OnChoiceClickListener(){
				@Override
				public void onClick(List<String> strs) {
					m_projects = strs;
				}
			});
		}else if(R.id.tag == id){
			Set<String> tags = TaskHelper.getTags(m_tasks);
			return createDialog(tags, m_tags, R.id.tag, new OnChoiceClickListener() {
				@Override
				public void onClick(List<String> strs) {
					m_tags = strs;
				}
			});
		}else{
			return null;
		}
	}
	
	private interface OnChoiceClickListener {
		void onClick(List<String> strs);
	}
	
	private Dialog createDialog(Set<String> in, List<String> selected,
			final int dialogid, final OnChoiceClickListener listener) {
		final List<String> cStrs = new ArrayList<String>(in);
		Collections.sort(cStrs);
		int size = in.size();
		boolean[] values = new boolean[size];
		for (String sel : selected) {
			int index = cStrs.indexOf(sel);
			if(index != -1){
				values[index] = true;
			}
		}
		Dialog d = Util.createMultiChoiceDialog(this, cStrs
				.toArray(new String[size]), values, null, null,
				new OnMultiChoiceDialogListener() {
					@Override
					public void onClick(boolean[] selected) {
						List<String> cStrs2 = new ArrayList<String>();
						for (int i = 0; i < selected.length; i++) {
							if(selected[i]){
								cStrs2.add(cStrs.get(i));
							}
						}
						listener.onClick(cStrs2);
						setFilteredTasks();
						removeDialog(dialogid);
					}
				});
		d.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				removeDialog(dialogid);
			}
		});
		return d;
	}
	
	private void clearFilter(){
		m_prios = Collections.emptyList();
		m_contexts = Collections.emptyList();
		m_projects = Collections.emptyList();
		m_tags = Collections.emptyList();
	}
	
	private void setFilteredTasks(){
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
			tasks = TaskHelper.getByText(tasks, m_search);
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
		}
	}

	private void populateFromFile(){
		try {
			m_tasks = TodoUtil.loadTasksFromFile();
		} catch (IOException e) {
			Log.e(TAG, "localFile" + e.getMessage());
		}
		Log.d(TAG, "populateFromFile size=" + m_tasks.size());
		clearFilter();
		setFilteredTasks();
	}

	private void populateFromExternal(){
    	new AsyncTask<Void, Void, Void>() {
    		@Override
    		protected void onPreExecute() {
    			m_ProgressDialog = ProgressDialog.show(TodoTxtTouch.this,
    					"Please wait...", "Retrieving todo.txt ...", true);
    		}
			@Override
			protected Void doInBackground(Void... params) {
				try {
					if(m_client != null){
						InputStream is = DropboxClientHelper.getFileStream(m_client, Constants.REMOTE_FILE);
						m_tasks = TodoUtil.loadTasksFromStream(TodoTxtTouch.this, is);
					}else{
						m_tasks = TodoUtil.loadTasksFromUrl(TodoTxtTouch.this, m_fileUrl);
					}
					TodoUtil.writeToFile(m_tasks, Constants.TODOFILE);
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}
				return null;
			}
    		@Override
    		protected void onPostExecute(Void result) {
    			m_ProgressDialog.dismiss();
    			clearFilter();
    			setFilteredTasks();
    			Log.d(TAG, "populateFromUrl size=" + m_tasks.size());
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
				case 1:
					holder.taskprio.setTextColor(0xFFFF0000);
//					convertView.setBackgroundColor(0xFFFF0000);
					break;
				case 2:
					holder.taskprio.setTextColor(0xFF00FF00);
//					convertView.setBackgroundColor(0xFF00FF00);
					break;
				case 3:
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
