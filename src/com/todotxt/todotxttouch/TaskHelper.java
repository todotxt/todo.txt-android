package com.todotxt.todotxttouch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskHelper {
	
	public static final String NONE = "-";

	public static Comparator<Task> byId = new Comparator<Task>() {
		@Override
		public int compare(Task arg0, Task arg1) {
			if (arg0 != null && arg1 != null) {
				return Integer.valueOf(arg0.id).compareTo(arg1.id);
			}
			return -1;
		}
	};

	public static Comparator<Task> byPrio = new Comparator<Task>() {
		@Override
		public int compare(Task arg0, Task arg1) {
			if (arg0 != null && arg1 != null) {
				return new Integer(arg0.prio).compareTo(new Integer(arg1.prio));
			}
			return -1;
		}
	};

	public static int parsePrio(String s) {
		if (!Util.isEmpty(s) && !NONE.equals(s)) {
			char c = s.charAt(0);
			return (c - 'A') + 1;
		}
		return 0;
	}

	public static void appendPrio(StringBuilder sb, int prio) {
		if (prio > 0) {
			sb.append((char) ('A' + (prio - 1)));
		}
	}

	public static String toString(int prio) {
		return prio > 0 ? "" + (char) ('A' + (prio - 1)) : NONE;
	}

	public static List<Task> getByPrio(List<Task> items, int prio) {
		List<Task> res = new ArrayList<Task>();
		for (Task item : items) {
			if (item.prio == prio) {
				res.add(item);
			}
		}
		return res;
	}

	public static List<Task> getByPrio(List<Task> items, List<Integer> prios) {
		List<Task> res = new ArrayList<Task>();
		for (Task item : items) {
			if (prios.contains(item.prio)) {
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

	public static List<Task> getByContext(List<Task> items, List<String> contexts) {
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

	public static Set<String> getContexts(List<Task> items){
		Set<String> res = new HashSet<String>();
		for (Task item : items) {
			res.addAll(item.contexts);
		}
		return res;
	}

	public static Set<String> getPrios(List<Task> items){
		Set<String> res = new HashSet<String>();
		for (Task item : items) {
			res.add(toString(item.prio));
		}
		return res;
	}

	public static String getContextsAsString(Task task){
		StringBuilder sb = new StringBuilder();
		for (String cxt : task.contexts) {
			sb.append("[").append(cxt).append("] ");
		}
		return sb.toString();
	}

	public static String toFileFormat(Task task) {
		StringBuilder sb = new StringBuilder();
		if (task.prio > 0) {
			sb.append("(");
			TaskHelper.appendPrio(sb, task.prio);
			sb.append(") ");
		}
		sb.append(task.text);
		return sb.toString();
	}

	public static String toDisplayFormat(Task task) {
		StringBuilder sb = new StringBuilder();
		sb.append(task.id).append(" ");
		if (task.prio > 0) {
			sb.append("(");
			TaskHelper.appendPrio(sb, task.prio);
			sb.append(") ");
		}
		sb.append(task.text);
		return sb.toString();
	}

}
