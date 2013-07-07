/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2013 Todo.txt contributors (http://todotxt.com)
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
 * @copyright 2009-2013 Todo.txt contributors (http://todotxt.com)
 */
package com.todotxt.todotxttouch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.todotxt.todotxttouch.task.Priority;
import com.todotxt.todotxttouch.task.Task;
import com.todotxt.todotxttouch.task.TaskBag;
import com.todotxt.todotxttouch.util.Util;

public class AddTask extends SherlockActivity {

	private final static String TAG = AddTask.class.getSimpleName();

	private ProgressDialog m_ProgressDialog = null;

	private Task m_backup;

	private TodoApplication m_app;

	private TaskBag taskBag;

	private EditText textInputField;

	private String share_text;

	private ListView m_drawerList;

	private DrawerLayout m_drawerLayout;

	private LinearLayout m_tabletDrawerLayout;

	private ActionBarDrawerToggle m_drawerToggle;

	private ArrayList<String> m_lists;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private void noteToSelf(Intent intent) {
		String task = intent.getStringExtra(Intent.EXTRA_TEXT);
		taskBag.addAsTask(task);
		m_app.m_prefs.storeNeedToPush(true);
		sendBroadcast(new Intent(Constants.INTENT_START_SYNC_TO_REMOTE));
		m_app.showToast(R.string.taskadded);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (m_drawerLayout != null) {
				if (m_drawerLayout.isDrawerOpen(m_drawerList)) {
					m_drawerLayout.closeDrawer(m_drawerList);
				} else {
					m_drawerLayout.openDrawer(m_drawerList);
				}
			} else {
				finish();
			}
			break;
		case R.id.menu_save_task:
			final String input = textInputField.getText().toString();
			if (input.trim().equalsIgnoreCase("")) {
				Util.showToastLong(this, R.string.add_empty_task);
			} else {
				addEditAsync(input);
			}
			break;
		case R.id.menu_add_prio:
			showPrioMenu(findViewById(R.id.menu_add_prio));
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
		} else if ("com.google.android.gm.action.AUTO_SEND".equals(action)) {
			// Called as note to self from google search/now
			noteToSelf(intent);
			finish();
			return;
		}
		// text
		textInputField = (EditText) findViewById(R.id.taskText);
		textInputField.setGravity(Gravity.TOP);

		// Setup Navigation drawer
		m_drawerList = (ListView) findViewById(R.id.left_drawer);
		m_drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		m_tabletDrawerLayout = (LinearLayout) findViewById(R.id.tablet_drawer_layout);

		// Set the adapter for the list view
		updateNavigationDrawer();
		if (m_drawerLayout==null) {
			// Tablet landscape mode
			textInputField.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
					updateNavigationDrawer();
					return false;
				}
				
			});
		}
		
		// Set up fortune hint text
		Random rand = new Random();
		int fortune_hint_index = Math.abs(rand.nextInt()) % 5;
		int fortune_hint_text;
		switch (fortune_hint_index) {
		case 0:
			fortune_hint_text = R.string.tasktexthint0;
			break;
		case 1:
			fortune_hint_text = R.string.tasktexthint1;
			break;
		case 2:
			fortune_hint_text = R.string.tasktexthint2;
			break;
		case 3:
			fortune_hint_text = R.string.tasktexthint3;
			break;
		case 4:
			fortune_hint_text = R.string.tasktexthint4;
			break;
		default:
			fortune_hint_text = R.string.tasktexthint2;
		}

		textInputField.setHint(fortune_hint_text);

		if (share_text != null) {
			textInputField.setText(share_text);
		}

		Task iniTask = null;

		Task task = (Task) getIntent().getSerializableExtra(
				Constants.EXTRA_TASK);
		if (task != null) {
			m_backup = task;
			iniTask = m_backup;
			textInputField.setText(task.inFileFormat());
			setTitle(R.string.updatetask);
		} else {
			setTitle(R.string.addtask);

			if (textInputField.getText().length() == 0) {
				@SuppressWarnings("unchecked")
				ArrayList<Priority> prios = (ArrayList<Priority>) intent
						.getSerializableExtra(Constants.EXTRA_PRIORITIES_SELECTED);
				@SuppressWarnings("unchecked")
				ArrayList<String> contexts = (ArrayList<String>) intent
						.getSerializableExtra(Constants.EXTRA_CONTEXTS_SELECTED);
				@SuppressWarnings("unchecked")
				ArrayList<String> projects = (ArrayList<String>) intent
						.getSerializableExtra(Constants.EXTRA_PROJECTS_SELECTED);

				iniTask = new Task(1, "");
				iniTask.initWithFilters(prios, contexts, projects);
			}
		}

		textInputField.setSelection(textInputField.getText().toString()
				.length());

		textInputField.requestFocus();
	}

	private void updateNavigationDrawer() {
		m_lists = labelsInTaskbagAndText();
		if (m_lists.size() == 0) {
			if (m_drawerLayout != null) {
				// No contexts or projects, disable navigation drawer
				m_drawerLayout.setDrawerLockMode(
						DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.LEFT);
			} else {
				m_tabletDrawerLayout.setVisibility(View.GONE);
			}
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
			getSupportActionBar().setHomeButtonEnabled(false);
		} else {
			if (m_drawerLayout != null) {
				m_drawerLayout.setDrawerLockMode(
						DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.LEFT);
				m_drawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
				m_drawerLayout, /* DrawerLayout object */
				R.drawable.ic_drawer, /* nav drawer icon to replace 'Up' caret */
				R.string.quickfilter, /* "open drawer" description */
				R.string.app_label /* "close drawer" description */
				) {
					@Override
					public void onDrawerSlide(View drawerView, float slideOffset) {
						// Redraw menu to show or hide menu items
						supportInvalidateOptionsMenu();
						m_lists.clear();
						m_lists.addAll(labelsInTaskbagAndText());
						((ArrayAdapter<?>) m_drawerList.getAdapter()).notifyDataSetChanged();
						super.onDrawerSlide(drawerView, slideOffset);
					}
				};

				// Set the drawer toggle as the DrawerListener
				m_drawerLayout.setDrawerListener(m_drawerToggle);
				getSupportActionBar().setDisplayHomeAsUpEnabled(true);
				getSupportActionBar().setHomeButtonEnabled(true);
				m_drawerToggle.syncState();
			} else {
				getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			}
			m_drawerList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			m_drawerList.setAdapter(new ArrayAdapter<String>(this,
					R.layout.drawer_list_item, R.id.left_drawer_text, m_lists));

			// Set the list's click listener
			m_drawerList.setOnItemClickListener(new DrawerItemClickListener());
		}
	}

	private void showPrioMenu(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final Priority[] priorities = Priority.values();
		ArrayList<String> priorityCodes = new ArrayList<String>();

		for (Priority prio : priorities) {
			priorityCodes.add(prio.getCode());
		}

		builder.setItems(priorityCodes.toArray(new String[0]),
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int which) {
						replacePriority(priorities[which].getCode());
					}
				});

		// Create the AlertDialog
		AlertDialog dialog = builder.create();
		dialog.setTitle(R.string.assignpriority);
		dialog.show();
	}

	private void replacePriority(CharSequence newPrio) {
		// save current selection and length
		int start = textInputField.getSelectionStart();
		int end = textInputField.getSelectionEnd();
		int length = textInputField.getText().length();
		int sizeDelta;
		ArrayList<String> lines = new ArrayList<String>();
		for (String line : textInputField.getText().toString()
				.split("\\r\\n|\\r|\\n")) {
			lines.add(line);
		}

		// figure out what task the cursor is on
		CharSequence enteredText = textInputField.getText().toString();
		CharSequence textToCursor = enteredText.subSequence(0, start);
		ArrayList<String> linesBeforeCursor = new ArrayList<String>();
		for (String line : textToCursor.toString().split("\\r\\n|\\r|\\n")) {
			linesBeforeCursor.add(line);
		}
		int currentLine = 0;
		if (linesBeforeCursor.size() > 0) {
			currentLine = linesBeforeCursor.size() - 1;
		}

		Task t = new Task(0, lines.get(currentLine));
		t.setPriority(Priority.toPriority(newPrio.toString()));
		lines.set(currentLine, t.inFileFormat());
		textInputField.setText(Util.join(lines, "\n"));

		// restore selection
		sizeDelta = textInputField.getText().length() - length;
		textInputField.setSelection(start + sizeDelta, end + sizeDelta);
	}

	private void replaceTextAtSelection(CharSequence title) {
		int start = textInputField.getSelectionStart();
		int end = textInputField.getSelectionEnd();
		if (start == end && start != 0) {
			// no selection prefix with space if needed
			if (!(textInputField.getText().charAt(start - 1) == ' ')) {
				title = " " + title;
			}
		}
		textInputField.getText().replace(Math.min(start, end),
				Math.max(start, end), title, 0, title.length());
	}

	private void addEditAsync(final String input) {
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
						input = input.replaceAll("\\r\\n|\\r|\\n", " ");
						task.update(input);
						taskBag.update(task);
					} else {
						for (String text : input.split("\\r\\n|\\r|\\n")) {
							taskBag.addAsTask(text);
						}
					}

					// make widgets update
					m_app.broadcastWidgetUpdate();
					return true;
				} catch (Exception e) {
					Log.e(TAG, "input: " + input + " - " + e.getMessage());
					return false;
				}
			}

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

	/** Handle help message **/
	public void onHelpClick() {
		Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
		startActivity(intent);
	}

	private ArrayList<String> labelsInTaskbagAndText() {
		/*
		 * Returns the defined labels in the taskbag and the current task being
		 * added. This way when adding multiple tasks labels from previous lines
		 * will be available fordropdown
		 */
		Set<String> contextsSet = new LinkedHashSet<String>();
		Set<String> projectsSet = new LinkedHashSet<String>();
		Task temp = new Task(1, textInputField.getText().toString());

		ArrayList<String> contexts = taskBag.getContexts(false);
		contexts.addAll(temp.getContexts());
		Collections.sort(contexts);
		for (String item : contexts) {
			contextsSet.add("@" + item);
		}
		ArrayList<String> projects = taskBag.getProjects(false);
		projects.addAll(temp.getProjects());
		Collections.sort(projects);
		for (String item : projects) {
			projectsSet.add("+" + item);
		}
		ArrayList<String> result = new ArrayList<String>();
		result.addAll(contextsSet);
		result.addAll(projectsSet);
		return result;
	}

	private class DrawerItemClickListener implements
			AdapterView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			TextView tv = (TextView) view.findViewById(R.id.left_drawer_text);
			String itemTitle = tv.getText().toString();
			Log.v(TAG, "Clicked on drawer " + itemTitle);
			if (itemTitle.substring(0, 1).equals("@")
					&& !m_app.m_contexts.remove(itemTitle.substring(1))) {
				m_app.m_contexts = new ArrayList<String>();
				m_app.m_contexts.add(itemTitle.substring(1));
			} else if (itemTitle.substring(0, 1).equals("+")
					&& !m_app.m_projects.remove(itemTitle.substring(1))) {
				m_app.m_projects = new ArrayList<String>();
				m_app.m_projects.add(itemTitle.substring(1));
			}
			replaceTextAtSelection(itemTitle);
			m_drawerList.clearChoices();
			if (m_drawerLayout != null) {
				m_drawerLayout.closeDrawer(m_drawerList);
			}
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (m_drawerToggle!=null) {
			m_drawerToggle.onConfigurationChanged(newConfig);
		}
	}
}
