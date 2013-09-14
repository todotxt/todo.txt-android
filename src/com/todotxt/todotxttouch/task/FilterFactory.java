/**
 * This file is part of Todo.txt for Android, an app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 *
 * LICENSE:
 *
 * Todo.txt for Android is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt for Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt for Android. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Todo.txt for Android's source code is available at https://github.com/ginatrapani/todo.txt-android
 *
 * @author Todo.txt for Android contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
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
