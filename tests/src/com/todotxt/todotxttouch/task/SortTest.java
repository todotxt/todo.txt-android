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

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortTest extends TestCase {
	private List<Task> unsortedTasks = new ArrayList<Task>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		unsortedTasks.add(new Task(1, "(E) a test task"));
		unsortedTasks.add(new Task(55, "z ultimate task"));
		unsortedTasks.add(new Task(99, "(A) awesome task"));
		unsortedTasks.add(new Task(5, "A capitalized task"));
		unsortedTasks.add(new Task(9999, "x a completed task"));
	}

	public void testSort_priorityDescending() {
		Sort sort = Sort.PRIORITY_DESC;
		Collections.sort(unsortedTasks, sort.getComparator());

		assertEquals(99, unsortedTasks.get(0).getId());
		assertEquals(1, unsortedTasks.get(1).getId());
		assertEquals(5, unsortedTasks.get(2).getId());
		assertEquals(55, unsortedTasks.get(3).getId());
		assertEquals(9999, unsortedTasks.get(4).getId());
	}

	public void testSort_idDescending() {
		Sort sort = Sort.ID_DESC;
		Collections.sort(unsortedTasks, sort.getComparator());

		assertEquals(9999, unsortedTasks.get(0).getId());
		assertEquals(99, unsortedTasks.get(1).getId());
		assertEquals(55, unsortedTasks.get(2).getId());
		assertEquals(5, unsortedTasks.get(3).getId());
		assertEquals(1, unsortedTasks.get(4).getId());
	}

	public void testSort_idAscending() {
		Sort sort = Sort.ID_ASC;
		Collections.sort(unsortedTasks, sort.getComparator());

		assertEquals(1, unsortedTasks.get(0).getId());
		assertEquals(5, unsortedTasks.get(1).getId());
		assertEquals(55, unsortedTasks.get(2).getId());
		assertEquals(99, unsortedTasks.get(3).getId());
		assertEquals(9999, unsortedTasks.get(4).getId());
	}

	public void testSort_textAscending() {
		Sort sort = Sort.TEXT_ASC;
		Collections.sort(unsortedTasks, sort.getComparator());

		assertEquals(5, unsortedTasks.get(0).getId());
		assertEquals(9999, unsortedTasks.get(1).getId());
		assertEquals(1, unsortedTasks.get(2).getId());
		assertEquals(99, unsortedTasks.get(3).getId());
		assertEquals(55, unsortedTasks.get(4).getId());
	}

}
