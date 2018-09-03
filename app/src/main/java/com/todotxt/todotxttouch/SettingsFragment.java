package com.todotxt.todotxttouch;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int ABOUT_DIALOG = 1;
    private static final int LOGOUT_DIALOG = 2;
    TodoApplication m_app;
    private Preference aboutDialog;
    private Preference logoutDialog;
    private ListPreference periodicSync;
    private String version;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        m_app = (TodoApplication) getActivity().getApplication();
        ((CheckBoxPreference) findPreference(m_app.m_prefs
                .getPrependDatePrefKey())).setChecked(m_app.m_prefs
                .isPrependDateEnabled());

        PackageInfo packageInfo;

        try {
            packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(),
                    0);
            Preference versionPref = findPreference("app_version");
            versionPref.setSummary("v" + packageInfo.versionName);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // e.printStackTrace();
        }

        aboutDialog = findPreference("app_version");
        logoutDialog = findPreference("logout_dropbox");
        periodicSync = (ListPreference) findPreference(m_app.m_prefs
                .getPeriodicSyncPrefKey());
        setPeriodicSummary(periodicSync.getValue());
        periodicSync
                .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference,
                                                      Object newValue) {
                        setPeriodicSummary(newValue);
                        return true;
                    }
                });
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }

    private void setPeriodicSummary(Object newValue) {
        // Sync preference summary with selected entry. Ugly but this is the
        // only way that works.
        periodicSync.setSummary(periodicSync.getEntries()[periodicSync
                .findIndexOfValue((String) newValue)]);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {

        if (preference instanceof TodoLocationPreference) {

            TodoLocationPreferenceFragment dialogFragment = TodoLocationPreferenceFragment.newInstance(preference.getKey());
            dialogFragment.setApp((TodoApplication) getActivity().getApplication());
            dialogFragment.setDisplayWarning(m_app.m_prefs.needToPush());
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), "android.support.v7.preference" +
                    ".PreferenceFragment.DIALOG");
        } else if (preference instanceof LogoutPreference) {
            // bit hacky but it's overkill to do the whole subclass PreferenceDialogFragmentCompat
            // just for this
            ((LogoutPreference) preference).displayLogoutDialog(getContext(), m_app);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }


//        if (id == ABOUT_DIALOG) {
//            AlertDialog.Builder aboutAlert = new AlertDialog.Builder(this);
//            aboutAlert.setTitle("Todo.txt v" + version);
//            aboutAlert
//                    .setMessage("by Gina Trapani &\nthe Todo.txt community\n\nhttp://todotxt.com");
//            aboutAlert.setIcon(R.drawable.todotxt_touch_icon);
//
//            aboutAlert.setPositiveButton("Follow us",
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface arg0, int arg1) {
//                            Intent i = new Intent(Intent.ACTION_VIEW);
//                            i.setData(Uri
//                                    .parse("https://mobile.twitter.com/todotxt"));
//                            startActivity(i);
//                        }
//                    });
//
//            aboutAlert.setNegativeButton("Close",
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface arg0, int arg1) {
//                        }
//                    });
//
//            aboutAlert.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                @SuppressWarnings("deprecation")
//                @Override
//                public void onCancel(DialogInterface dialog) {
//                    removeDialog(id);
//                }
//            });
//
//            return aboutAlert.create();
//        } else if (id == LOGOUT_DIALOG) {
//            AlertDialog.Builder logoutAlert = new AlertDialog.Builder(this);
//            logoutAlert.setTitle(R.string.areyousure);
//            SpannableStringBuilder ss = new SpannableStringBuilder();
//
//            if (m_app.m_prefs.needToPush()) {
//                ss.append(getString(R.string.dropbox_logout_warning));
//                ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(),
//                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                ss.append("\n\n");
//            }
//
//            ss.append(getString(R.string.dropbox_logout_explainer));
//            logoutAlert.setMessage(ss);
//
//            logoutAlert.setPositiveButton(R.string.dropbox_logout_pref_title,
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            ((TodoApplication) getApplication())
//                                    .getRemoteClientManager().getRemoteClient()
//                                    .deauthenticate();
//
//                            // produce a logout intent and broadcast it
//                            Intent broadcastLogoutIntent = new Intent();
//                            broadcastLogoutIntent
//                                    .setAction(Constants.INTENT_ACTION_LOGOUT);
//                            sendBroadcast(broadcastLogoutIntent);
//                            finish();
//                        }
//                    });
//
//            logoutAlert.setNegativeButton(R.string.cancel, null);
//
//            logoutAlert.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                @SuppressWarnings("deprecation")
//                @Override
//                public void onCancel(DialogInterface dialog) {
//                    removeDialog(id);
//                }
//            });
//
//            return logoutAlert.create();
//        }
    }
}
