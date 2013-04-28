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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class Preferences extends PreferenceActivity {
	final static String TAG = Preferences.class.getSimpleName();

	private Preference aboutDialog;
	private Preference logoutDialog;
	private Preference archiveDialog;
	private static final int ABOUT_DIALOG = 1;
	private static final int LOGOUT_DIALOG = 2;
	private static final int ARCHIVE_DIALOG = 3;
	public static final int RESULT_SYNC_LIST = 2;

	private String version;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		PackageInfo packageInfo;
		try {
			packageInfo = getPackageManager().getPackageInfo(getPackageName(),
					0);
			Preference versionPref = (Preference) findPreference("app_version");
			versionPref.setSummary("v" + packageInfo.versionName);
			version = packageInfo.versionName;

		} catch (NameNotFoundException e) {
			// e.printStackTrace();
		}
		aboutDialog = findPreference("app_version");
		logoutDialog = findPreference("logout_dropbox");
		archiveDialog = findPreference("archive_now");
	}

	protected void onResume() {
		super.onResume();
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen screen,
			Preference preference) {
		if (preference == aboutDialog) {
			showDialog(ABOUT_DIALOG);
		} else if (preference == logoutDialog) {
			showDialog(LOGOUT_DIALOG);
		} else if (preference == archiveDialog) {
			showDialog(ARCHIVE_DIALOG);
		}
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == ABOUT_DIALOG) {
			AlertDialog.Builder aboutAlert = new AlertDialog.Builder(this);
			aboutAlert.setTitle("Todo.txt v" + version);
			aboutAlert
					.setMessage("by Gina Trapani &\nthe Todo.txt community\n\nhttp://todotxt.com");
			aboutAlert.setIcon(R.drawable.todotxt_touch_icon);
			aboutAlert.setPositiveButton("Follow us",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
							Intent i = new Intent(Intent.ACTION_VIEW);
							i.setData(Uri
									.parse("https://mobile.twitter.com/todotxt"));
							startActivity(i);
						}
					});
			aboutAlert.setNegativeButton("Close",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
						}
					});
			return aboutAlert.show();
		} else if (id == LOGOUT_DIALOG) {
			AlertDialog.Builder logoutAlert = new AlertDialog.Builder(this);
			logoutAlert.setTitle(R.string.areyousure);
			logoutAlert.setMessage(R.string.dropbox_logout_explainer);
			logoutAlert.setPositiveButton(R.string.dropbox_logout_pref_title,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							((TodoApplication) getApplication())
									.getRemoteClientManager().getRemoteClient()
									.deauthenticate();
							Preferences.this.setResult(RESULT_SYNC_LIST);

							// produce a logout intent and broadcast it
							Intent broadcastLogoutIntent = new Intent();
							broadcastLogoutIntent
									.setAction("com.todotxt.todotxttouch.ACTION_LOGOUT");
							sendBroadcast(broadcastLogoutIntent);
							finish();
						}
					});
			logoutAlert.setNegativeButton(R.string.cancel, null);
			return logoutAlert.show();
		} else if (id == ARCHIVE_DIALOG) {
			AlertDialog.Builder archiveAlert = new AlertDialog.Builder(this);
			archiveAlert.setTitle(R.string.archive_now_title);
			archiveAlert.setMessage(R.string.archive_now_explainer);
			archiveAlert.setPositiveButton(R.string.archive_now_pref_title,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Preferences.this.setResult(RESULT_OK);

							// produce a archive intent and broadcast it
							Intent broadcastArchiveIntent = new Intent();
							broadcastArchiveIntent
									.setAction("com.todotxt.todotxttouch.ACTION_ARCHIVE");
							sendBroadcast(broadcastArchiveIntent);
							finish();
						}
					});
			archiveAlert.setNegativeButton(R.string.cancel, null);
			return archiveAlert.show();
		}
		return null;
	}
}
