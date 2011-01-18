/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/Preferences.java
 *
 * Copyright (c) 2009-2011 Gina Trapani, mathias
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
 * @author Gina Trapani <ginatrapani[at]gmail[dot]com>
 * @author mathias <mathias[at]x2[dot](none)>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 Gina Trapani, mathias
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
	private Preference aboutDialog;
	private Preference logoutDialog;
	private static final int ABOUT_DIALOG = 1;
	private static final int LOGOUT_DIALOG = 2;
	public static final int RESULT_SYNC_LIST = 2;
	private String version;

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

	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen screen,
			Preference preference) {
		if (preference == aboutDialog) {
			showDialog(ABOUT_DIALOG);
		} else if (preference == logoutDialog) {
			showDialog(LOGOUT_DIALOG);
		}

		return true;

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == ABOUT_DIALOG) {
			AlertDialog.Builder aboutAlert = new AlertDialog.Builder(this);
			aboutAlert.setTitle("Todo.txt Touch");
			aboutAlert
					.setMessage("Version "
							+ version
							+ "\nBy Gina Trapani &\nthe Todo.txt community\ntodotxt@yahoogroups.com");
			aboutAlert.setIcon(R.drawable.icon_crystal_clear_checkmark);
			aboutAlert.setPositiveButton("Homepage",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
							Intent i = new Intent(Intent.ACTION_VIEW);
							i.setData(Uri.parse("http://todotxt.com"));
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
									.unlinkDropbox();
							Preferences.this.setResult(RESULT_SYNC_LIST);
							Preferences.this.finish();
						}
					});
			logoutAlert.setNegativeButton(R.string.cancel, null);
			return logoutAlert.show();
		}
		return null;

	}
}
