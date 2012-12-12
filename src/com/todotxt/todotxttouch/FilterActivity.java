package com.todotxt.todotxttouch;

import java.util.ArrayList;
import java.util.Locale;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

@SuppressWarnings("unused")
public class FilterActivity extends Activity {


	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private FilterListFragment prioritiesFragment;
	private FilterListFragment projectsFragment;
	private FilterListFragment contextsFragment;

	@Override
	protected void onDestroy () {
		super.onDestroy();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filter);

		final ActionBar actionbar = getActionBar();
		ActionBar.Tab tab = null;
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		//save the original intent at first call from main screen



		//Create the fragments
		prioritiesFragment = new FilterListFragment();
		prioritiesFragment.setArguments(
				getIntent().getStringArrayListExtra(Constants.EXTRA_PRIORITIES),
				getIntent().getStringArrayListExtra(Constants.EXTRA_PRIORITIES_SELECTED),
				R.layout.tab_priorities, R.id.prioritieslv);
		projectsFragment = new FilterListFragment();
		projectsFragment.setArguments(
				getIntent().getStringArrayListExtra(Constants.EXTRA_PROJECTS),
				getIntent().getStringArrayListExtra(Constants.EXTRA_PROJECTS_SELECTED),
				R.layout.tab_projects, R.id.projectslv);
		contextsFragment = new FilterListFragment();
		contextsFragment.setArguments(
				getIntent().getStringArrayListExtra(Constants.EXTRA_CONTEXTS),
				getIntent().getStringArrayListExtra(Constants.EXTRA_CONTEXTS_SELECTED),
				R.layout.tab_contexts, R.id.contextslv);


		// Add the fragments in the action_bar
		actionbar.addTab(actionbar.newTab().setText(R.string.filter_tab_contexts)
				.setTabListener(new MyTabsListener(contextsFragment)));
		actionbar.addTab(actionbar.newTab().setText(R.string.filter_tab_projects)
				.setTabListener(new MyTabsListener(projectsFragment)));
		actionbar.addTab(actionbar.newTab().setText(R.string.filter_tab_priorities)
				.setTabListener(new MyTabsListener(prioritiesFragment)));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.filter, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_apply_filter:
			applyFilter();
			break;
		}
		return true;
	}

	private void applyFilter() {
		ArrayList<String> appliedFilters;
		Intent data = new Intent();

		appliedFilters = new ArrayList<String>();

		data.putStringArrayListExtra(Constants.EXTRA_PRIORITIES,
				prioritiesFragment.getFilters());
		data.putStringArrayListExtra(Constants.EXTRA_PROJECTS,
				projectsFragment.getFilters());
		data.putStringArrayListExtra(Constants.EXTRA_CONTEXTS,
				contextsFragment.getFilters());
		data.putStringArrayListExtra(Constants.EXTRA_APPLIED_FILTERS,
				appliedFilters);
		setResult(Activity.RESULT_OK, data);
		finish();

	}

	private class MyTabsListener implements ActionBar.TabListener {
		public Fragment fragment;

		public MyTabsListener(Fragment fragment) {
			this.fragment = fragment;
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			//	ft.replace(R.id.fragment_container, fragment);
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			ft.replace(R.id.fragment_container, fragment);
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			//	ft.remove(fragment);
		}

	}
}


