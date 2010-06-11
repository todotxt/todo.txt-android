package com.todotxt.todotxttouch;

import java.util.List;

public class Task {

	public int id;

	public int prio;

	public String taskDescription;

	public List<String> contexts;

	public Task(int id, int prio, String taskDescription, List<String> contexts) {
		this.id = id;
		this.prio = prio;
		this.taskDescription = taskDescription;
		this.contexts = contexts;
	}
	
	public String getContextsAsString(){
		StringBuilder sb = new StringBuilder();
		for (String cxt : contexts) {
			sb.append("[").append(cxt).append("] ");
		}
		return sb.toString();
	}

	public String toFileFormat() {
		StringBuilder sb = new StringBuilder();
		if (prio > 0) {
			sb.append("(");
			TaskHelper.appendPrio(sb, prio);
			sb.append(") ");
		}
		sb.append(taskDescription);
		return sb.toString();
	}

	public String toDisplayFormat() {
		StringBuilder sb = new StringBuilder();
		sb.append(id).append(" ");
		if (prio > 0) {
			sb.append("(");
			TaskHelper.appendPrio(sb, prio);
			sb.append(") ");
		}
		sb.append(taskDescription);
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[id=").append(id).append("]");
		sb.append("[prio=").append(prio).append("]");
		sb.append("[taskDescription=").append(taskDescription).append("]");
		sb.append("[");
		for (String cxt : contexts) {
			sb.append("[context=").append(cxt).append("]");
		}
		sb.append("]");
		return sb.toString();
	}

}
