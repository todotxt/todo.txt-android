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

/**
 * A composite filter. At least one subfilters must be true for this filter to
 * be true. Returns true when there are no subfilters.
 * 
 * @author Tim Barlotta
 */
class OrFilter implements Filter<Task> {
	private ArrayList<Filter<Task>> filters = new ArrayList<Filter<Task>>();

	public void addFilter(Filter<Task> filter) {
		if (filter != null) {
			filters.add(filter);
		}
	}

	@Override
	public boolean apply(Task input) {
		if (filters.size() <= 0) {
			return true;
		}

		for (Filter<Task> f : filters) {
			if (f.apply(input)) {
				return true;
			}
		}
		return false;
	}
}
