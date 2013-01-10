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


	
	private ArrayList<FilterListFragment> fragments = new ArrayList<FilterListFragment>();
	FilterListFragment fragment;

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
		fragment = new FilterListFragment();		
		fragment.setArguments(
				getIntent().getStringArrayListExtra(Constants.EXTRA_CONTEXTS),
				getIntent().getStringArrayListExtra(Constants.EXTRA_CONTEXTS_SELECTED),
				Constants.EXTRA_CONTEXTS,
				R.layout.tab_contexts, R.id.contextslv, R.string.filter_tab_contexts, actionbar);
		fragments.add(fragment);
		
		fragment = new FilterListFragment();		
		fragment.setArguments(
				getIntent().getStringArrayListExtra(Constants.EXTRA_PROJECTS),
				getIntent().getStringArrayListExtra(Constants.EXTRA_PROJECTS_SELECTED),
				Constants.EXTRA_PROJECTS,
				R.layout.tab_projects, R.id.projectslv, R.string.filter_tab_projects, actionbar);
		fragments.add(fragment);

		fragment = new FilterListFragment();
		fragment.setArguments(
				getIntent().getStringArrayListExtra(Constants.EXTRA_PRIORITIES),
				getIntent().getStringArrayListExtra(Constants.EXTRA_PRIORITIES_SELECTED),
				Constants.EXTRA_PRIORITIES,
				R.layout.tab_priorities, R.id.prioritieslv, R.string.filter_tab_priorities, actionbar);
		fragments.add(fragment);


		// Add the fragments in the action_bar
		for (FilterListFragment fragment : fragments) {
		actionbar.addTab(actionbar.newTab().setText(fragment.title())
				.setTabListener(new MyTabsListener(fragment)));
		}

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
		case R.id.menu_select_all:
			selectAll();
			break;
		}
		return true;
	}

	private void selectAll() {
		fragments.get(actionbar.getSelectedNavigationIndex()).selectAll();
		
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

		for (FilterListFragment fragment : fragments) {
		
			data.putStringArrayListExtra(fragment.getFilterName(),
				fragment.getFilters());		
		}
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


