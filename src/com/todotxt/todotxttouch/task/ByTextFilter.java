/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/task/ByTextFilter.java
 *
 * Copyright (c) 2011 Tim Barlotta
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
 * @author Tim Barlotta <tim[at]barlotta[dot]net>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2011 Tim Barlotta
 */

package com.todotxt.todotxttouch.task;

/**
 * A filter that matches Tasks containing the specified text
 * 
 * @author Tim Barlotta
 */
class ByTextFilter implements Filter<Task> {
	private String text;
	private boolean caseSensitive;

	public ByTextFilter(String text, boolean caseSensitive) {
		if (text == null) {
			text = "";
		}
		this.text = caseSensitive ? text : text.toUpperCase();
		this.caseSensitive = caseSensitive;
	}

	@Override
	public boolean apply(Task input) {
		if (text.length() <= 0) {
			return true;
		}

		String taskText = caseSensitive ? input.getText() : input.getText()
				.toUpperCase();
		if (taskText.contains(text)) {
			return true;
		}
		return false;
	}

	/* FOR TESTING ONLY, DO NOT USE IN APPLICATION */
	String getText() {
		return text;
	}

	boolean isCaseSensitive() {
		return caseSensitive;
	}
}
