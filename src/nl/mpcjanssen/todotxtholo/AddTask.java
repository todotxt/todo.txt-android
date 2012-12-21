/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2012 Todo.txt contributors (http://todotxt.com)
 *
 * LICENSE:
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
 * @author Todo.txt contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2012 Todo.txt contributors (http://todotxt.com)
 */
package nl.mpcjanssen.todotxtholo;

import java.util.ArrayList;
import java.util.List;

import nl.mpcjanssen.todotxtholo.task.Priority;
import nl.mpcjanssen.todotxtholo.task.PriorityTextSplitter;
import nl.mpcjanssen.todotxtholo.task.Task;
import nl.mpcjanssen.todotxtholo.task.TaskBag;
import nl.mpcjanssen.todotxtholo.util.CursorPositionCalculator;
import nl.mpcjanssen.todotxtholo.util.Strings;
import nl.mpcjanssen.todotxtholo.util.Util;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;


public class AddTask extends Activity {

	private final static String TAG = AddTask.class.getSimpleName();

	private ProgressDialog m_ProgressDialog = null;

	private Task m_backup;

	private TodoApplication m_app;

	private TaskBag taskBag;

	private String share_text;

	private Spinner priorities;
	private Spinner contexts;
	private Spinner projects;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_task, menu);

		if (m_backup!=null) {
			MenuItem m = menu.findItem(R.id.menu_add_task);
			if (m!=null) {
				m.setTitle(R.string.update);
				m.setIcon(R.drawable.content_save);
			}
		}
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add_task:
			// strip line breaks
			final EditText textInputField = (EditText) findViewById(R.id.taskText);
			final String input = textInputField.getText().toString()
					.replaceAll("\\r\\n|\\r|\\n", " ");
			addEditAsync(input);
			break;
		case R.id.menu_add_task_help:
			Intent intent = new Intent(this.getApplicationContext(),HelpActivity.class);
			startActivity(intent);
			break;
		}
		return true;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.add_task);

		m_app = (TodoApplication) getApplication();
		taskBag = m_app.getTaskBag();	
		
		sendBroadcast(new Intent(Constants.INTENT_START_SYNC_WITH_REMOTE));

		
		
		final Intent intent = getIntent();
		final String action = intent.getAction();
		// create shortcut and exit
		if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
			Log.d(TAG, "Setting up shortcut icon");
			setupShortcut();
			finish();
			return;
		} else if (Intent.ACTION_SEND.equals(action)) {
			Log.d(TAG, "Share");
			share_text = (String) intent
					.getCharSequenceExtra(Intent.EXTRA_TEXT);
			Log.d(TAG, share_text);
		}

		// text
		final EditText textInputField = (EditText) findViewById(R.id.taskText);
		textInputField.setGravity(Gravity.TOP);

		if (share_text != null) {
			textInputField.setText(share_text);
		}
		
		Task iniTask = null;
		setTitle(R.string.task);
		Task task = (Task) getIntent().getSerializableExtra(
				Constants.EXTRA_TASK);
		if (task != null) {
			m_backup = task;
			iniTask = m_backup;
			textInputField.setText(task.inFileFormat());
		} else {
			if (textInputField.getText().length() == 0)
			{
				@SuppressWarnings("unchecked")
				ArrayList<Priority> prios = (ArrayList<Priority>)intent.getSerializableExtra(Constants.EXTRA_PRIORITIES_SELECTED);
				@SuppressWarnings("unchecked")
				ArrayList<String> contexts = (ArrayList<String>) intent.getSerializableExtra(Constants.EXTRA_CONTEXTS_SELECTED);
				@SuppressWarnings("unchecked")
				ArrayList<String> projects = (ArrayList<String>) intent.getSerializableExtra(Constants.EXTRA_PROJECTS_SELECTED);

				iniTask = new Task(1, "");
				iniTask.initWithFilters(prios, contexts, projects);
			}
		}
		
		textInputField.setSelection(textInputField.getText().toString()
				.length());
		
		// priorities
		priorities = (Spinner) findViewById(R.id.priority_spinner);
		final ArrayList<String> prioArr = new ArrayList<String>();
		prioArr.add(0,"-");
		prioArr.addAll(Priority.rangeInCode(Priority.A, Priority.E));
		priorities.setAdapter(Util.newSpinnerAdapter(this, prioArr));
		if (iniTask != null) {
			int index = prioArr.indexOf(iniTask.getPriority().getCode());
			priorities.setSelection(index < 0 ? 0 : index);
		}
		priorities.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long id) {
				int cursorPosition = textInputField.getSelectionStart();
				String currentText = textInputField.getText().toString();
				Priority priority = Priority.NONE;
				if (position > 0) {
					String item = prioArr.get(position);
					priority = Priority.toPriority(item);
				}
				String text = PriorityTextSplitter.getInstance().split(
						currentText).text;
				textInputField.setText(Strings.insertPadded(text, 0,
						priority.inFileFormat()));
				textInputField.setSelection(CursorPositionCalculator.calculate(
						cursorPosition, currentText, textInputField.getText()
								.toString()));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		
		
		// projects
		projects = (Spinner) findViewById(R.id.project_spinner);
		final ArrayList<String> projectsArr = taskBag.getProjects();
		projects.setAdapter(Util.newSpinnerAdapter(this, projectsArr));
		
		if (iniTask != null) {
			List<String> ps = iniTask.getProjects();
			
			if ((ps != null) && (ps.size() == 1))
			{
				int index = projectsArr.indexOf(ps.get(0));
				projects.setSelection(index < 0 ? 0 : index);
			}
		}

		projects.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long id) {
				if (position > 0) {
					int cursorPosition = textInputField.getSelectionStart();
					String currentText = textInputField.getText().toString();
					String item = "+" + projectsArr.get(position);
					textInputField.setText(Strings.insertPaddedIfNeeded(currentText,
							cursorPosition, item));
					textInputField.setSelection(CursorPositionCalculator
							.calculate(cursorPosition, currentText,
									textInputField.getText().toString()));
				}
				projects.setSelection(0);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		// contexts
		contexts = (Spinner) findViewById(R.id.context_spinner);
		final ArrayList<String> contextsArr = taskBag.getContexts();
		contexts.setAdapter(Util.newSpinnerAdapter(this, contextsArr));
		
		if (iniTask != null) {
			List<String> cs = iniTask.getContexts();
			
			if ((cs != null) && (cs.size() == 1))
			{
				int index = contextsArr.indexOf(cs.get(0));
				contexts.setSelection(index < 0 ? 0 : index);
			}
		}
		

		
		contexts.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long id) {
				if (position > 0) {
					int cursorPosition = textInputField.getSelectionStart();
					String currentText = textInputField.getText().toString();
					String item = "@" + contextsArr.get(position);
					textInputField.setText(Strings.insertPaddedIfNeeded(currentText,
							cursorPosition, item));
					textInputField.setSelection(CursorPositionCalculator
							.calculate(cursorPosition, currentText,
									textInputField.getText().toString()));
				}
				contexts.setSelection(0);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		/*
		 * After adding contexts and projects from the filter put cursor back at the beginning
		 */
		textInputField.setSelection(0);
	}

	private void addEditAsync(final String input) {
		new AsyncTask<Object, Void, Boolean>() {
			@Override
			protected void onPreExecute() {
				m_ProgressDialog = ProgressDialog.show(AddTask.this,
						getTitle(), "Please wait...", true);
			}

			@Override
			protected Boolean doInBackground(Object... params) {
				try {
					Task task = (Task) params[0];
					String input = (String) params[1];
					if (task != null) {
						task.update(input);
						taskBag.update(task);
					} else {
						taskBag.addAsTask(input);
					}

					// make widgets update
					m_app.broadcastWidgetUpdate();
					return true;
				} catch (Exception e) {
					Log.e(TAG, "input: " + input + " - " + e.getMessage());
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result) {
					String res = m_backup != null ? getString(R.string.updated_task)
							: getString(R.string.added_task);
					Util.showToastLong(AddTask.this, res);
					sendBroadcast(new Intent(
							Constants.INTENT_START_SYNC_TO_REMOTE));
					finish();
				} else {
					String res = m_backup != null ? getString(R.string.update_task_failed)
							: getString(R.string.add_task_failed);
					Util.showToastLong(AddTask.this, res);
				}
				m_ProgressDialog.dismiss();
			}
		}.execute(m_backup, input);
	}

	private void setupShortcut() {
		Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
		shortcutIntent.setClassName(this, this.getClass().getName());

		Intent intent = new Intent();
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
				getString(R.string.shortcut_addtask_name));
		Parcelable iconResource = Intent.ShortcutIconResource.fromContext(this,
				R.drawable.todotxt_touch_icon);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);

		setResult(RESULT_OK, intent);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (m_ProgressDialog != null) {
			m_ProgressDialog.dismiss();
		}
	}
}
