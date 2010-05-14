package com.todotxt.todotxttouch;

import java.util.ArrayList;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class TodoTxtTouch extends ListActivity {
	private ProgressDialog m_ProgressDialog = null;
	private ArrayList<Task> m_tasks = null;
	private TaskAdapter m_adapter;
	private Runnable viewTasks;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		m_tasks = new ArrayList<Task>();
		this.m_adapter = new TaskAdapter(this, R.layout.list_item, m_tasks);
		setListAdapter(this.m_adapter);

		viewTasks = new Runnable() {
			@Override
			public void run() {
				getTasks();
			}
		};
		Thread thread = new Thread(null, viewTasks, "MagentoBackground");
		thread.start();
		m_ProgressDialog = ProgressDialog.show(TodoTxtTouch.this,
				"Please wait...", "Retrieving todo.txt ...", true);
	}

	private Runnable returnRes = new Runnable() {

		@Override
		public void run() {
			if (m_tasks != null && m_tasks.size() > 0) {
				m_adapter.notifyDataSetChanged();
				for (int i = 0; i < m_tasks.size(); i++)
					m_adapter.add(m_tasks.get(i));
			}
			m_ProgressDialog.dismiss();
			m_adapter.notifyDataSetChanged();
		}
	};

	private void getTasks() {
		try {
			// TODO: Retrieve todo.txt from some (web-based) location here
			// Perhaps using this tutorial: http://www.softwarepassion.com/android-series-get-post-and-multipart-post-requests/
			String todotxt_file_contents = "Call Mom @phone\n"
					+ "Schedule teeth cleaning with dentist @phone\n"
					+ "Pick up milk and bread @store\n"
					+ "Develop Android app\n"
					+ "Add configurable project/context/priority tabs to this list (defaults: priority A, @phone, @store)\n"
					+ "Add checkboxes to mark items as done\n"
					+ "Apply for Dropbox API access\n"
					+ "Implement Dropbox Anywhere API to read/write todo.txt and done.txt\n";
			String todos[] = todotxt_file_contents.split("\n");
			m_tasks = new ArrayList<Task>();
			for (String todo : todos) {
				Task t = new Task();
				t.setTaskDescription(todo);
				m_tasks.add(t);
			}
			Thread.sleep(5000);
			Log.i("ARRAY", "" + m_tasks.size());
		} catch (Exception e) {
			Log.e("BACKGROUND_PROC", e.getMessage());
		}
		runOnUiThread(returnRes);
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
