/**
 * This file is part of Todo.txtndroid app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2013 Todo.txt contributors (http://todotxt.com)
 *
 * LICENSE:
 *
 * Todo.tTodo.txttware: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt is Todo.txt the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt.  If not,Todo.txt//www.gnu.org/licenses/>.
 *
 * @author Todo.txt contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt contributors (http://todotxt.com)
 */

package com.todotxt.todotxttouch.task;

import junit.framework.TestCase;

import java.util.Arrays;

/**
 * A junit based test class for testing the ByPriorityFilter class
 * 
 * @author Tim Barlotta
 */
public class ByPriorityFilterTest extends TestCase {
    public void testConstructor_nullContexts() {
        ByPriorityFilter filter = new ByPriorityFilter(null);
        assertNotNull(filter.getPriorities());
        assertEquals(0, filter.getPriorities().size());
    }

    public void testConstructor_valid() {
        ByPriorityFilter filter = new ByPriorityFilter(Arrays.asList(
                Priority.A, Priority.B));
        assertNotNull(filter.getPriorities());
        assertEquals(2, filter.getPriorities().size());
        assertEquals(Priority.A, filter.getPriorities().get(0));
        assertEquals(Priority.B, filter.getPriorities().get(1));
    }

    public void testFilter_noFilterPriorities_noTaskPriorities() {
        ByPriorityFilter filter = new ByPriorityFilter(null);
        assertTrue("apply was not true",
                filter.apply(new Task(1, "hello world")));
    }

    public void testFilter_oneFilterPriority_noTaskPriorities() {
        ByPriorityFilter filter = new ByPriorityFilter(
                Arrays.asList(Priority.A));
        assertFalse("apply was not false",
                filter.apply(new Task(1, "hello world")));
    }

    public void testFilter_noFilterPriority_oneTaskPriorities() {
        ByPriorityFilter filter = new ByPriorityFilter(null);
        assertTrue("apply was not true",
                filter.apply(new Task(1, "(A) hello world")));
    }

    public void testFilter_oneFilterPriority_sameTaskPriority() {
        ByPriorityFilter filter = new ByPriorityFilter(
                Arrays.asList(Priority.A));
        assertTrue("apply was not true",
                filter.apply(new Task(1, "(A) hello world")));
    }

    public void testFilter_oneFilterPriority_differentTaskPriority() {
        ByPriorityFilter filter = new ByPriorityFilter(
                Arrays.asList(Priority.A));
        assertFalse("apply was not false",
                filter.apply(new Task(1, "(B) hello world")));
    }

    public void testFilter_multipleFilterPriority_oneSameTaskPriority() {
        ByPriorityFilter filter = new ByPriorityFilter(Arrays.asList(
                Priority.A, Priority.B));
        assertTrue("apply was not true",
                filter.apply(new Task(1, "(A) hello world")));
    }

    public void testFilter_multipleFilterPriority_oneDifferentTaskPriority() {
        ByPriorityFilter filter = new ByPriorityFilter(Arrays.asList(
                Priority.A, Priority.B));
        assertFalse("apply was not false",
                filter.apply(new Task(1, "(C) hello world")));
    }
}
