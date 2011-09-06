/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/remote/local/LocalLoginTask.java
 *
 * Copyright (c) 2011 Tomasz Roszko
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
 * @author Tomasz Roszko <geekonek[at]gmail[dot]com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2011 Tomasz Roszko
 */
package com.todotxt.todotxttouch.remote.local;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import com.todotxt.todotxttouch.R;
import com.todotxt.todotxttouch.remote.RemoteLoginTask;

public class LocalLoginTask implements RemoteLoginTask {

	private LocalRemoteClient localRemoteClient;

	public LocalLoginTask(LocalRemoteClient localRemoteClient) {
		this.localRemoteClient = localRemoteClient;
	}

	@Override
	public void showLoginDialog(Activity act) {
		LayoutInflater inflator = LayoutInflater.from(act);
		View v = inflator.inflate(R.layout.local_login_dialog, null);
		
		AlertDialog.Builder b = new AlertDialog.Builder(act);
		b.setView(v);
		b.setTitle(R.string.ok);
		b.setCancelable(true);
		b.setPositiveButton(R.string.login_button, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				localRemoteClient.login();
				localRemoteClient.sendBroadcast(new Intent(
						"com.todotxt.todotxttouch.ACTION_LOGIN"));
			}
		});
		b.show();

	}

}
