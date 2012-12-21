/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2012 Todo.txt contributors (http://todotxt.com)
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
 * @copyright 2009-2012 Todo.txt contributors (http://todotxt.com)
 */
package nl.mpcjanssen.todotxtholo.task;

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * A junit based test class for testing the OrFilter class
 * 
 * @author Tim Barlotta
 */
public class OrFilterTest extends TestCase {
	public void testNoFilters() {
		OrFilter orFilter = new OrFilter();
		assertTrue("apply was not true",
				orFilter.apply(new Task(1, "(A) abc 123")));
	}

	public void testMultipleFilters_matchesBoth() {
		OrFilter orFilter = new OrFilter();
		orFilter.addFilter(new ByPriorityFilter(Arrays.asList(Priority.A)));
		orFilter.addFilter(new ByTextFilter("abc", false));

		assertTrue("apply was not true",
				orFilter.apply(new Task(1, "(A) abc 123")));
	}

	public void testMultipleFilters_matchesOnlyOne() {
		OrFilter orFilter = new OrFilter();
		orFilter.addFilter(new ByPriorityFilter(Arrays.asList(Priority.A)));
		orFilter.addFilter(new ByTextFilter("abc", false));

		assertTrue("apply was not true",
				orFilter.apply(new Task(1, "(A) hello world")));
	}

	public void testMultipleFilters_matchesNone() {
		OrFilter orFilter = new OrFilter();
		orFilter.addFilter(new ByPriorityFilter(Arrays.asList(Priority.A)));
		orFilter.addFilter(new ByTextFilter("abc", false));

		assertFalse("apply was not false",
				orFilter.apply(new Task(1, "(B) hello world")));
	}

}
