package com.todotxt.todotxttouch;

import com.dropbox.client.DropboxAPI;
import com.dropbox.client.DropboxAPI.Config;

import android.os.AsyncTask;

public class DropboxLoginAsyncTask extends AsyncTask<Void, Void, Integer> {

	private TodoTxtTouch m_act;
	private Config m_config;
	private String m_username;
	private String m_password;

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
}
