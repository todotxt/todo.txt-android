package com.todotxt.todotxttouch;

import com.dropbox.client.DropboxAPI;
import com.dropbox.client.DropboxAPI.Config;

import android.os.AsyncTask;

public class DropboxLoginAsyncTask extends AsyncTask<Void, Void, Integer>{

	private TodoTxtTouch m_act;
	private Config m_config;
	private String m_username;
	private String m_password;

	public DropboxLoginAsyncTask(TodoTxtTouch act, Config config) {
		m_act = act;
		m_config = config;
	}

	public DropboxLoginAsyncTask(TodoTxtTouch act, String username, String password, Config config) {
		m_act = act;
		m_config = config;
		m_username = username;
		m_password = password;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		DropboxAPI api = m_act.getAPI();
		
		if ( !api.isAuthenticated() ) {
			m_config = api.authenticate(m_config, m_username, m_password);
			m_act.setConfig(m_config);
		}
		
		if ( m_config == null ) return 0;
		return 1;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		Util.showToastLong(m_act, "Returned " + result);
		m_act.populateFromExternal();
	}

	
}
