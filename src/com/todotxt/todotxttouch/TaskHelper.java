package com.todotxt.todotxttouch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskHelper {
	
	public final static char NONE = '-';

	public final static String COMPLETED = "x ";

	private final static Pattern prioPattern = Pattern.compile("\\(([A-Z])\\) (.*)");

	private final static Pattern contextPattern = Pattern.compile("@(\\w+)");

	private final static Pattern projectPattern = Pattern.compile("\\+(\\w+)");

	private final static Pattern tagPattern = Pattern.compile("#(\\w+)");

	public static Task createTask(long id, String line){
		//prio and text
		Matcher m = prioPattern.matcher(line);
		char prio = NONE;
		String text;
		if(m.find()){
			prio = m.group(1).charAt(0);
			text = m.group(2);
		}else{
			text = line;
		}
		return new Task(id, prio, text.trim());
	}
	
	public static List<String> getContexts(String text){
		return getItems(contextPattern, text);
	}

	public static List<String> getProjects(String text){
		return getItems(projectPattern, text);
	}

	public static List<String> getTags(String text){
		return getItems(tagPattern, text);
	}

	private static List<String> getItems(Pattern p, String text){
		Matcher m = p.matcher(text);
		List<String> projects = new ArrayList<String>();
		while(m.find()){
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
				return new Integer(arg0.prio).compareTo(new Integer(arg1.prio));
			}
			return -1;
		}
	};

	public static String toString(char prio) {
		return prio >= 'A' && prio <= 'Z' ? "" + prio : "" + NONE;
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
			if (prios.contains(""+item.prio)) {
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

	public static List<Task> getByProject(List<Task> items, List<String> projects) {
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

	public static List<Task> getByTag(List<Task> items, List<String> tags) {
		List<Task> res = new ArrayList<Task>();
		for (Task item : items) {
			for (String cxt : item.tags) {
				if (tags.contains(cxt)) {
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
			if(item.text.contains(text)){
				res.add(item);
			}
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

	public static Set<String> getContexts(List<Task> items){
		Set<String> res = new HashSet<String>();
		for (Task item : items) {
			res.addAll(item.contexts);
		}
		return res;
	}

	public static Set<String> getProjects(List<Task> items){
		Set<String> res = new HashSet<String>();
		for (Task item : items) {
			res.addAll(item.projects);
		}
		return res;
	}

	public static Set<String> getTags(List<Task> items){
		Set<String> res = new HashSet<String>();
		for (Task item : items) {
			res.addAll(item.tags);
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
		if(!task.deleted) {
			if(task.completed){
				sb.append(COMPLETED);
			}else{
				if (task.prio >= 'A' && task.prio <= 'Z') {
					sb.append("(");
					sb.append(task.prio);
					sb.append(") ");
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
	public static Task updateById(List<Task> tasks, Task task){
		for (Task task2 : tasks) {
			if(task.id == task2.id){
				Task backup = copy(task2);
				copy(task, task2);
				return backup;
			}
		}
		return null;
	}

	public static Task copy(Task src){
		return new Task(src.id, src.prio, src.text);
	}
	
	public static void copy(Task src, Task dest){
		dest.id = src.id;
		dest.deleted = src.deleted;
		dest.contexts = src.contexts;
		dest.prio = src.prio;
		dest.projects = src.projects;
		dest.tags = src.tags;
		dest.text = src.text;
	}

}
