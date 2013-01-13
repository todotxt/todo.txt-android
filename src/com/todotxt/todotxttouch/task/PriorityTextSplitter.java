/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2013 Todo.txt contributors (http://todotxt.com)
 *
 * LICENSE:
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
 * @author Todo.txt contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt contributors (http://todotxt.com)
 */
package com.todotxt.todotxttouch.task;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PriorityTextSplitter {
	private final static Pattern PRIORITY_PATTERN = Pattern
			.compile("^\\(([A-Z])\\) (.*)");

	private final static PriorityTextSplitter INSTANCE = new PriorityTextSplitter();

	private PriorityTextSplitter() {
	}

	public static PriorityTextSplitter getInstance() {
		return INSTANCE;
	}

	public static class PrioritySplitResult {
		public final Priority priority;
		public final String text;

		public PrioritySplitResult(Priority priority, String text) {
			this.priority = priority;
			this.text = text;
		}
	}

	public PrioritySplitResult split(String text) {
		if (text == null) {
			return new PrioritySplitResult(Priority.NONE, "");
		}
		Priority priority = Priority.NONE;
		Matcher priorityMatcher = PRIORITY_PATTERN.matcher(text);
		if (priorityMatcher.find()) {
			priority = Priority.toPriority(priorityMatcher.group(1));
			text = priorityMatcher.group(2);
		}
		return new PrioritySplitResult(priority, text);
	}
}
