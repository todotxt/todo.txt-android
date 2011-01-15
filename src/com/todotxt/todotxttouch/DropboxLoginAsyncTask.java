/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/DropboxLoginAsyncTask.java
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
package com.todotxt.todotxttouch;

import com.dropbox.client.DropboxAPI;
import com.dropbox.client.DropboxAPI.Config;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class DropboxLoginAsyncTask extends AsyncTask<Void, Void, Integer> {

	private TodoTxtTouch m_act;
	private Config m_config;
	private String m_username;
	private String m_password;

	public void setUsername(String username) {
		m_username = username;
	}
	public void setPassword(String password) {
		m_password = password;
	}

	public DropboxLoginAsyncTask(TodoTxtTouch act, Config config) {
		m_act = act;
		m_config = config;
	}

	public DropboxLoginAsyncTask(TodoTxtTouch act, String username,
			String password, Config config) {
		m_act = act;
		m_config = config;
		m_username = username;
		m_password = password;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		DropboxAPI api = m_act.getAPI();

		if (!api.isAuthenticated()) {
			m_config = api.authenticate(m_config, m_username, m_password);
			m_act.setConfig(m_config);

			if (m_config.authStatus != DropboxAPI.STATUS_SUCCESS)
				return m_config.authStatus;
		}

		if (!api.accountInfo().isError()) {
			return DropboxAPI.STATUS_SUCCESS;
		} else {
			return DropboxAPI.STATUS_FAILURE;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);

		if (result == DropboxAPI.STATUS_SUCCESS) {
			if (m_config != null
					&& m_config.authStatus == DropboxAPI.STATUS_SUCCESS) {
				m_act.storeKeys(m_config.accessTokenKey,
						m_config.accessTokenSecret);
				m_act.setLoggedIn(true);
				m_act.showToast("Logged into Dropbox");
			}
		} else {
			if (result == DropboxAPI.STATUS_NETWORK_ERROR) {
				m_act.showToast("Network error: " + m_config.authDetail);
			} else {
				m_act.showToast("Unsuccessful login.");
			}
		}
	}

	public void showLoginDialog() {
		LayoutInflater inflator = LayoutInflater.from(m_act);
		View v = inflator.inflate(R.layout.logindialog, null);
		final TextView usernameTV = (TextView) v.findViewById(R.id.username);
		final TextView passwordTV = (TextView) v.findViewById(R.id.password);
		
		AlertDialog.Builder b = new AlertDialog.Builder(m_act);
		b.setView(v);
		b.setTitle(R.string.dropbox_authentication);
		b.setCancelable(true);
		b.setPositiveButton(R.string.login_button, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String u = usernameTV.getText().toString();
				String p = passwordTV.getText().toString();
				if ( u != null && u.length() > 0 && p != null && p.length() > 0 ) {
					DropboxLoginAsyncTask.this.setUsername(u);
					DropboxLoginAsyncTask.this.setPassword(p);
					DropboxLoginAsyncTask.this.execute();
				} else {
					DropboxLoginAsyncTask.this.cancel(false);
				}
			}
		});
		b.show();
	}

}