/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/LoginScreen.java
 *
 * Copyright (c) 2009-2011 Hrayr Artunyan
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
 * @author Hrayr Artunyan <hrayr[dot]artunyan[at]gmail[dot]com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 Hrayr Artunyan
 */
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
