package com.todotxt.todotxttouch;

import java.io.File;

import android.os.Environment;

public class Constants {

	public static final String PREF_FIRSTRUN = "firstrun";
	public static final String PREF_ACCESSTOKEN_KEY = "accesstokenkey";
	public static final String PREF_ACCESSTOKEN_SECRET = "accesstokensecret";

	public static final String REMOTE_FILE = "/todo.txt";

	public final static File TODOFILE = new File(
			Environment.getExternalStorageDirectory(),
			"data/com.todotxt.todotxttouch/todo.txt");

	public final static File TODOFILETMP = new File(
			Environment.getExternalStorageDirectory(),
			"data/com.todotxt.todotxttouch/tmp/todo.txt");

	public final static long INVALID_ID = -1;
	public final static int INVALID_POSITION = -1;

	public final static String EXTRA_PRIORITIES = "PRIORITIES";
	public final static String EXTRA_PRIORITIES_SELECTED = "PRIORITIES_SELECTED";
	public final static String EXTRA_PROJECTS = "PROJECTS";
	public final static String EXTRA_PROJECTS_SELECTED = "PROJECTS_SELECTED";
	public final static String EXTRA_CONTEXTS = "CONTEXTS";
	public final static String EXTRA_CONTEXTS_SELECTED = "CONTEXTS_SELECTED";
	public final static String EXTRA_SEARCH = "SEARCH";
	public final static String EXTRA_TASK = "TASK";

}
