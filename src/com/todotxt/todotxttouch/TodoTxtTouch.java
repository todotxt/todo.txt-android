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

import com.todotxt.todotxttouch.Util.OnMultiChoiceDialogListener;

public class TodoTxtTouch extends ListActivity implements OnSharedPreferenceChangeListener {

	private final static String TAG = TodoTxtTouch.class.getSimpleName();
	
	private SharedPreferences m_prefs;
	private ProgressDialog m_ProgressDialog = null;
	private ArrayList<Task> m_tasks = new ArrayList<Task>();
	private TaskAdapter m_adapter;
	private String m_fileUrl;

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
        case R.id.add_new:
        	Intent addTaskActivity = new Intent(getBaseContext(),
        			AddTask.class);
        	startActivity(addTaskActivity);
        	break;
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
        default:
        	return super.onMenuItemSelected(featureId, item);
        }
        return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		final Dialog d;
		switch (id) {
		case R.id.priority:
			Set<String> prios = TaskHelper.getPrios(m_tasks);
			final List<String> pStrs = new ArrayList<String>(prios);
			Collections.sort(pStrs);
			d = Util.createMultiChoiceDialog(this, pStrs
					.toArray(new String[prios.size()]), null, null, null,
					new OnMultiChoiceDialogListener() {
						@Override
						public void onClick(boolean[] selected) {
							List<Integer> pInts = new ArrayList<Integer>();
							for (int i = 0; i < selected.length; i++) {
								if(selected[i]){
									pInts.add(TaskHelper.parsePrio(pStrs.get(i)));
								}
							}
							List<Task> items = TaskHelper.getByPrio(m_tasks, pInts);
							TodoUtil.setTasks(m_adapter, items);
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
		case R.id.context:
			Set<String> contexts = TaskHelper.getContexts(m_tasks);
			final List<String> cStrs = new ArrayList<String>(contexts);
			Collections.sort(cStrs);
			d = Util.createMultiChoiceDialog(this, cStrs
					.toArray(new String[contexts.size()]), null, null, null,
					new OnMultiChoiceDialogListener() {
						@Override
						public void onClick(boolean[] selected) {
							List<String> cStrs2 = new ArrayList<String>();
							for (int i = 0; i < selected.length; i++) {
								if(selected[i]){
									cStrs2.add(cStrs.get(i));
								}
							}
							List<Task> items = TaskHelper.getByContext(m_tasks, cStrs2);
							TodoUtil.setTasks(m_adapter, items);
							removeDialog(R.id.context);
						}
					});
			d.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					removeDialog(R.id.context);
				}
			});
			return d;
		}
		return null;
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
			LocalFile localFile = LocalFile.getInstance();
			m_tasks = localFile.getTasks();
		} catch (IOException e) {
			Log.e(TAG, "localFile" + e.getMessage());
		}
		Log.d(TAG, "populateFromFile size=" + m_tasks.size());
		TodoUtil.setTasks(m_adapter, m_tasks);
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
					ArrayList<Task> todos = TodoUtil.loadTasksFromUrl(TodoTxtTouch.this, m_fileUrl);

					if(Util.isDeviceWritable())
					{
						LocalFile localFile = LocalFile.getInstance();
						
						localFile.mergeTasks(todos);
						localFile.save();
						
						m_tasks = localFile.getTasks();
					}
					else
						m_tasks = todos;
					
					Log.i(TAG, "ARRAY " + m_tasks.size());
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
				return null;
			}
    		@Override
    		protected void onPostExecute(Void result) {
    			m_ProgressDialog.dismiss();
    			TodoUtil.setTasks(m_adapter, m_tasks);
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
