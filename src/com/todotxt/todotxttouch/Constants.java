/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/Constants.java
 *
 * Copyright (c) 2009-2011 mathias, Gina Trapani, Tormod Haugen
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
 * @author mathias <mathias[at]x2[dot](none)>
 * @author Gina Trapani <ginatrapani[at]gmail[dot]com>
 * @author Tormod Haugen <tormodh[at]gmail[dot]com>
 * @author mathias <mathias[at]ws7862[dot](none)>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 mathias, Gina Trapani, Tormod Haugen
 */
package com.todotxt.todotxttouch;

public class Constants {

	public static final String PREF_FIRSTRUN = "firstrun";
	public static final String PREF_ACCESSTOKEN_KEY = "accesstokenkey";
	public static final String PREF_ACCESSTOKEN_SECRET = "accesstokensecret";
	public static final String DROPBOX_MODUS = "dropbox";

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
	public final static String EXTRA_APPLIED_FILTERS = "APPLIED_FITERS";

	public final static String INTENT_ACTION_LOGOUT = "com.todotxt.todotxttouch.ACTION_LOGOUT";
	public final static String INTENT_ASYNC_SUCCESS = "com.todotxt.todotxttouch.ASYNC_SUCCESS";
	public final static String INTENT_ASYNC_FAILED = "com.todotxt.todotxttouch.ASYNC_FAILED";
	public final static String INTENT_START_SYNC_TO_REMOTE = "com.todotxt.todotxttouch.START_SYNC_TO";
	public final static String INTENT_START_SYNC_FROM_REMOTE = "com.todotxt.todotxttouch.START_SYNC_FROM";
	public final static String INTENT_GO_OFFLINE = "com.todotxt.todotxttouch.GO_OFFLINE";
	public final static String INTENT_UPDATE_UI = "com.todotxt.todotxttouch.UPDATE_UI";
}
