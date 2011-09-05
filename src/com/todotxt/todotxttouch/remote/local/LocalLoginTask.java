package com.todotxt.todotxttouch.remote.local;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.dropbox.client.DropboxAPI;
import com.dropbox.client.DropboxAPI.Config;
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
