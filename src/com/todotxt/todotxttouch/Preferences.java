package com.todotxt.todotxttouch;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

public class Preferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		// Get the custom preference
		Preference customPref = (Preference) findPreference("editTextPref");
		customPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						SharedPreferences customSharedPreference = getSharedPreferences(
								"myCustomSharedPrefs", Activity.MODE_PRIVATE);
						SharedPreferences.Editor editor = customSharedPreference
								.edit();
						editor.putString("myCustomPref",
								"The preference has been clicked");
						editor.commit();
						return true;
					}

				});
	}
}
