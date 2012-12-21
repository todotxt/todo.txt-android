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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

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

}
