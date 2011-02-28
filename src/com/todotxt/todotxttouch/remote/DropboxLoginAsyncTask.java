/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/remote/DropboxLoginAsyncTask.java
 *
 * Copyright (c) 2009-2011 Tormod Haugen
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
 * @author Tormod Haugen <tormodh[at]gmail[dot]com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 Tormod Haugen
 */
package com.todotxt.todotxttouch.remote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.dropbox.client.DropboxAPI;
import com.dropbox.client.DropboxAPI.Config;
import com.todotxt.todotxttouch.Constants;
import com.todotxt.todotxttouch.R;

class DropboxLoginAsyncTask extends AsyncTask<Void, Void, Integer>
		implements RemoteLoginTask {

	private DropboxRemoteClient dropboxRemoteClient;
    private SharedPreferences sharedPreferences;
	private String m_username;
	private String m_password;

	public DropboxLoginAsyncTask(DropboxRemoteClient dropboxRemoteClient, SharedPreferences sharedPreferences) {
        this.dropboxRemoteClient = dropboxRemoteClient;
        this.sharedPreferences = sharedPreferences;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		if (!dropboxRemoteClient.isAuthenticated()) {
			dropboxRemoteClient.login(m_username, m_password);

			final Config config = dropboxRemoteClient.getConfig();
			if (config.authStatus != DropboxAPI.STATUS_SUCCESS)
				return config.authStatus;
		}
		DropboxAPI api = dropboxRemoteClient.getAPI();
		Log.d("Xtra", "" + api.accountInfo());
		if (!api.accountInfo().isError()) {
			return DropboxAPI.STATUS_SUCCESS;
		} else {
			return DropboxAPI.STATUS_FAILURE;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		final Config config = dropboxRemoteClient.getConfig();

		if (result == DropboxAPI.STATUS_SUCCESS) {
			if (null != config
					&& config.authStatus == DropboxAPI.STATUS_SUCCESS) {
				storeKeys(config.accessTokenKey, config.accessTokenSecret);
				dropboxRemoteClient.showToast("Logged into Dropbox");
				Intent broadcastLoginIntent = new Intent();
				broadcastLoginIntent
						.setAction("com.todotxt.todotxttouch.ACTION_LOGIN");
				dropboxRemoteClient.sendBroadcast(broadcastLoginIntent);
			}
		} else {
			if (result == DropboxAPI.STATUS_NETWORK_ERROR) {
				dropboxRemoteClient.showToast("Network error: " + config.authDetail);
			} else {
				dropboxRemoteClient.showToast("Unsuccessful login.");
			}
		}
	}

	public void storeKeys(String accessTokenKey, String accessTokenSecret) {
		Editor editor = sharedPreferences.edit();
		editor.putString(Constants.PREF_ACCESSTOKEN_KEY, accessTokenKey);
		editor.putString(Constants.PREF_ACCESSTOKEN_SECRET, accessTokenSecret);
		editor.commit();
	}

	public void showLoginDialog(Activity act) {
		LayoutInflater inflator = LayoutInflater.from(act);
		View v = inflator.inflate(R.layout.logindialog, null);
		final TextView usernameTV = (TextView) v.findViewById(R.id.username);
		final TextView passwordTV = (TextView) v.findViewById(R.id.password);

		TextView mTextSample = (TextView) v.findViewById(R.id.register_hint);
		mTextSample.setMovementMethod(LinkMovementMethod.getInstance());
		String text = "No account? Create one at <a href=\"http://dropbox.com/m/register\">Dropbox</a>.";
		mTextSample.setText(Html.fromHtml(text));
		mTextSample.setFocusable(false);

		AlertDialog.Builder b = new AlertDialog.Builder(act);
		b.setView(v);
		b.setTitle(R.string.dropbox_authentication);
		b.setCancelable(true);
		b.setPositiveButton(R.string.login_button, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String u = usernameTV.getText().toString();
				String p = passwordTV.getText().toString();
				if (u != null && u.length() > 0 && p != null && p.length() > 0) {
					m_username = u;
                    m_password = p;
					execute();
				} else {
					cancel(false);
				}
			}
		});
		b.show();
	}
}