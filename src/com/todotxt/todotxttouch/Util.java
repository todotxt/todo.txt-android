package com.todotxt.todotxttouch;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Util {

	private static String TAG = Util.class.getSimpleName();

	private static final int CONNECTION_TIMEOUT = 120000;

	private static final int SOCKET_TIMEOUT = 120000;

	private Util() {
	}
	
	public static boolean isEmpty(String in){
		return in == null || in.length() == 0;
	}

	public static HttpParams getTimeoutHttpParams() {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);
		return params;
	}

	public static void closeStream(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
				stream = null;
			} catch (IOException e) {
				Log.w(TAG, "Close stream exception", e);
			}
		}
	}
	
	public static InputStream getInputStreamFromUrl(String url)
			throws ClientProtocolException, IOException {
		HttpGet request = new HttpGet(url);
		DefaultHttpClient client = new DefaultHttpClient(getTimeoutHttpParams());
		HttpResponse response = client.execute(request);
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			Log.e(TAG, "Failed to get stream for: " + url);
			throw new IOException("Failed to get stream for: " + url);
		}
		return response.getEntity().getContent();
	}

	public static String fetchContent(String url)
			throws ClientProtocolException, IOException {
		InputStream input = getInputStreamFromUrl(url);
		try {
			int c;
			byte[] buffer = new byte[8192];
			StringBuilder sb = new StringBuilder();
			while ((c = input.read(buffer)) != -1) {
				sb.append(new String(buffer, 0, c));
			}
			return sb.toString();
		} finally {
			closeStream(input);
		}
	}

	public static void showToastLong(Context cxt, int resid) {
		Toast.makeText(cxt, resid, Toast.LENGTH_LONG).show();
	}

	public static void showToastShort(Context cxt, int resid) {
		Toast.makeText(cxt, resid, Toast.LENGTH_SHORT).show();
	}

	public static void showToastLong(Context cxt, String msg) {
		Toast.makeText(cxt, msg, Toast.LENGTH_LONG).show();
	}

	public static void showToastShort(Context cxt, String msg) {
		Toast.makeText(cxt, msg, Toast.LENGTH_SHORT).show();
	}

	public interface OnMultiChoiceDialogListener{
		void onClick(boolean[] selected);
	}

	public static Dialog createMultiChoiceDialog(Context cxt,
			CharSequence[] keys, boolean[] values, Integer titleId,
			Integer iconId, final OnMultiChoiceDialogListener listener) {
		final boolean[] res;
		if(values == null){
			res = new boolean[keys.length];
		}else{
			res = values;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
		if(iconId != null){
			builder.setIcon(iconId);
		}
		if(titleId != null){
			builder.setTitle(titleId);
		}
		builder.setMultiChoiceItems(keys, values,
				new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton, boolean isChecked) {
						res[whichButton] = isChecked;
					}
				});
		builder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						listener.onClick(res);
					}
				});
		builder.setNegativeButton(R.string.cancel, null);
		return builder.create();
	}

	public static void showDialog(Context cxt, int titleid, int msgid) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
		builder.setTitle(titleid);
		builder.setMessage(msgid);
		builder.setPositiveButton(android.R.string.ok, null);
		builder.setCancelable(true);
		builder.show();
	}

	public static void showDialog(Context cxt, int titleid, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
		builder.setTitle(titleid);
		builder.setMessage(msg);
		builder.setPositiveButton(android.R.string.ok, null);
		builder.setCancelable(true);
		builder.show();
	}
	
	public static boolean isDeviceWritable()
	{
		String sdState = Environment.getExternalStorageState();
		if(Environment.MEDIA_MOUNTED.equals(sdState)){
			return true;
		}else{
			return false;
		}
	}
	
	public static boolean isDeviceReadable()
	{
		String sdState = Environment.getExternalStorageState();
		if(Environment.MEDIA_MOUNTED.equals(sdState) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(sdState)){
			return true;
		}else{
			return false;
		}
	}

	public interface InputDialogListener {
		void onClick(String input);
	}

	public static void showInputDialog(Context cxt, int titleid, int msgid,
			String defaulttext, int lines,
			final InputDialogListener oklistener, int icon) {
		LayoutInflater factory = LayoutInflater.from(cxt);
		final View textEntryView = factory.inflate(R.layout.inputdialog, null);
		final TextView textinput = (TextView) textEntryView
				.findViewById(R.id.input);
		textinput.setText(defaulttext);
		textinput.setLines(lines);
		AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
		if (icon > 0) {
			builder.setIcon(icon);
		}
		builder.setTitle(titleid);
		builder.setMessage(msgid);
		builder.setView(textEntryView);
		builder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String input = textinput.getText().toString();
						oklistener.onClick(input);
					}
				});
		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});
		builder.setCancelable(true);
		builder.show();
	}

}
