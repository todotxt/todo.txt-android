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

import java.util.ArrayList;
import java.util.List;

/**
 * A filter that matches Tasks containing the specified contexts
 * 
 * @author Tim Barlotta
 */
class ByContextFilter implements Filter<Task> {
	private ArrayList<String> contexts = new ArrayList<String>();

	public ByContextFilter(List<String> contexts) {
		if (contexts != null) {
			this.contexts.addAll(contexts);
		}
	}

	@Override
	public boolean apply(Task input) {
		if (contexts.size() == 0) {
			return true;
		}

		for (String c : input.getContexts()) {
			if (contexts.contains(c)) {
				return true;
			}
		}
		/*
		 * Match tasks without context if filter contains "-"
		 */
		if (input.getContexts().size()==0 && contexts.contains("-")) {
			return true;
		}

		return false;

	}

	/* FOR TESTING ONLY, DO NOT USE IN APPLICATION */
	ArrayList<String> getContexts() {
		return contexts;
	}
}
