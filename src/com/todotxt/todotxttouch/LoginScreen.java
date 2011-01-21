package com.todotxt.todotxttouch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.dropbox.client.DropboxAPI;

public class LoginScreen extends Activity {

	final static String TAG = TodoTxtTouch.class.getSimpleName();

	private TodoApplication m_app;
	private Button m_LoginButton;
	private BroadcastReceiver m_broadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login);
		
		m_app = (TodoApplication) getApplication();
		
		// supposed to help with the banding on the green background
		findViewById(R.id.loginbackground).getBackground().setDither(true);
		
		IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction("com.todotxt.todotxttouch.ACTION_LOGIN");
	    m_broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
    			Intent i = new Intent(context, TodoTxtTouch.class);
    			startActivity(i);
                finish();
            }
        };
	    registerReceiver(m_broadcastReceiver, intentFilter);
	    
		m_LoginButton = (Button)findViewById(R.id.login);
		m_LoginButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				login();
			}
		});
		
		final DropboxAPI api = m_app.getAPI();
		if (api.isAuthenticated() && m_app.m_loggedIn) {
			Intent intent = new Intent(this, TodoTxtTouch.class);
			startActivity(intent);
			finish();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(m_broadcastReceiver);
	}
	
	void login() {
		final DropboxAPI api = m_app.getAPI();
		if (api.isAuthenticated() && !m_app.m_loggedIn) {
			DropboxLoginAsyncTask loginTask = new DropboxLoginAsyncTask(m_app,
					m_app.getConfig());
			loginTask.execute();
		} else {
			DropboxLoginAsyncTask loginTask = new DropboxLoginAsyncTask(m_app,
					m_app.getConfig());
			loginTask.showLoginDialog(this);
		}
	}
}
