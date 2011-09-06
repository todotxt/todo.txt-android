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
 * @author Tomasz Roszko <geekone[at]gmail[dot]com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 Hrayr Artunyan, Tomasz Roszko
 */
package com.todotxt.todotxttouch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.todotxt.todotxttouch.remote.Client;
import com.todotxt.todotxttouch.remote.RemoteClient;
import com.todotxt.todotxttouch.remote.RemoteLoginTask;
import com.todotxt.todotxttouch.util.Util;

public class LoginScreen extends Activity {

	final static String TAG = LoginScreen.class.getSimpleName();

	private TodoApplication m_app;
	private Button m_LoginButton;
	private BroadcastReceiver m_broadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login);

		// remote provider selection
		final Spinner clientSelect = (Spinner) findViewById(R.id.remote_select);		
		ArrayAdapter<Client> adapter = new ArrayAdapter<Client>(this, 
				android.R.layout.simple_spinner_item, Client.values()){

					@Override
					public View getView(int position, View convertView,
							ViewGroup parent) {
						
						final LayoutInflater inflater = getLayoutInflater();
						final View spinnerEntry = inflater.inflate(R.layout.remote_provider_list_entry, null);
						
						final TextView providerName = (TextView) spinnerEntry.findViewById(R.id.remoteProviderName);
						final TextView providerDesc = (TextView) spinnerEntry.findViewById(R.id.remoteProviderDescription);
						
						final Client client = getItem(position);
						
						
						
						String lowerCaseName = client.name().toLowerCase();
						providerName.setText(getResources().getIdentifier("@string/remote_provider_name_"+lowerCaseName, null, getPackageName()));
						providerDesc.setText(getResources().getIdentifier("@string/remote_provider_desc_"+lowerCaseName, null, getPackageName()));
						
						return spinnerEntry;
					}
			
					@Override
					public View getDropDownView(int position, View convertView,
							ViewGroup parent) {
						return getView(position, convertView, parent);
					}
					
		};
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		clientSelect.setAdapter(adapter);
		
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

		m_LoginButton = (Button) findViewById(R.id.login);
		m_LoginButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//set new remote client based on selection
				String newClientName = ((Client)clientSelect.getSelectedItem()).name();
				boolean ret = m_app.getRemoteClientManager().setClient(newClientName);
				if (ret) {
					Log.d(TAG, "Changed remote client to: "+newClientName);
					//login
					login();
				} else {
					Log.e(TAG, "Could not change remote client to: "+newClientName);
				}
			}
		});

		
		final RemoteClient remoteClient = m_app.getRemoteClientManager()
				.getRemoteClient();
		if (remoteClient.isAuthenticated()) {
			switchToTodolist();
		}
		
	}

	private void switchToTodolist() {
		Intent intent = new Intent(this, TodoTxtTouch.class);
		startActivity(intent);
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(m_broadcastReceiver);
	}

	void login() {
		final RemoteClient client = m_app.getRemoteClientManager()
				.getRemoteClient();

		if (!client.isAvailable()) {
			Log.d(TAG, "Remote service " + client.getClass().getSimpleName()
					+ " is not available; aborting login");
			Util.showToastLong(m_app, R.string.toast_login_notconnected);
		} else {
			RemoteLoginTask loginTask = client.getLoginTask();
			loginTask.showLoginDialog(this);
		}
	}

}
