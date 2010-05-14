package com.todotxt.todotxttouch;

import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

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
			String todotxt_file_contents = "No todo's to display";
			try {
				HttpClient client = new DefaultHttpClient();
				String getURL = "http://ginatrapani.github.com/todo.txt-touch/todo.txt";
				HttpGet get = new HttpGet(getURL);
				HttpResponse responseGet = client.execute(get);
				HttpEntity resEntityGet = responseGet.getEntity();
				if (resEntityGet != null) {
					// do something with the response
					long len = resEntityGet.getContentLength();
					if (len != -1 && len < 2048) {
						todotxt_file_contents = EntityUtils
								.toString(resEntityGet);
					} else {
						todotxt_file_contents = "File too long";
						Log.i("GET RESPONSE", EntityUtils
								.toString(resEntityGet));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
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
