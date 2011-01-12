package com.todotxt.todotxttouch;

import com.dropbox.client.DropboxAPI;
import com.dropbox.client.DropboxAPI.Config;

import android.os.AsyncTask;

public class DropboxLoginAsyncTask extends AsyncTask<Void, Void, Integer>{

	private TodoApplication m_app;
	private Config m_config;
	private String m_username;
	private String m_password;

	public DropboxLoginAsyncTask(TodoApplication app, Config config) {
		m_app = app;
		m_config = config;
	}

	public DropboxLoginAsyncTask(TodoApplication app, String username, String password, Config config) {
		m_app = app;
		m_config = config;
		m_username = username;
		m_password = password;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		DropboxAPI api = m_app.getAPI();
		
		if ( !api.isAuthenticated() ) {
			m_config = api.authenticate(m_config, m_username, m_password);
			m_app.setConfig(m_config);
		}
		
		// TODO Auto-generated method stub
		return 1;
	}

}
