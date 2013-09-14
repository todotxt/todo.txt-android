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
 * A junit based test class for testing the ByContextFilter class
 * 
 * @author Tim Barlotta
 */
public class ByContextFilterTest extends TestCase {
    public void testConstructor_nullContexts() {
        ByContextFilter filter = new ByContextFilter(null);
        assertNotNull(filter.getContexts());
        assertEquals(0, filter.getContexts().size());
    }

    public void testConstructor_valid() {
        ByContextFilter filter = new ByContextFilter(Arrays.asList("abc",
                "123", "hello"));
        assertNotNull(filter.getContexts());
        assertEquals(3, filter.getContexts().size());
        assertEquals("abc", filter.getContexts().get(0));
        assertEquals("123", filter.getContexts().get(1));
        assertEquals("hello", filter.getContexts().get(2));
    }

    public void testFilter_noFilterContexts_noTaskContexts() {
        ByContextFilter filter = new ByContextFilter(null);
        assertTrue("apply was not true",
                filter.apply(new Task(1, "hello world")));
    }

    public void testFilter_oneFilterContext_noTaskContexts() {
        ByContextFilter filter = new ByContextFilter(Arrays.asList("abc"));
        assertFalse("apply was not false",
                filter.apply(new Task(1, "hello world")));
    }

    public void testFilter_noFilterContext_oneTaskContexts() {
        ByContextFilter filter = new ByContextFilter(null);
        assertTrue("apply was not true",
                filter.apply(new Task(1, "hello world @abc")));
    }

    public void testFilter_oneFilterContext_sameTaskContext() {
        ByContextFilter filter = new ByContextFilter(Arrays.asList("abc"));
        assertTrue("apply was not true",
                filter.apply(new Task(1, "hello world @abc")));
    }

    public void testFilter_oneFilterContext_differentTaskContext() {
        ByContextFilter filter = new ByContextFilter(Arrays.asList("abc"));
        assertFalse("apply was not false",
                filter.apply(new Task(1, "hello world @123")));
    }

    public void testFilter_multipleFilterContext_oneSameTaskContext() {
        ByContextFilter filter = new ByContextFilter(Arrays.asList("abc",
                "123", "hello"));
        assertTrue("apply was not true",
                filter.apply(new Task(1, "hello world @123")));
    }

    public void testFilter_multipleFilterContext_multipleTaskContext() {
        ByContextFilter filter = new ByContextFilter(Arrays.asList("abc",
                "123", "hello"));
        assertTrue("apply was not true",
                filter.apply(new Task(1, "hello world @123 @goodbye")));
    }

    public void testFilter_multipleFilterContext_multipleSameTaskContext() {
        ByContextFilter filter = new ByContextFilter(Arrays.asList("abc",
                "123", "hello"));
        assertTrue("apply was not true",
                filter.apply(new Task(1, "hello world @123 @hello")));
    }

    public void testFilter_multipleFilterContext_multipleDifferentTaskContext() {
        ByContextFilter filter = new ByContextFilter(Arrays.asList("abc",
                "123", "hello"));
        assertFalse("apply was not false",
                filter.apply(new Task(1, "hello world @xyz @goodbye")));
    }

    public void testFilter_noContextFilter_noTaskContext() {
        ByContextFilter filter = new ByContextFilter(Arrays.asList("-"));
        assertTrue("apply was not true",
                filter.apply(new Task(1, "hello world")));
    }

    public void testFilter_noContextFilter_oneTaskContext() {
        ByContextFilter filter = new ByContextFilter(Arrays.asList("-"));
        assertFalse("apply was not false",
                filter.apply(new Task(1, "hello world @xyz")));
    }
}
