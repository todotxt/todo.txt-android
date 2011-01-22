/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/TaskHelper.java
 *
 * Copyright (c) 2009-2011 mathias, Gina Trapani
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
 * @author mathias <mathias[at]ws7862[dot](none)>
 * @author Gina Trapani <ginatrapani[at]gmail[dot]com>
 * @author mathias <mathias[at]x2[dot](none)>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 mathias, Gina Trapani
 */
package com.todotxt.todotxttouch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class TaskHelper {

	private final static String TAG = TaskHelper.class.getSimpleName();

	public final static char NONE = '-';

	public final static String COMPLETED = "x ";

	public final static SimpleDateFormat DATEFORMAT = new SimpleDateFormat(
			"yyyy-MM-dd ");

	private final static Pattern prioPattern = Pattern
			.compile("\\(([A-Z])\\) (.*)");

	private final static Pattern contextPattern = Pattern.compile("(?:^|\\s)@(\\w+)");

	private final static Pattern projectPattern = Pattern.compile("\\+(\\w+)");

	private final static Pattern prependedDatePattern = Pattern
			.compile("^(\\d{4})-(\\d{2})-(\\d{2}) (.*)");

	public static Task createTask(long id, String line) {
		// prio and text
		Matcher m = prioPattern.matcher(line);
		char prio = NONE;
		String text;
		String prepended_date = "";
		if (m.find()) {
			prio = m.group(1).charAt(0);
			text = m.group(2);
		} else {
			text = line;
		}
		// prepended date
		Matcher match_prepended_date = prependedDatePattern.matcher(text);
		if (match_prepended_date.find()) {
			text = match_prepended_date.group(0).substring(11);
			Log.i(TAG, "Date " + match_prepended_date.group(0).substring(0, 10)
					+ " found in line " + text);
			prepended_date = match_prepended_date.group(0).substring(0, 10);
		}
		return new Task(id, prio, prepended_date, text.trim());
	}

	public static List<String> getContexts(String text) {
		return getItems(contextPattern, text);
	}

	public static List<String> getProjects(String text) {
		return getItems(projectPattern, text);
	}

	private static List<String> getItems(Pattern p, String text) {
		Matcher m = p.matcher(text);
		List<String> projects = new ArrayList<String>();
		while (m.find()) {
			String project = m.group(1);
			projects.add(project);
		}
		return projects;
	}

	public static Comparator<Task> byId = new Comparator<Task>() {
		@Override
		public int compare(Task arg0, Task arg1) {
			if (arg0 != null && arg1 != null) {
				return Long.valueOf(arg0.id).compareTo(arg1.id);
			}
			return -1;
		}
	};

	public static Comparator<Task> byPrio = new Comparator<Task>() {
		@Override
		public int compare(Task arg0, Task arg1) {
			if (arg0 != null && arg1 != null) {
				if (arg0.prio == TaskHelper.NONE
						|| arg1.prio == TaskHelper.NONE) {
					// Put prio NONE last.
					return new Character(arg1.prio).compareTo(new Character(
							arg0.prio));
				} else {
					return new Character(arg0.prio).compareTo(new Character(
							arg1.prio));
				}
			}
			return -1;
		}
	};

	public static Comparator<Task> byText = new Comparator<Task>() {
		@Override
		public int compare(Task arg0, Task arg1) {
			try {
				return arg0.text.compareTo(arg1.text);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
			return -1;
		}
	};

	public static String toString(char prio) {
		return prio >= 'A' && prio <= 'Z' ? "" + prio : "";
	}

	public static List<Task> getByPrio(List<Task> items, char prio) {
		List<Task> res = new ArrayList<Task>();
		for (Task item : items) {
			if (item.prio == prio) {
				res.add(item);
			}
		}
		return res;
	}

	public static List<Task> getByPrio(List<Task> items, List<String> prios) {
		List<Task> res = new ArrayList<Task>();
		for (Task item : items) {
			if (prios.contains("" + item.prio)) {
				res.add(item);
			}
		}
		return res;
	}

	public static List<Task> getByContext(List<Task> items, String context) {
		List<Task> res = new ArrayList<Task>();
		for (Task item : items) {
			if (item.contexts.contains(context)) {
				res.add(item);
			}
		}
		return res;
	}

	public static List<Task> getByContext(List<Task> items,
			List<String> contexts) {
		List<Task> res = new ArrayList<Task>();
		for (Task item : items) {
			for (String cxt : item.contexts) {
				if (contexts.contains(cxt)) {
					res.add(item);
					break;
				}
			}
		}
		return res;
	}

	public static List<Task> getByProject(List<Task> items,
			List<String> projects) {
		List<Task> res = new ArrayList<Task>();
		for (Task item : items) {
			for (String cxt : item.projects) {
				if (projects.contains(cxt)) {
					res.add(item);
					break;
				}
			}
		}
		return res;
	}

	public static List<Task> getByText(List<Task> items, String text) {
		List<Task> res = new ArrayList<Task>();
		for (Task item : items) {
			if (item.text.contains(text)) {
				res.add(item);
			}
		}
		return res;
	}

	public static List<Task> getByTextIgnoreCase(List<Task> items, String text) {
		text = text.toUpperCase();
		List<Task> res = new ArrayList<Task>();
		for (Task item : items) {
			if (item.text.toUpperCase().contains(text)) {
				res.add(item);
			}
		}
		return res;
	}

	public static ArrayList<String> getPrios(List<Task> items) {
		Set<String> res = new HashSet<String>();
		for (Task item : items) {
			res.add(toString(item.prio));
		}
		ArrayList<String> ret = new ArrayList<String>(res);
		Collections.sort(ret);
		return ret;
	}

	public static ArrayList<String> getContexts(List<Task> items) {
		Set<String> res = new HashSet<String>();
		for (Task item : items) {
			res.addAll(item.contexts);
		}
		ArrayList<String> ret = new ArrayList<String>(res);
		Collections.sort(ret);
		return ret;
	}

	public static ArrayList<String> getProjects(List<Task> items) {
		Set<String> res = new HashSet<String>();
		for (Task item : items) {
			res.addAll(item.projects);
		}
		ArrayList<String> ret = new ArrayList<String>(res);
		Collections.sort(ret);
		return ret;
	}

	public static String toFileFormat(Task task) {
		StringBuilder sb = new StringBuilder();
		if (!TaskHelper.isDeleted(task)) {
			if (!TaskHelper.isCompleted(task)) {
				if (task.prio >= 'A' && task.prio <= 'Z') {
					sb.append("(");
					sb.append(task.prio);
					sb.append(") ");
				}
				if (!task.prepended_date.equalsIgnoreCase("")) {
					sb.append(task.prepended_date + " ");
				}
			}
			sb.append(task.text);
		}
		return sb.toString();
	}

	/**
	 * @param tasks
	 * @param task
	 * @return old task or null
	 */
	public static Task updateById(List<Task> tasks, Task task) {
		for (Task task2 : tasks) {
			if (task.id == task2.id) {
				Task backup = copy(task2);
				copy(task, task2);
				return backup;
			}
		}
		return null;
	}

	public static Task copy(Task src) {
		return new Task(src.id, src.prio, src.prepended_date, src.text);
	}

	public static void copy(Task src, Task dest) {
		dest.id = src.id;
		dest.contexts = src.contexts;
		dest.prio = src.prio;
		dest.projects = src.projects;
		dest.text = src.text;
	}

	public static Task find(List<Task> tasks, Task task) {
		for (Task task2 : tasks) {
			if (task2.text.equals(task.text) && task2.prio == task.prio) {
				return copy(task2);
			}
		}
		return null;
	}

	public static boolean isDeleted(Task task) {
		return Util.isEmpty(task.text);
	}

	public static boolean isCompleted(Task task) {
		return task.text.startsWith(COMPLETED);
	}

}
