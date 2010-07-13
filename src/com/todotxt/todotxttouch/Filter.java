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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TabHost tabHost = getTabHost();

		LayoutInflater.from(this).inflate(R.layout.filter,
				tabHost.getTabContentView(), true);

		tabHost.addTab(tabHost.newTabSpec("Priorities").setIndicator("Priorities")
				.setContent(R.id.priorities));
		tabHost.addTab(tabHost.newTabSpec("Projects").setIndicator("Projects")
				.setContent(R.id.projects));
		tabHost.addTab(tabHost.newTabSpec("Contexts").setIndicator("Contexts")
				.setContent(R.id.contexts));
		tabHost.addTab(tabHost.newTabSpec("Tags").setIndicator("Tags")
				.setContent(R.id.tags));
		tabHost.addTab(tabHost.newTabSpec("Search").setIndicator("Search")
				.setContent(R.id.search));
		
		Intent data = getIntent();
		ArrayList<String> priosArr = data.getStringArrayListExtra(Constants.EXTRA_PRIORITIES);
		ArrayList<String> projectsArr = data.getStringArrayListExtra(Constants.EXTRA_PROJECTS);
		ArrayList<String> contextsArr = data.getStringArrayListExtra(Constants.EXTRA_CONTEXTS);
		ArrayList<String> tagsArr = data.getStringArrayListExtra(Constants.EXTRA_TAGS);

		ArrayList<String> priosArrSelected = data.getStringArrayListExtra(Constants.EXTRA_PRIORITIES_SELECTED);
		ArrayList<String> projectsArrSelected = data.getStringArrayListExtra(Constants.EXTRA_PROJECTS_SELECTED);
		ArrayList<String> contextsArrSelected = data.getStringArrayListExtra(Constants.EXTRA_CONTEXTS_SELECTED);
		ArrayList<String> tagsArrSelected = data.getStringArrayListExtra(Constants.EXTRA_TAGS_SELECTED);

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

		final ListView tags = (ListView) findViewById(R.id.tagslv);
		tags.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		tags.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_multiple_choice, tagsArr));
		setSelected(tags, tagsArrSelected);

		final EditText search = (EditText) findViewById(R.id.searchet);
		search.setText(searchTerm);

		Button ok = (Button) findViewById(R.id.ok);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "onClick OK");
				Intent data = new Intent();
				data.putStringArrayListExtra(Constants.EXTRA_PRIORITIES, getItems(priorities));
				data.putStringArrayListExtra(Constants.EXTRA_PROJECTS, getItems(projects));
				data.putStringArrayListExtra(Constants.EXTRA_CONTEXTS, getItems(contexts));
				data.putStringArrayListExtra(Constants.EXTRA_TAGS, getItems(tags));
				data.putExtra(Constants.EXTRA_SEARCH, search.getText().toString());
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
				setSelected(priorities, null);
				setSelected(projects, null);
				setSelected(contexts, null);
				setSelected(tags, null);
				search.setText("");
			}
		});
	}

	private static ArrayList<String> getItems(ListView adapter) {
		ArrayList<String> arr = new ArrayList<String>();
		int size = adapter.getCount();
		for (int i = 0; i < size; i++) {
			if(adapter.isItemChecked(i)){
				arr.add((String)adapter.getAdapter().getItem(i));
			}
		}
		return arr;
	}

	private static void setSelected(ListView lv, ArrayList<String> selected){
		int count = lv.getCount();
		for (int i = 0; i < count; i++) {
			String str = (String) lv.getItemAtPosition(i);
			lv.setItemChecked(i, selected != null && selected.contains(str));
		}
	}

}
