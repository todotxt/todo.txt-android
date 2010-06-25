package com.todotxt.todotxttouch;

import java.io.File;

import android.os.Environment;

public class Constants {

	public static final String CONSUMER_KEY = "24uvvbp09jcgkv0";
	public static final String CONSUMER_SECRET = "arl24x8l1jgqy7p";
	
	public static final String REMOTE_FILE = "todo.txt";

	public final static File TODOFILE = new File(Environment
			.getExternalStorageDirectory(),
			"data/com.todotxt.todotxttouch/todo.txt");
	
	public final static File TODOFILETEMP = new File(Environment
			.getExternalStorageDirectory(),
			"data/com.todotxt.todotxttouch/todo.tmp");
	
}
