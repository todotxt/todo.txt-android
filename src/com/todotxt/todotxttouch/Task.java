package com.todotxt.todotxttouch;

import java.util.List;

public class Task {

	public int id;

	public int prio;

	public String text;

	public List<String> contexts;

	public Task(int id, int prio, String taskDescription, List<String> contexts) {
		this.id = id;
		this.prio = prio;
		this.text = taskDescription;
		this.contexts = contexts;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[id=").append(id).append("]");
		sb.append("[prio=").append(prio).append("]");
		sb.append("[taskDescription=").append(text).append("]");
		sb.append("[");
		for (String cxt : contexts) {
			sb.append("[context=").append(cxt).append("]");
		}
		sb.append("]");
		return sb.toString();
	}

}
