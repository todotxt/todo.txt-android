package com.todotxt.todotxttouch;

import java.util.List;

public class Task {

	public long id;

	public char prio;

	public String text;

	public List<String> contexts;

	public List<String> projects;

	public List<String> tags;

	public Task(long id, char prio, String text) {
		this.id = id;
		this.prio = prio;
		this.text = text;
		this.contexts = TaskHelper.getContexts(text);
		this.projects = TaskHelper.getProjects(text);
		this.tags = TaskHelper.getTags(text);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[id=").append(id).append("]");
		sb.append("[prio=").append(prio).append("]");
		sb.append("[text=").append(text).append("]");
		sb.append("[deleted=").append(TaskHelper.isDeleted(this)).append("]");
		sb.append("[completed=").append(TaskHelper.isCompleted(this)).append("]");
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
