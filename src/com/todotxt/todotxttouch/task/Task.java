/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/task/Task.java
 *
 * Copyright (c) 2009-2011 mathias, Gina Trapani, Tim Barlotta
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
 * @author Gina Trapani <ginatrapani[at]gmail[dot]com>
 * @author Tim Barlotta <tim[at]barlotta[dot]net>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 mathias, Gina Trapani, Tim Barlotta
 */
package com.todotxt.todotxttouch.task;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.todotxt.todotxttouch.TodoTxtTouch;
import com.todotxt.todotxttouch.util.RelativeDate;
import com.todotxt.todotxttouch.util.Strings;

@SuppressWarnings("serial")
public class Task implements Serializable {
	private static final String COMPLETED = "x ";
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private final String originalText;
	private final Priority originalPriority;

	private long id;
	private Priority priority;
	private boolean deleted = false;
	private boolean completed = false;
	private String text;
	private String completionDate;
	private String prependedDate;
	private String relativeAge = "";
	private List<String> contexts;
	private List<String> projects;

	public Task(long id, String rawText, Date defaultPrependedDate) {
		this.id = id;
		this.init(rawText, defaultPrependedDate);
		this.originalPriority = priority;
		this.originalText = text;
	}

	public Task(long id, String rawText) {
		this(id, rawText, null);
	}

	public void update(String rawText) {
		this.init(rawText, null);
	}

	private void init(String rawText, Date defaultPrependedDate) {
		TextSplitter splitter = TextSplitter.getInstance();
		TextSplitter.SplitResult splitResult = splitter.split(rawText);
		this.priority = splitResult.priority;
		this.text = splitResult.text;
		this.prependedDate = splitResult.prependedDate;
		this.completed = splitResult.completed;
		this.completionDate = splitResult.completedDate;

		this.contexts = ContextParser.getInstance().parse(text);
		this.projects = ProjectParser.getInstance().parse(text);
		this.deleted = Strings.isEmptyOrNull(text);

		if (defaultPrependedDate != null
				&& Strings.isEmptyOrNull(this.prependedDate)) {
			SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
			this.prependedDate = formatter.format(defaultPrependedDate);
		}

		if (!Strings.isEmptyOrNull(this.prependedDate)) {
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			try {
				Date d = sdf.parse(this.prependedDate);
				this.relativeAge = RelativeDate.getRelativeDate(d);
			} catch (ParseException e) {
				// e.printStackTrace();
			}
		}
	}

	public Priority getOriginalPriority() {
		return originalPriority;
	}

	public String getOriginalText() {
		return originalText;
	}

	public String getText() {
		return text;
	}

	public long getId() {
		return id;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public Priority getPriority() {
		return priority;
	}

	public List<String> getContexts() {
		return contexts;
	}

	public List<String> getProjects() {
		return projects;
	}

	public String getPrependedDate() {
		return prependedDate;
	}

	public String getRelativeAge() {
		return relativeAge;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public boolean isCompleted() {
		return completed;
	}

	public String getCompletionDate() {
		return completionDate;
	}

	public void markComplete(Date date) {
		if (!this.completed) {
			this.priority = Priority.NONE;
			this.completionDate = new SimpleDateFormat(Task.DATE_FORMAT)
					.format(date);
			this.deleted = false;
			this.completed = true;
		}
	}

	public void markIncomplete() {
		if (this.completed) {
			this.completionDate = "";
			this.completed = false;
		}
	}

	public void delete() {
		this.update("");
	}

	// TODO need a better solution (TaskFormatter?) here
	public String inScreenFormat() {
		StringBuilder sb = new StringBuilder();
		if (this.completed) {
			sb.append(COMPLETED).append(this.completionDate).append(" ");
			if (!Strings.isEmptyOrNull(this.prependedDate)) {
				sb.append(this.prependedDate).append(" ");
			}
		}
		sb.append(this.text);
		return sb.toString();
	}

	public String inFileFormat() {
		StringBuilder sb = new StringBuilder();
		if (this.completed) {
			sb.append(COMPLETED).append(this.completionDate).append(" ");
			if (!Strings.isEmptyOrNull(this.prependedDate)) {
				sb.append(this.prependedDate).append(" ");
			}
		} else {
			if (priority != Priority.NONE) {
				sb.append(priority.inFileFormat()).append(" ");
			}
			if (!Strings.isEmptyOrNull(this.prependedDate)) {
				sb.append(this.prependedDate).append(" ");
			}
		}
		sb.append(this.text);
		return sb.toString();
	}

	public void copyInto(Task destination) {
		destination.id = this.id;
		destination.init(this.inFileFormat(), null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Task task = (Task) o;

		if (completed != task.completed) {
			return false;
		}
		if (deleted != task.deleted) {
			return false;
		}
		if (id != task.id) {
			return false;
		}
		if (priority != task.priority) {
			return false;
		}
		if (!contexts.equals(task.contexts)) {
			return false;
		}
		if (!prependedDate.equals(task.prependedDate)) {
			return false;
		}
		if (!projects.equals(task.projects)) {
			return false;
		}
		if (!text.equals(task.text)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = (int) (id ^ (id >>> 32));
		result = 31 * result + priority.hashCode();
		result = 31 * result + (deleted ? 1 : 0);
		result = 31 * result + (completed ? 1 : 0);
		result = 31 * result + text.hashCode();
		result = 31 * result + prependedDate.hashCode();
		result = 31 * result + contexts.hashCode();
		result = 31 * result + projects.hashCode();
		return result;
	}
}