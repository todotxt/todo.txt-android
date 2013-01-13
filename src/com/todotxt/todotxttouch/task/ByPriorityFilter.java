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
 * A filter that matches Tasks containing the specified priorities
 * 
 * @author Tim Barlotta
 */
class ByPriorityFilter implements Filter<Task> {
	ArrayList<Priority> priorities = new ArrayList<Priority>();

	public ByPriorityFilter(List<Priority> priorities) {
		if (priorities != null) {
			this.priorities.addAll(priorities);
		}
	}

	@Override
	public boolean apply(Task input) {
		if (priorities.size() == 0) {
			return true;
		}

		if (priorities.contains(input.getPriority())) {
			return true;
		}
		return false;
	}

	/* FOR TESTING ONLY, DO NOT USE IN APPLICATION */
	ArrayList<Priority> getPriorities() {
		return priorities;
	}
}
