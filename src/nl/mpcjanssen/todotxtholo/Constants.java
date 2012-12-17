/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2012 Todo.txt contributors (http://todotxt.com)
 *
 * LICENSE:
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
 * @author Todo.txt contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2012 Todo.txt contributors (http://todotxt.com)
 */
package nl.mpcjanssen.todotxtholo;

public class Constants {
	
	public static final int SORT_ORIGINAL = 0;
	public static final int SORT_REVERSE = 1;

	public static final String PREF_FIRSTRUN = "firstrun";
	public static final String PREF_ACCESSTOKEN_KEY = "accesstokenkey";
	public static final String PREF_ACCESSTOKEN_SECRET = "accesstokensecret";
	public static final String PREF_TODO_REV = "todo_rev";
	public static final String PREF_DONE_REV = "done_rev";
	public static final String PREF_MANUAL_MODE = "workofflinepref";
	public static final String PREF_NEED_TO_PUSH = "need_to_push";
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
	public final static String EXTRA_FORCE_SYNC = "FORCE_SYNC";
	public final static String EXTRA_OVERWRITE = "OVERWRITE";

	public final static String INTENT_ACTION_ARCHIVE = "nl.mpcjanssen.todotxtholo.ACTION_ARCHIVE";
	public final static String INTENT_ACTION_LOGOUT = "nl.mpcjanssen.todotxtholo.ACTION_LOGOUT";
	public final static String INTENT_ASYNC_SUCCESS = "nl.mpcjanssen.todotxtholo.ASYNC_SUCCESS";
	public final static String INTENT_ASYNC_FAILED = "nl.mpcjanssen.todotxtholo.ASYNC_FAILED";
	public final static String INTENT_SYNC_CONFLICT = "nl.mpcjanssen.todotxtholo.SYNC_CONFLICT";
	public final static String INTENT_START_SYNC_WITH_REMOTE = "nl.mpcjanssen.todotxtholo.START_SYNC";
	public final static String INTENT_START_SYNC_TO_REMOTE = "nl.mpcjanssen.todotxtholo.START_SYNC_TO";
	public final static String INTENT_START_SYNC_FROM_REMOTE = "nl.mpcjanssen.todotxtholo.START_SYNC_FROM";
	public final static String INTENT_SET_MANUAL = "nl.mpcjanssen.todotxtholo.GO_OFFLINE";
	public final static String INTENT_UPDATE_UI = "nl.mpcjanssen.todotxtholo.UPDATE_UI";
	public final static String INTENT_WIDGET_UPDATE = "nl.mpcjanssen.todotxtholo.APPWIDGET_UPDATE";
	public final static String INTENT_SYNC_START = "nl.mpcjanssen.todotxtholo.SYNC_START";
	public final static String INTENT_SYNC_DONE = "nl.mpcjanssen.todotxtholo.SYNC_DONE";
}
