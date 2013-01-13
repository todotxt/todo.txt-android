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

import java.util.List;

import com.todotxt.todotxttouch.util.Strings;

/**
 * Creates a filter based on passed in variables
 * 
 * @author Tim Barlotta
 */
public class FilterFactory {

	public static Filter<Task> generateAndFilter(List<Priority> priorities,
			List<String> contexts, List<String> projects, String text,
			boolean caseSensitive) {
		AndFilter filter = new AndFilter();

		if (priorities.size() > 0) {
			filter.addFilter(new ByPriorityFilter(priorities));
		}

		if (contexts.size() > 0) {
			filter.addFilter(new ByContextFilter(contexts));
		}

		if (projects.size() > 0) {
			filter.addFilter(new ByProjectFilter(projects));
		}

		if (!Strings.isEmptyOrNull(text)) {
			filter.addFilter(new ByTextFilter(text, caseSensitive));
		}
		return filter;
	}

}
