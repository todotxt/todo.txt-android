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
        assertFalse("apply was not false",
                filter.apply(new Task(1, "hello world")));
    }

    public void testFilter_abcText_containsTaskText_wrongCase_caseSensitive() {
        ByTextFilter filter = new ByTextFilter("abc", true);
        assertFalse("apply was not false",
                filter.apply(new Task(1, "hello ABC world")));
    }

    public void testFilter_abcText_containsTaskText_wrongCase_caseInSensitive() {
        ByTextFilter filter = new ByTextFilter("abc", false);
        assertTrue("apply was not true",
                filter.apply(new Task(1, "hello ABC world")));
    }

    public void testFilter_abcText_containsTaskTextNotPadded_wrongCase_caseInSensitive() {
        ByTextFilter filter = new ByTextFilter("abc", false);
        assertTrue("apply was not true",
                filter.apply(new Task(1, "helloABCworld")));
    }

    private void shouldMatch(String pattern, String rawText, boolean cs) {
        ByTextFilter filter = new ByTextFilter(pattern, cs);
        assertTrue(String.format("'%s' should match '%s'", pattern, rawText),
                filter.apply(new Task(1, rawText)));
    }

    private void shouldNotMatch(String pattern, String rawText, boolean cs) {
        ByTextFilter filter = new ByTextFilter(pattern, cs);
        assertFalse(
                String.format("'%s' should not match '%s'", pattern, rawText),
                filter.apply(new Task(1, rawText)));
    }

    public void testFilter_andCaseSensitive() {
        this.shouldMatch("abc xyz", "abc xyz", true);
        this.shouldMatch("abc xyz", "abcxyz", true);
        this.shouldMatch("abc xyz", "xyz abc", true);
        this.shouldNotMatch("abc xyz", "xyz", true);
        this.shouldNotMatch("abc xyz", "ABC xyz", true);
    }

    public void testFilter_andCaseInsensitive() {
        this.shouldMatch("abc xyz", "abc xyz", false);
        this.shouldMatch("abc xyz", "abcxyz", false);
        this.shouldMatch("abc xyz", "xyz abc", false);
        this.shouldNotMatch("abc xyz", "xyz", false);
        this.shouldMatch("abc xyz", "ABC xyz", false);
    }

    public void testFilter_andIgnoreWhitespace() {
        this.shouldMatch("abc ", "abc", true);
        this.shouldMatch(" abc", "abc", true);
        this.shouldMatch(" abc ", "abc", true);
    }
}
