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
import com.todotxt.todotxttouch.task.Task;

public class TaskHelper {
	private final static String TAG = TaskHelper.class.getSimpleName();

	public static Comparator<Task> byId = new Comparator<Task>() {
		@Override
		public int compare(Task arg0, Task arg1) {
			if (arg0 != null && arg1 != null) {
				return Long.valueOf(arg0.getId()).compareTo(arg1.getId());
			}
			return -1;
		}
	};

	public static Comparator<Task> byIdReverse = new Comparator<Task>() {
		@Override
		public int compare(Task arg0, Task arg1) {
			if (arg0 != null && arg1 != null) {
				return Long.valueOf(arg1.getId()).compareTo(arg0.getId());
			}
			return -1;
		}
	};

	public static Comparator<Task> byPrio = new Comparator<Task>() {
		@Override
		public int compare(Task arg0, Task arg1) {
			if (arg0 != null && arg1 != null) {
				if (arg0.getPriority() == Task.NO_PRIORITY
						|| arg1.getPriority() == Task.NO_PRIORITY) {
					// Put prio NONE last.
					return new Character(arg1.getPriority()).compareTo(new Character(
							arg0.getPriority()));
				} else {
					return new Character(arg0.getPriority()).compareTo(new Character(
							arg1.getPriority()));
				}
			}
			return -1;
		}
	};

	public static Comparator<Task> byText = new Comparator<Task>() {
		@Override
		public int compare(Task arg0, Task arg1) {
			try {
				return arg0.getText().compareTo(arg1.getText());
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
			if (item.getPriority() == prio) {
				res.add(item);
			}
		}
		return res;
	}

	public static List<Task> getByPrio(List<Task> items, List<String> prios) {
		List<Task> res = new ArrayList<Task>();
		for (Task item : items) {
			if (prios.contains("" + item.getPriority())) {
				res.add(item);
			}
		}
		return res;
	}

	public static List<Task> getByContext(List<Task> items, String context) {
		List<Task> res = new ArrayList<Task>();
		for (Task item : items) {
			if (item.getContexts().contains(context)) {
				res.add(item);
			}
		}
		return res;
	}

	public static List<Task> getByContext(List<Task> items,
			List<String> contexts) {
		List<Task> res = new ArrayList<Task>();
		for (Task item : items) {
			for (String cxt : item.getContexts()) {
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
			for (String cxt : item.getProjects()) {
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
			if (item.getText().contains(text)) {
				res.add(item);
			}
		}
		return res;
	}

	public static List<Task> getByTextIgnoreCase(List<Task> items, String text) {
		text = text.toUpperCase();
		List<Task> res = new ArrayList<Task>();
		for (Task item : items) {
			if (item.getText().toUpperCase().contains(text)) {
				res.add(item);
			}
		}
		return res;
	}

	public static ArrayList<String> getPrios(List<Task> items) {
		Set<String> res = new HashSet<String>();
		for (Task item : items) {
			res.add(toString(item.getPriority()));
		}
		ArrayList<String> ret = new ArrayList<String>(res);
		Collections.sort(ret);
		return ret;
	}

	public static ArrayList<String> getContexts(List<Task> items) {
		Set<String> res = new HashSet<String>();
		for (Task item : items) {
			res.addAll(item.getContexts());
		}
		ArrayList<String> ret = new ArrayList<String>(res);
		Collections.sort(ret);
		return ret;
	}

	public static ArrayList<String> getProjects(List<Task> items) {
		Set<String> res = new HashSet<String>();
		for (Task item : items) {
			res.addAll(item.getProjects());
		}
		ArrayList<String> ret = new ArrayList<String>(res);
		Collections.sort(ret);
		return ret;
	}

	/**
	 * @param tasks
	 * @param task
	 * @return old task or null
	 */
	public static Task updateById(List<Task> tasks, Task task) {
		for (Task task2 : tasks) {
			if (task.getId() == task2.getId()) {
				Task backup = task2.copy();
				task.copyInto(task2);
				return backup;
			}
		}
		return null;
	}

	public static Task find(List<Task> tasks, Task task) {
		for (Task task2 : tasks) {
			if (task2.getText().equals(task.getText()) && task2.getPriority() == task.getPriority()) {
				return task2.copy();
			}
		}
		return null;
	}

}
