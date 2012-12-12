package com.todotxt.todotxttouch;

import java.util.ArrayList;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

@SuppressWarnings("unused")
public class FilterActivity extends FragmentActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	private FilterListFragment prioritiesFragment;
	private FilterListFragment projectsFragment;
	private FilterListFragment contextsFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filter);

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
		
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
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

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			switch (position) {
			case 0: return prioritiesFragment;
			case 1: return projectsFragment;
			case 2: return contextsFragment;
			}
			return null;
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getString(R.string.priority_prompt).toUpperCase(Locale.getDefault());
			case 1:
				return getString(R.string.project_prompt).toUpperCase(Locale.getDefault());
			case 2:
				return getString(R.string.context_prompt).toUpperCase(Locale.getDefault());
			case 3:
				return getString(R.string.search).toUpperCase(Locale.getDefault());
			}
			return null;
		}
	}

}
