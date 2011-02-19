/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/task/ByTextFilterTest.java
 *
 * Copyright (c) 2011 Tim Barlotta
 *
 * LICENSE:
 *
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
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
 * @author Tim Barlotta <tim[at]barlotta[dot]net>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2011 Tim Barlotta
 */

package com.todotxt.todotxttouch.task;

import junit.framework.TestCase;

/**
 * A junit based test class for testing the ByTextFilter class
 *
 * @author Tim Barlotta
 */
public class ByTextFilterTest extends TestCase {
    public void testConstructor_nullText_false() {
        ByTextFilter filter = new ByTextFilter(null, false);
        assertNotNull(filter.getText());
        assertEquals("", filter.getText());
        assertFalse(filter.isCaseSensitive());
    }

    public void testConstructor_nullText_true() {
        ByTextFilter filter = new ByTextFilter(null, true);
        assertNotNull(filter.getText());
        assertEquals("", filter.getText());
        assertTrue(filter.isCaseSensitive());
    }

    public void testConstructor_valid_false() {
        ByTextFilter filter = new ByTextFilter("abc", false);
        assertNotNull(filter.getText());
        assertEquals("ABC", filter.getText());
        assertFalse(filter.isCaseSensitive());
    }

    public void testConstructor_valid_true() {
        ByTextFilter filter = new ByTextFilter("abc", true);
        assertNotNull(filter.getText());
        assertEquals("abc", filter.getText());
        assertTrue(filter.isCaseSensitive());
    }

    public void testFilter_noText_noTaskText() {
        ByTextFilter filter = new ByTextFilter("", false);
        assertTrue("apply was not true", filter.apply(new Task(1, "")));
    }

    public void testFilter_noText_hasTaskText() {
        ByTextFilter filter = new ByTextFilter("", false);
        assertTrue("apply was not true", filter.apply(new Task(1, "abc")));
    }

    public void testFilter_abcText_noTaskText() {
        ByTextFilter filter = new ByTextFilter("abc", false);
        assertFalse("apply was not false", filter.apply(new Task(1, "")));
    }

    public void testFilter_abcText_notContainedTaskText() {
        ByTextFilter filter = new ByTextFilter("abc", false);
        assertFalse("apply was not false", filter.apply(new Task(1, "hello world")));
    }

    public void testFilter_abcText_containsTaskText_wrongCase_caseSensitive() {
        ByTextFilter filter = new ByTextFilter("abc", true);
        assertFalse("apply was not false", filter.apply(new Task(1, "hello ABC world")));
    }

    public void testFilter_abcText_containsTaskText_wrongCase_caseInSensitive() {
        ByTextFilter filter = new ByTextFilter("abc", false);
        assertTrue("apply was not true", filter.apply(new Task(1, "hello ABC world")));
    }

    public void testFilter_abcText_containsTaskTextNotPadded_wrongCase_caseInSensitive() {
        ByTextFilter filter = new ByTextFilter("abc", false);
        assertTrue("apply was not true", filter.apply(new Task(1, "helloABCworld")));
    }
}
