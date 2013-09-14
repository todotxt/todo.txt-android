/**
 * This file is part of Todo.txt for Android, an app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 *
 * LICENSE:
 *
 * Todo.txt for Android is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt for Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt for Android. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Todo.txt for Android's source code is available at https://github.com/ginatrapani/todo.txt-android
 *
 * @author Todo.txt for Android contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 */

package com.todotxt.todotxttouch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.todotxt.todotxttouch.task.Priority;
import com.todotxt.todotxttouch.task.Sort;
import com.todotxt.todotxttouch.util.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class TodoPreferences {
	final static String TAG = TodoPreferences.class.getSimpleName();

	private SharedPreferences m_prefs;

	/*
	 * Preference keys that aren't used in the resource file should be defined
	 * as constants
	 */
	public static final String PREF_FIRSTRUN = "firstrun";
	public static final String PREF_VERSION = "versionCode";
	public static final String PREF_ACCESSTOKEN_KEY = "accesstokenkey";
	public static final String PREF_ACCESSTOKEN_SECRET = "accesstokensecret";
	public static final String PREF_TODO_REV = "todo_rev";
	public static final String PREF_DONE_REV = "done_rev";
	public static final String PREF_NEED_TO_PUSH = "need_to_push";
	public static final String PREF_SORT = "sort";
	public static final String PREF_FILTER_PRIOS = "filter_prios";
	public static final String PREF_FILTER_CONTEXTS = "filter_contexts";
	public static final String PREF_FILTER_PROJECTS = "filter_projects";
	public static final String PREF_FILTER_SEARCH = "filter_search";
	public static final String PREF_FILTER_SUMMARY = "filter_summary";

	/* Localizable defaults that should be defined in strings.xml */
	private String todo_path_default;

	/*
	 * Keys that are used in resources should be defined in keys.xml and read
	 * into variables in the constructor
	 */
	private String auto_archive_pref_key;
	private String todo_path_key;
	private String prepend_date_pref_key;
	private String periodic_sync_pref_key;

	public TodoPreferences(Context c, SharedPreferences prefs) {
		m_prefs = prefs;

		auto_archive_pref_key = c.getString(R.string.auto_archive_pref_key);
		periodic_sync_pref_key = c.getString(R.string.periodic_sync_pref_key);
		prepend_date_pref_key = c.getString(R.string.prepend_date_pref_key);
		todo_path_key = c.getString(R.string.todo_path_key);
		todo_path_default = c.getString(R.string.todo_path_default);
		// dump();
	}

	/*
	 * Accessor methods for preference keys go here
	 */

	public String getPrependDatePrefKey() {
		return prepend_date_pref_key;
	}

	public String getPeriodicSyncPrefKey() {
		return periodic_sync_pref_key;
	}

	public String getTodoPathKey() {
		return todo_path_key;
	}

	/*
	 * Accessor methods for preference values go here
	 */

	public String getAccessToken() {
		return m_prefs.getString(PREF_ACCESSTOKEN_KEY, null);
	}

	public String getAccessTokenSecret() {
		return m_prefs.getString(PREF_ACCESSTOKEN_SECRET, null);
	}

	public void storeAccessToken(String accessTokenKey, String accessTokenSecret) {
		Editor editor = m_prefs.edit();
		editor.putString(PREF_ACCESSTOKEN_KEY, accessTokenKey);
		editor.putString(PREF_ACCESSTOKEN_SECRET, accessTokenSecret);
		editor.commit();
	}

	public Boolean isAutoArchiveEnabled() {
		return m_prefs.getBoolean(auto_archive_pref_key, false);
	}

	public String getFileRevision(String key) {
		return m_prefs.getString(key, null);
	}

	public void storeFileRevision(String key, String rev) {
		Log.d(TAG, "Storing rev. key=" + key + ". val=" + rev);

		Editor editor = m_prefs.edit();
		editor.putString(key, rev);
		editor.commit();
	}

	public Boolean isFirstRun() {
		return m_prefs.getBoolean(PREF_FIRSTRUN, true);
	}

	public void storeFirstRun(boolean value) {
		Editor editor = m_prefs.edit();
		editor.putBoolean(PREF_FIRSTRUN, value);
		editor.commit();
	}

	public Boolean isPrependDateEnabled() {
		return m_prefs.getBoolean(prepend_date_pref_key, true);
	}

	public boolean isManualModeEnabled() {
		try {
			long period = Long.parseLong(m_prefs.getString(
					periodic_sync_pref_key, "0"));

			return period < 0;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	public boolean needToPush() {
		return m_prefs.getBoolean(PREF_NEED_TO_PUSH, false);
	}

	public void storeNeedToPush(boolean needToPush) {
		Editor editor = m_prefs.edit();
		editor.putBoolean(PREF_NEED_TO_PUSH, needToPush);
		editor.commit();
	}

	public long getSyncPeriod() {
		try {
			long period = Long.parseLong(m_prefs.getString(
					periodic_sync_pref_key, "0"));

			return period > 0 ? period : 0;
		} catch (NumberFormatException ex) {
			return 0L;
		}
	}

	public void storeSort(Sort sort) {
		Editor editor = m_prefs.edit();
		editor.putInt(PREF_SORT, sort.getId());
		editor.commit();
	}

	public Sort getSort() {
		return Sort.getById(m_prefs.getInt(PREF_SORT,
				Sort.PRIORITY_DESC.getId()));
	}

	public void storeFilters(Collection<Priority> prios,
			Collection<?> contexts, Collection<?> projects, String search,
			Collection<?> summaries) {
		Editor editor = m_prefs.edit();

		if (prios != null) {
			editor.putString(PREF_FILTER_PRIOS,
					Util.join(Priority.inCode(prios), " "));
		}

		if (contexts != null) {
			editor.putString(PREF_FILTER_CONTEXTS, Util.join(contexts, " "));
		}

		if (projects != null) {
			editor.putString(PREF_FILTER_PROJECTS, Util.join(projects, " "));
		}

		if (search != null) {
			editor.putString(PREF_FILTER_SEARCH, search);
		}

		if (search != null) {
			// split on tab just in case there is a space in the text
			editor.putString(PREF_FILTER_SUMMARY, Util.join(summaries, "\t"));
		}

		editor.commit();
	}

	public ArrayList<Priority> getFilteredPriorities() {
		return Priority.toPriority(Util.split(
				m_prefs.getString(PREF_FILTER_PRIOS, ""), " "));
	}

	public ArrayList<String> getFilteredContexts() {
		return Util.split(m_prefs.getString(PREF_FILTER_CONTEXTS, ""), " ");
	}

	public ArrayList<String> getFilteredProjects() {
		return Util.split(m_prefs.getString(PREF_FILTER_PROJECTS, ""), " ");
	}

	public String getSearch() {
		return m_prefs.getString(PREF_FILTER_SEARCH, "");
	}

	public ArrayList<String> getFilterSummaries() {
		// split on tab just in case there is a space in the text
		return Util.split(m_prefs.getString(PREF_FILTER_SUMMARY, ""), "\t");
	}

	public String getTodoFilePath() {
		return m_prefs.getString(todo_path_key, todo_path_default);
	}

	/**
	 * Returns the most recent version to have successfully run upgrade tasks.
	 * Returns 0 if upgrade tasks have never been run.
	 * 
	 * @return version code in the same format as in the manifest file.
	 */
	public int getVersion() {
		return m_prefs.getInt(PREF_VERSION, 0);
	}

	/**
	 * Store the current application version so that we know when we have been
	 * upgraded.
	 * 
	 * @param versionCode
	 *            version code in the same format as in the manifest file.
	 */
	public void storeVersion(int versionCode) {
		Editor editor = m_prefs.edit();
		editor.putInt(PREF_VERSION, versionCode);
		editor.commit();
	}

	/*
	 * Utility methods go here
	 */

	public void clearState() {
		Editor editor = m_prefs.edit();
		editor.remove(PREF_FIRSTRUN);
		editor.remove(PREF_DONE_REV);
		editor.remove(PREF_TODO_REV);
		editor.remove(PREF_NEED_TO_PUSH);
		editor.commit();
	}

	public void clear() {
		Editor editor = m_prefs.edit();
		editor.clear();
		editor.commit();
	}

	public void registerOnSharedPreferenceChangeListener(
			SharedPreferences.OnSharedPreferenceChangeListener listener) {
		m_prefs.registerOnSharedPreferenceChangeListener(listener);
	}

	public void unregisterOnSharedPreferenceChangeListener(
			SharedPreferences.OnSharedPreferenceChangeListener listener) {
		m_prefs.unregisterOnSharedPreferenceChangeListener(listener);
	}

	public void dump() {
		Map<String, ?> keys = m_prefs.getAll();

		for (Map.Entry<String, ?> entry : keys.entrySet()) {
			Log.d("map values", entry.getKey() + ": "
					+ entry.getValue().toString());
		}
	}
}
