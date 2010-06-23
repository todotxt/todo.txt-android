package com.todotxt.todotxttouch;

import java.io.IOException;
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
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.todotxt.todotxttouch.Util.InputDialogListener;
import com.todotxt.todotxttouch.Util.OnMultiChoiceDialogListener;

public class TodoTxtTouch extends ListActivity implements OnSharedPreferenceChangeListener {

	private final static String TAG = TodoTxtTouch.class.getSimpleName();
	
	private SharedPreferences m_prefs;
	private ProgressDialog m_ProgressDialog = null;
	private ArrayList<Task> m_tasks = new ArrayList<Task>();
	private TaskAdapter m_adapter;
	private String m_fileUrl;

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

		// Get the xml/preferences.xml preferences
		m_prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		m_prefs.registerOnSharedPreferenceChangeListener(this);
		String defValue = getString(R.string.todourl_default);
		m_fileUrl = m_prefs.getString(getString(R.string.todourl_key), defValue);

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
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

        switch(item.getItemId())
        {
        //TODO show when implemented
//        case R.id.add_new:
//        	// Switch to task adding activity
//        	// TODO: Get rid of this toast
//        	Toast.makeText(getApplicationContext(), "Unimplemented", Toast.LENGTH_SHORT).show();
//        	break;
        case R.id.sync:
        	populateFromUrl();
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
			return createDialog(contexts, m_contexts, R.id.context, new OnClickListener() {
				@Override
				public void onClick(List<String> strs) {
					m_contexts = strs;
				}
			});
		}else if(R.id.project == id){
			Set<String> projects = TaskHelper.getProjects(m_tasks);
			return createDialog(projects, m_projects, R.id.project, new OnClickListener(){
				@Override
				public void onClick(List<String> strs) {
					m_projects = strs;
				}
			});
		}else if(R.id.tag == id){
			Set<String> tags = TaskHelper.getTags(m_tasks);
			return createDialog(tags, m_tags, R.id.tag, new OnClickListener() {
				@Override
				public void onClick(List<String> strs) {
					m_tags = strs;
				}
			});
		}else{
			return null;
		}
	}
	
	private interface OnClickListener {
		void onClick(List<String> strs);
	}
	
	private Dialog createDialog(Set<String> in, List<String> selected,
			final int dialogid, final OnClickListener listener) {
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
			populateFromUrl();
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

	private void populateFromUrl(){
    	new AsyncTask<Void, Void, Void>() {
    		@Override
    		protected void onPreExecute() {
    			m_ProgressDialog = ProgressDialog.show(TodoTxtTouch.this,
    					"Please wait...", "Retrieving todo.txt ...", true);
    		}
			@Override
			protected Void doInBackground(Void... params) {
				try {
					m_tasks = TodoUtil.loadTasksFromUrl(TodoTxtTouch.this, m_fileUrl);
					TodoUtil.writeToFile(m_tasks);
				} catch (IOException e) {
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
