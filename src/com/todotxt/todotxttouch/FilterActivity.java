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

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class FilterActivity extends TabActivity {

	private final static String TAG = FilterActivity.class.getSimpleName();
	private static ArrayList<String> appliedFilters = new ArrayList<String>();
	private TabHost mTabHost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTabHost = getTabHost();

		Drawable actionBarBg = getResources().getDrawable(
				R.drawable.title_background);
		mTabHost.getTabWidget().setBackgroundDrawable(actionBarBg);

		LayoutInflater.from(this).inflate(R.layout.filter,
				mTabHost.getTabContentView(), true);

		mTabHost.addTab(mTabHost
				.newTabSpec(getString(R.string.filter_tab_priorities))
				// .setIndicator(getString(R.string.filter_tab_priorities))
				.setIndicator(buildIndicator(R.string.filter_tab_priorities))
				.setContent(R.id.priorities));
		mTabHost.addTab(mTabHost
				.newTabSpec(getString(R.string.filter_tab_projects))
				.setIndicator(buildIndicator(R.string.filter_tab_projects))
				.setContent(R.id.projects));
		mTabHost.addTab(mTabHost
				.newTabSpec(getString(R.string.filter_tab_contexts))
				.setIndicator(buildIndicator(R.string.filter_tab_contexts))
				.setContent(R.id.contexts));
		mTabHost.addTab(mTabHost
				.newTabSpec(getString(R.string.filter_tab_search))
				.setIndicator(buildIndicator(R.string.filter_tab_search))
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
				R.layout.simple_list_item_multiple_choice, priosArr));
		setSelected(priorities, priosArrSelected);

		final ListView projects = (ListView) findViewById(R.id.projectslv);
		projects.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		projects.setAdapter(new ArrayAdapter<String>(this,
				R.layout.simple_list_item_multiple_choice, projectsArr));
		setSelected(projects, projectsArrSelected);

		final ListView contexts = (ListView) findViewById(R.id.contextslv);
		contexts.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		contexts.setAdapter(new ArrayAdapter<String>(this,
				R.layout.simple_list_item_multiple_choice, contextsArr));
		setSelected(contexts, contextsArrSelected);

		final EditText search = (EditText) findViewById(R.id.searchet);
		search.setText(searchTerm);

		Button ok = (Button) findViewById(R.id.ok);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "onClick OK");
				Intent data = new Intent();
				Log.v(TAG, "Clearing all filter types.");
				appliedFilters = new ArrayList<String>();
				data.putStringArrayListExtra(Constants.EXTRA_PRIORITIES,
						getItems(priorities, getString(R.string.filter_tab_priorities)));
				data.putStringArrayListExtra(Constants.EXTRA_PROJECTS,
						getItems(projects, getString(R.string.filter_tab_projects)));
				data.putStringArrayListExtra(Constants.EXTRA_CONTEXTS,
						getItems(contexts, getString(R.string.filter_tab_contexts)));
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

	private View buildIndicator(int textRes) {
		final TextView indicator = (TextView) this.getLayoutInflater().inflate(
				R.layout.tab_indicator, mTabHost.getTabWidget(), false);
		indicator.setText(textRes);
		return indicator;
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
					Log.v(TAG, " Adding " + type + " to applied filter types.");
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
