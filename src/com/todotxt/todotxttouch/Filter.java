/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/Filter.java
 *
 * Copyright (c) 2009-2011 mathias, Gina Trapani
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
 * @author mathias <mathias[at]x2[dot](none)>
 * @author Gina Trapani <ginatrapani[at]gmail[dot]com>
 * @author mathias <mathias[at]ws7862[dot](none)>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 mathias, Gina Trapani
 */
package com.todotxt.todotxttouch;

import java.util.ArrayList;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;

public class Filter extends TabActivity {

	private final static String TAG = Filter.class.getSimpleName();
	private static ArrayList<String> appliedFilters = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TabHost tabHost = getTabHost();

		LayoutInflater.from(this).inflate(R.layout.filter,
				tabHost.getTabContentView(), true);

		tabHost.addTab(tabHost
				.newTabSpec(getString(R.string.filter_tab_priorities))
				.setIndicator(getString(R.string.filter_tab_priorities))
				.setContent(R.id.priorities));
		tabHost.addTab(tabHost
				.newTabSpec(getString(R.string.filter_tab_projects))
				.setIndicator(getString(R.string.filter_tab_projects))
				.setContent(R.id.projects));
		tabHost.addTab(tabHost
				.newTabSpec(getString(R.string.filter_tab_contexts))
				.setIndicator(getString(R.string.filter_tab_contexts))
				.setContent(R.id.contexts));
		tabHost.addTab(tabHost
				.newTabSpec(getString(R.string.filter_tab_search))
				.setIndicator(getString(R.string.filter_tab_search))
				.setContent(R.id.search));

		Intent data = getIntent();
		ArrayList<String> priosArr = data
				.getStringArrayListExtra(Constants.EXTRA_PRIORITIES);
		ArrayList<String> projectsArr = data
				.getStringArrayListExtra(Constants.EXTRA_PROJECTS);
		ArrayList<String> contextsArr = data
				.getStringArrayListExtra(Constants.EXTRA_CONTEXTS);

		ArrayList<String> priosArrSelected = data
				.getStringArrayListExtra(Constants.EXTRA_PRIORITIES_SELECTED);
		ArrayList<String> projectsArrSelected = data
				.getStringArrayListExtra(Constants.EXTRA_PROJECTS_SELECTED);
		ArrayList<String> contextsArrSelected = data
				.getStringArrayListExtra(Constants.EXTRA_CONTEXTS_SELECTED);

		String searchTerm = data.getStringExtra(Constants.EXTRA_SEARCH);

		final ListView priorities = (ListView) findViewById(R.id.prioritieslv);
		priorities.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		priorities.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_multiple_choice, priosArr));
		setSelected(priorities, priosArrSelected);

		final ListView projects = (ListView) findViewById(R.id.projectslv);
		projects.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		projects.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_multiple_choice, projectsArr));
		setSelected(projects, projectsArrSelected);

		final ListView contexts = (ListView) findViewById(R.id.contextslv);
		contexts.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		contexts.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_multiple_choice, contextsArr));
		setSelected(contexts, contextsArrSelected);

		final EditText search = (EditText) findViewById(R.id.searchet);
		search.setText(searchTerm);

		Button ok = (Button) findViewById(R.id.ok);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "onClick OK");
				Intent data = new Intent();
				data.putStringArrayListExtra(Constants.EXTRA_PRIORITIES,
						getItems(priorities, "Priority"));
				data.putStringArrayListExtra(Constants.EXTRA_PROJECTS,
						getItems(projects, "Project"));
				data.putStringArrayListExtra(Constants.EXTRA_CONTEXTS,
						getItems(contexts, "Context"));
				data.putExtra(Constants.EXTRA_SEARCH, search.getText()
						.toString());
				data.putStringArrayListExtra(Constants.EXTRA_APPLIED_FILTERS,
						appliedFilters);
				setResult(Activity.RESULT_OK, data);
				finish();
			}
		});

		Button cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "onClick Cancel");
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});

		Button clear = (Button) findViewById(R.id.clear);
		clear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "onClick Clear");
				appliedFilters = new ArrayList<String>();
				setSelected(priorities, null);
				setSelected(projects, null);
				setSelected(contexts, null);
				search.setText("");
			}
		});
	}

	private static ArrayList<String> getItems(ListView adapter, String type) {
		ArrayList<String> arr = new ArrayList<String>();
		int size = adapter.getCount();
		for (int i = 0; i < size; i++) {
			if (adapter.isItemChecked(i)) {
				arr.add((String) adapter.getAdapter().getItem(i));
				Log.v(TAG, " Adding "
						+ (String) adapter.getAdapter().getItem(i)
						+ " to applied filters.");
				if (!appliedFilters.contains(type)) {
					appliedFilters.add(type);
				}
			}
		}
		return arr;
	}

	private static void setSelected(ListView lv, ArrayList<String> selected) {
		int count = lv.getCount();
		for (int i = 0; i < count; i++) {
			String str = (String) lv.getItemAtPosition(i);
			lv.setItemChecked(i, selected != null && selected.contains(str));
		}
	}

}
