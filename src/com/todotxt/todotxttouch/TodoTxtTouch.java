package com.todotxt.todotxttouch;

import java.io.IOException;
import java.util.ArrayList;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class TodoTxtTouch extends ListActivity {

	private final static String TAG = TodoTxtTouch.class.getSimpleName();
	
	private ProgressDialog m_ProgressDialog = null;
	private ArrayList<Task> m_tasks = null;
	private TaskAdapter m_adapter;
	private String m_fileUrl = "http://ginatrapani.github.com/todo.txt-touch/todo.txt";
	private LocalFile m_localFile = new LocalFile(LocalFile.Type.TODO);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		try {
			m_tasks = m_localFile.getTasks();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			m_tasks = new ArrayList<Task>();
		}
		
		m_adapter = new TaskAdapter(this, R.layout.list_item, m_tasks);

		setListAdapter(this.m_adapter);

		// Get the xml/preferences.xml preferences
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		m_fileUrl = prefs.getString("editTextPref", m_fileUrl);

		populate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

        switch(item.getItemId())
        {
        case R.id.add_new:
        	// Switch to task adding activity
        	// TODO: Get rid of this toast
        	Toast.makeText(getApplicationContext(), "Unimplemented", Toast.LENGTH_SHORT).show();
        	break;
        case R.id.sync:
        	populate();
        	break;
        case R.id.preferences:
        	Intent settingsActivity = new Intent(getBaseContext(),
					Preferences.class);
			startActivity(settingsActivity);
			break;
        }
        
        return true;
	}
	
	private void populate(){
    	new AsyncTask<Void, Void, Void>(){
    		@Override
    		protected void onPreExecute() {
    			m_ProgressDialog = ProgressDialog.show(TodoTxtTouch.this,
    					"Please wait...", "Retrieving todo.txt ...", true);
    		}
			@Override
			protected Void doInBackground(Void... params) {
				try {
					String todotxt_file_contents = "No todo's to display";
					todotxt_file_contents = Util.fetchContent(m_fileUrl);
					String todos[] = todotxt_file_contents.split("\n");
					
					m_localFile.update();
					m_localFile.mergeTasks(todos);
					m_localFile.save();
					
					m_tasks = m_localFile.getTasks();
					
					Log.i(TAG, "ARRAY " + m_tasks.size());
				} catch (Exception e) {
					Log.e(TAG, "BACKGROUND_PROC "+ e.getMessage());
					Util.showToastLong(TodoTxtTouch.this, e.getMessage());
				}
				return null;
			}
    		@Override
    		protected void onPostExecute(Void result) {
    			if (m_tasks != null && m_tasks.size() > 0) {
    				m_adapter.clear();
    				for (int i = 0; i < m_tasks.size(); i++){
    					m_adapter.add(m_tasks.get(i));
    				}
    			}
    			m_ProgressDialog.dismiss();
    			m_adapter.notifyDataSetChanged();
    		}
    	}.execute();
	}

	private class TaskAdapter extends ArrayAdapter<Task> {

		private ArrayList<Task> items;

		public TaskAdapter(Context context, int textViewResourceId,
				ArrayList<Task> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.list_item, null);
			}
			Task o = items.get(position);
			if (o != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				if (tt != null) {
					tt.setText(o.getTaskDescription());
				}
			}
			ListView lv = getListView();
			lv.setTextFilterEnabled(true);

			lv.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// When clicked, show a toast with the TextView text
					Toast.makeText(getApplicationContext(),
							((TextView) view).getText(), Toast.LENGTH_SHORT)
							.show();
				}
			});
			return v;
		}
	}

}
