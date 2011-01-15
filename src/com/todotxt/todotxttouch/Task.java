/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/Task.java
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
 * @author mathias <mathias[at]x2[dot](none)>
 * @author mathias <mathias[at]ws7862[dot](none)>
 * @author Gina Trapani <ginatrapani[at]gmail[dot]com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 mathias, Gina Trapani
 */
package com.todotxt.todotxttouch;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class Task implements Serializable {

	public long id;

	public char prio;

	public String text;

	public List<String> contexts;

	public List<String> projects;

	public String prepended_date = "";

	public Task(long id, char prio, String prepended_date, String text) {
		this.id = id;
		this.prio = prio;
		this.prepended_date = prepended_date;
		this.text = text;
		this.contexts = TaskHelper.getContexts(text);
		this.projects = TaskHelper.getProjects(text);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[id=").append(id).append("]");
		sb.append("[prio=").append(prio).append("]");
		sb.append("[text=").append(text).append("]");
		sb.append("[deleted=").append(TaskHelper.isDeleted(this)).append("]");
		sb.append("[completed=").append(TaskHelper.isCompleted(this))
				.append("]");
		// contexts
		sb.append("[contexts:");
		for (String cxt : contexts) {
			sb.append("[context=").append(cxt).append("]");
		}
		sb.append("]");
		// projects
		sb.append("[projects:");
		for (String prj : projects) {
			sb.append("[project=").append(prj).append("]");
		}
		sb.append("]");
		return sb.toString();
	}

}
