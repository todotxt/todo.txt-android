package nl.mpcjanssen.todotxtholo;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnTouchListener;

@SuppressWarnings("unused")
public class FilterActivity extends Activity {

	private FilterListFragment prioritiesFragment;
	private FilterListFragment projectsFragment;
	private FilterListFragment contextsFragment;
	private ActionBar actionbar;

	private GestureDetector gestureDetector;

	private OnTouchListener gestureListener;

	@Override
	protected void onDestroy () {
		super.onDestroy();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filter);

		actionbar = getActionBar();
		ActionBar.Tab tab = null;
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		//save the original intent at first call from main screen



		//Create the fragments
		prioritiesFragment = new FilterListFragment();
		prioritiesFragment.setArguments(
				getIntent().getStringArrayListExtra(Constants.EXTRA_PRIORITIES),
				getIntent().getStringArrayListExtra(Constants.EXTRA_PRIORITIES_SELECTED),
				R.layout.tab_priorities, R.id.prioritieslv, actionbar);
		projectsFragment = new FilterListFragment();
		projectsFragment.setArguments(
				getIntent().getStringArrayListExtra(Constants.EXTRA_PROJECTS),
				getIntent().getStringArrayListExtra(Constants.EXTRA_PROJECTS_SELECTED),
				R.layout.tab_projects, R.id.projectslv, actionbar);
		contextsFragment = new FilterListFragment();
		contextsFragment.setArguments(
				getIntent().getStringArrayListExtra(Constants.EXTRA_CONTEXTS),
				getIntent().getStringArrayListExtra(Constants.EXTRA_CONTEXTS_SELECTED),
				R.layout.tab_contexts, R.id.contextslv, actionbar);


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

	// Safe the active tab on configuration changes
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putInt("active_tab", actionbar.getSelectedNavigationIndex());
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		actionbar.setSelectedNavigationItem(savedInstanceState.getInt("active_tab"));
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
		}		

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			ft.replace(R.id.fragment_container, fragment);
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		}

	}
}


