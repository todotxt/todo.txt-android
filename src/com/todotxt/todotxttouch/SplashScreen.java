package com.todotxt.todotxttouch;

import com.dropbox.client.DropboxAPI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SplashScreen extends Activity {

	private TodoApplication m_app;
	private final static String TAG = SplashScreen.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.logindialog);
		
		m_app = (TodoApplication) getApplication();

		if ( !m_app.m_loggedIn ) {
			login();
		} else {
		Intent intent = new Intent(this, TodoTxtTouch.class);
		startActivity(intent );
		}
	}

	private void login() {
		final DropboxAPI api = m_app.getAPI();
		if ( api.isAuthenticated() ) {
			DropboxLoginAsyncTask loginTask = new DropboxLoginAsyncTask(m_app, m_app.getConfig());
			loginTask.execute();
		} else {
			SplashScreen.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Util.LoginDialogListener dialogListener = new Util.LoginDialogListener() {
						@Override
						public void onClick(String username, String password) {
							try {
								String consumerKey = getResources().getText(R.string.dropbox_consumer_key).toString();
								String consumerSecret = getResources().getText(R.string.dropbox_consumer_secret).toString();
								Log.i(TAG, "Using Dropbox key " + consumerKey + " and secret " + consumerSecret);
								
								DropboxLoginAsyncTask loginTask = new DropboxLoginAsyncTask(m_app, username, password, m_app.getConfig());
								loginTask.execute();
								
								Intent intent = new Intent(SplashScreen.this, TodoTxtTouch.class);
								startActivity(intent );

							} catch (Exception e) {
								Log.i(TAG,
										"Could not create Dropbox client! Exception details: "
												+ e.getLocalizedMessage());
							}
						}
					};
					Util.showLoginDialog(SplashScreen.this, R.string.dropbox_authentication,
							R.string.login, "", dialogListener,
							R.drawable.menu_sync);
				}
			});

		}
	}

}
