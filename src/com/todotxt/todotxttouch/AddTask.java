/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/AddTask.java
 *
 * Copyright (c) 2009-2011 mathias, Gina Trapani, Tormod Haugen
 *
 * LICENSE:
 *
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
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
 * @author Gina Trapani <ginatrapani[at]gmail[dot]com>
 * @author mathias <mathias[at]x2[dot](none)>
 * @author Tormod Haugen <tormodh[at]gmail[dot]com>
 * @author mathias <mathias[at]ws7862[dot](none)>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 mathias, Gina Trapani, Tormod Haugen
 */
package com.todotxt.todotxttouch;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.todotxt.todotxttouch.task.Priority;
import com.todotxt.todotxttouch.task.PriorityTextSplitter;
import com.todotxt.todotxttouch.task.Task;
import com.todotxt.todotxttouch.task.TaskBag;
import com.todotxt.todotxttouch.util.CursorPositionCalculator;
import com.todotxt.todotxttouch.util.Strings;
import com.todotxt.todotxttouch.util.Util;

public class AddTask extends Activity {

	private final static String TAG = AddTask.class.getSimpleName();

	private ProgressDialog m_ProgressDialog = null;

	private Task m_backup;

	private TodoApplication m_app;
	private TaskBag taskBag;

	private TextView titleBarLabel;

	private String share_text;

	private Spinner priorities;
	private Spinner contexts;
	private Spinner projects;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.add_task);

		m_app = (TodoApplication) getApplication();
		taskBag = m_app.getTaskBag();

		final Intent intent = getIntent();
		final String action = intent.getAction();
		// create shortcut and exit
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

		// title bar label
		titleBarLabel = (TextView) findViewById(R.id.title_bar_label);

		// text
		final EditText textInputField = (EditText) findViewById(R.id.taskText);
		textInputField.setGravity(Gravity.TOP);

		if (share_text != null) {
			textInputField.setText(share_text);
		}

		Task task = (Task) getIntent().getSerializableExtra(
				Constants.EXTRA_TASK);
		if (task != null) {
			m_backup = task;
			textInputField.setText(task.inFileFormat());
			setTitle(R.string.update);
			titleBarLabel.setText(R.string.update);
		} else {
			setTitle(R.string.addtask);
			titleBarLabel.setText(R.string.addtask);
		}

		textInputField.setSelection(textInputField.getText().toString()
				.length());

		// priorities
		priorities = (Spinner) findViewById(R.id.priorities);
		final ArrayList<String> prioArr = new ArrayList<String>();
		prioArr.add("Priority");
		prioArr.addAll(Priority.rangeInCode(Priority.A, Priority.E));
		priorities.setAdapter(Util.newSpinnerAdapter(this, prioArr));
		if (m_backup != null) {
			int index = prioArr.indexOf(m_backup.getPriority().getCode());
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
		projects = (Spinner) findViewById(R.id.projects);
		final ArrayList<String> projectsArr = taskBag.getProjects();
		projectsArr.add(0, "Project");
		projects.setAdapter(Util.newSpinnerAdapter(this, projectsArr));
		projects.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long id) {
				if (position > 0) {
					int cursorPosition = textInputField.getSelectionStart();
					String currentText = textInputField.getText().toString();
					String item = "+" + projectsArr.get(position);
					textInputField.setText(Strings.insertPadded(currentText,
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
		contexts = (Spinner) findViewById(R.id.contexts);
		final ArrayList<String> contextsArr = taskBag.getContexts();
		contextsArr.add(0, "Context");
		contexts.setAdapter(Util.newSpinnerAdapter(this, contextsArr));
		contexts.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long id) {
				if (position > 0) {
					int cursorPosition = textInputField.getSelectionStart();
					String currentText = textInputField.getText().toString();
					String item = "@" + contextsArr.get(position);
					textInputField.setText(Strings.insertPadded(currentText,
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
				// strip line breaks
				final String input = textInputField.getText().toString()
						.replaceAll("\\r\\n|\\r|\\n", " ");

				new AsyncTask<Object, Void, Boolean>() {
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
							return true;
						} catch (Exception e) {
							Log.e(TAG,
									"input: " + input + " - " + e.getMessage());
							return false;
						}
					}

					protected void onPostExecute(Boolean result) {
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
						m_ProgressDialog.dismiss();
					}
				}.execute(m_backup, input);
			}
		});
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

	/** Handle priority spinner **/
	public void onPriorityClick(View v) {
		priorities.performClick();
	}

	/** Handle project spinner **/
	public void onProjectClick(View v) {
		projects.performClick();
	}

	/** Handle context spinner **/
	public void onContextClick(View v) {
		contexts.performClick();
	}

	/** Handle help message **/
	public void onHelpClick(View v) {
		Intent intent = new Intent(v.getContext(), HelpActivity.class);
		startActivity(intent);
	}
}