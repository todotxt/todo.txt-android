package com.todotxt.todotxttouch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.DialogPreference;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

public class LogoutPreference extends DialogPreference {
    private Context mContext;

    public LogoutPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mContext = context;
}

    public LogoutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public LogoutPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LogoutPreference(Context context) {
        this(context, null);
    }


    public AlertDialog displayLogoutDialog(Context context, final TodoApplication app) {
        AlertDialog.Builder logoutAlert = new AlertDialog.Builder(context);


        logoutAlert.setTitle(R.string.areyousure);
            SpannableStringBuilder ss = new SpannableStringBuilder();

            if (app.m_prefs.needToPush()) {
                ss.append(context.getResources().getString(R.string.dropbox_logout_warning));
                ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.append("\n\n");
            }

            ss.append(context.getResources().getString(R.string.dropbox_logout_explainer));
            logoutAlert.setMessage(ss);

            logoutAlert.setPositiveButton(R.string.dropbox_logout_pref_title,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            app
                                    .getRemoteClientManager().getRemoteClient()
                                    .deauthenticate();

                            // produce a logout intent and broadcast it
                            Intent broadcastLogoutIntent = new Intent();
                            broadcastLogoutIntent
                                    .setAction(Constants.INTENT_ACTION_LOGOUT);
                            app.sendBroadcast(broadcastLogoutIntent);
//                            finish();
                        }
                    });

            logoutAlert.setNegativeButton(R.string.cancel, null);

            logoutAlert.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @SuppressWarnings("deprecation")
                @Override
                public void onCancel(DialogInterface dialog) {
//                    removeDialog(id);
                }
            });

            return logoutAlert.show();
    }

}

/*
AlertDialog.Builder logoutAlert = new AlertDialog.Builder(this);
//
 */
