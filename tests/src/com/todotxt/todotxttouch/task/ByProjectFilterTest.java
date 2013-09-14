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
 * A junit based test class for testing the ByProjectFilter class
 * 
 * @author Tim Barlotta
 */
public class ByProjectFilterTest extends TestCase {
    public void testConstructor_nullProjects() {
        ByProjectFilter filter = new ByProjectFilter(null);
        assertNotNull(filter.getProjects());
        assertEquals(0, filter.getProjects().size());
    }

    public void testConstructor_valid() {
        ByProjectFilter filter = new ByProjectFilter(Arrays.asList("abc",
                "123", "hello"));
        assertNotNull(filter.getProjects());
        assertEquals(3, filter.getProjects().size());
        assertEquals("abc", filter.getProjects().get(0));
        assertEquals("123", filter.getProjects().get(1));
        assertEquals("hello", filter.getProjects().get(2));
    }

    public void testFilter_noFilterProjects_noTaskProjects() {
        ByProjectFilter filter = new ByProjectFilter(null);
        assertTrue("apply was not true",
                filter.apply(new Task(1, "hello world")));
    }

    public void testFilter_oneFilterProject_noTaskProjects() {
        ByProjectFilter filter = new ByProjectFilter(Arrays.asList("abc"));
        assertFalse("apply was not false",
                filter.apply(new Task(1, "hello world")));
    }

    public void testFilter_noFilterProject_oneTaskProjects() {
        ByProjectFilter filter = new ByProjectFilter(null);
        assertTrue("apply was not true",
                filter.apply(new Task(1, "hello world +abc")));
    }

    public void testFilter_oneFilterProject_sameTaskProject() {
        ByProjectFilter filter = new ByProjectFilter(Arrays.asList("abc"));
        assertTrue("apply was not true",
                filter.apply(new Task(1, "hello world +abc")));
    }

    public void testFilter_oneFilterProject_differentTaskProject() {
        ByProjectFilter filter = new ByProjectFilter(Arrays.asList("abc"));
        assertFalse("apply was not false",
                filter.apply(new Task(1, "hello world +123")));
    }

    public void testFilter_multipleFilterProject_oneSameTaskProject() {
        ByProjectFilter filter = new ByProjectFilter(Arrays.asList("abc",
                "123", "hello"));
        assertTrue("apply was not true",
                filter.apply(new Task(1, "hello world +123")));
    }

    public void testFilter_multipleFilterProject_multipleTaskProject() {
        ByProjectFilter filter = new ByProjectFilter(Arrays.asList("abc",
                "123", "hello"));
        assertTrue("apply was not true",
                filter.apply(new Task(1, "hello world +123 +goodbye")));
    }

    public void testFilter_multipleFilterProject_multipleSameTaskProject() {
        ByProjectFilter filter = new ByProjectFilter(Arrays.asList("abc",
                "123", "hello"));
        assertTrue("apply was not true",
                filter.apply(new Task(1, "hello world +123 +hello")));
    }

    public void testFilter_multipleFilterProject_multipleDifferentTaskProject() {
        ByProjectFilter filter = new ByProjectFilter(Arrays.asList("abc",
                "123", "hello"));
        assertFalse("apply was not false",
                filter.apply(new Task(1, "hello world +xyz +goodbye")));
    }

    public void testFilter_filterNoProject() {
        ByProjectFilter filter = new ByProjectFilter(Arrays.asList("-"));
        assertTrue("apply was not true",
                filter.apply(new Task(1, "hello world")));
    }

    public void testFilter_filterNoProject_oneTaskProject() {
        ByProjectFilter filter = new ByProjectFilter(Arrays.asList("-"));
        assertFalse("apply was not false",
                filter.apply(new Task(1, "hello world +Test")));
    }
}
