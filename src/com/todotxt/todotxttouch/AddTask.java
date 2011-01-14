package com.todotxt.todotxttouch;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

import com.dropbox.client.DropboxAPI;

public class AddTask extends Activity {

	private final static String TAG = AddTask.class.getSimpleName();

	private ProgressDialog m_ProgressDialog = null;

	private Task m_backup;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.add_task);

		ArrayList<Task> tasks;
		try {
			tasks = TodoUtil.loadTasksFromFile();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			tasks = new ArrayList<Task>();
		}

		// text
		final EditText text = (EditText) findViewById(R.id.taskText);
		Task task = (Task) getIntent().getSerializableExtra(
				Constants.EXTRA_TASK);
		if (task != null) {
			m_backup = task;
			text.setText(TaskHelper.toFileFormat(task));
			setTitle(R.string.update);
		} else {
			setTitle(R.string.addtask);
		}

		// priorities
		Spinner priorities = (Spinner) findViewById(R.id.priorities);
		final ArrayList<String> prioArr = new ArrayList<String>();
		prioArr.add("Priority");
		for (char c = 'A'; c <= 'D'; c++) {
			prioArr.add("" + c);
		}
		priorities.setAdapter(Util.newSpinnerAdapter(this, prioArr));
		if (m_backup != null && m_backup.prio >= 'A') {
			priorities.setSelection(2 + m_backup.prio - 'A');
		}
		priorities.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long id) {
				if (position > 0) {
					String item = prioArr.get(position);
					String t = text.getText().toString();
					Task task = TaskHelper.createTask(-1, t);
					task.prio = item.charAt(0);
					if (Util.isEmpty(t) && task.prio != TaskHelper.NONE) {
						task.text = " ";
					}
					text.setText(TaskHelper.toFileFormat(task));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		// projects
		Spinner projects = (Spinner) findViewById(R.id.projects);
		final ArrayList<String> projectsArr = TaskHelper.getProjects(tasks);
		projectsArr.add(0, "Project");
		projects.setAdapter(Util.newSpinnerAdapter(this, projectsArr));
		projects.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long id) {
				if (position > 0) {
					String item = projectsArr.get(position);
					text.append("+" + item + " ");
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		// contexts
		Spinner contexts = (Spinner) findViewById(R.id.contexts);
		final ArrayList<String> contextsArr = TaskHelper.getContexts(tasks);
		contextsArr.add(0, "Context");
		contexts.setAdapter(Util.newSpinnerAdapter(this, contextsArr));
		contexts.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long id) {
				if (position > 0) {
					String item = contextsArr.get(position);
					text.append("@" + item + " ");
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		// cancel
		Button cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		// add
		Button addBtn = (Button) findViewById(R.id.addTask);
		if (m_backup != null) {
			addBtn.setText(R.string.update);
		}
		addBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String input = text.getText().toString();
				new AsyncTask<Void, Void, Boolean>() {
					protected void onPreExecute() {
						m_ProgressDialog = ProgressDialog.show(AddTask.this,
								getTitle(), "Please wait...", true);
					}

					@Override
					protected Boolean doInBackground(Void... params) {
						try {
							TodoApplication app = (TodoApplication) getApplication();
							DropboxAPI api = app.getAPI();
							if (api != null) {
								if (m_backup != null) {
									Task updatedTask = TaskHelper.createTask(
											m_backup.id, input);
									return DropboxUtil.updateTask(api,
											updatedTask.prio, input, m_backup);
								} else {
									return DropboxUtil.addTask(api, input);
								}
							}
						} catch (Exception e) {
							Log.e(TAG,
									"input: " + input + " - " + e.getMessage());
						}
						return false;
					}

					protected void onPostExecute(Boolean result) {
						m_ProgressDialog.dismiss();
						if (result) {
							String res = m_backup != null ? getString(R.string.updated_task)
									: getString(R.string.added_task);
							Util.showToastLong(AddTask.this, res);
							finish();
						} else {
							String res = m_backup != null ? getString(R.string.update_task_failed)
									: getString(R.string.add_task_failed);
							Util.showToastLong(AddTask.this, res);
						}
					}
				}.execute();
			}
		});
	}

}