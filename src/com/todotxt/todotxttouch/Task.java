package com.todotxt.todotxttouch;

import java.util.List;

public class Task {

	public int id;

	public int prio;

	public String text;

	public List<String> contexts;

	public List<String> projects;

	public List<String> tags;
	
	public boolean deleted;

	public Task(int id, int prio, String taskDescription,
			List<String> contexts, List<String> projects, List<String> tags) {
		this.id = id;
		this.prio = prio;
		this.text = taskDescription;
		this.contexts = contexts;
		this.projects = projects;
		this.tags = tags;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[id=").append(id).append("]");
		sb.append("[prio=").append(prio).append("]");
		sb.append("[taskDescription=").append(text).append("]");
		//contexts
		sb.append("[contexts:");
		for (String cxt : contexts) {
			sb.append("[context=").append(cxt).append("]");
		}
		sb.append("]");
		//projects
		sb.append("[projects:");
		for (String prj : projects) {
			sb.append("[project=").append(prj).append("]");
		}
		sb.append("]");
		//tags
		sb.append("[tags:");
		for (String tag : tags) {
			sb.append("[tag=").append(tag).append("]");
		}
		sb.append("]");
		return sb.toString();
	}

}
