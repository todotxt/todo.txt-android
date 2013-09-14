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

public class TextSplitterTest extends TestCase {
    public void testSplit_empty() {
        String input = "";
        TextSplitter splitter = TextSplitter.getInstance();
        TextSplitter.SplitResult result = splitter.split(input);
        assertEquals(Priority.NONE, result.priority);
        assertEquals("", result.prependedDate);
        assertEquals("", result.text);
        assertFalse(result.completed);
        assertEquals("", result.completedDate);
    }

    public void testSplit_null() {
        String input = null;
        TextSplitter splitter = TextSplitter.getInstance();
        TextSplitter.SplitResult result = splitter.split(input);
        assertEquals(Priority.NONE, result.priority);
        assertEquals("", result.prependedDate);
        assertEquals("", result.text);
        assertFalse(result.completed);
        assertEquals("", result.completedDate);
    }

    public void testSplit_withPriority() {
        String input = "(A) test";
        TextSplitter splitter = TextSplitter.getInstance();
        TextSplitter.SplitResult result = splitter.split(input);
        assertEquals(Priority.A, result.priority);
        assertEquals("", result.prependedDate);
        assertEquals("test", result.text);
        assertFalse(result.completed);
        assertEquals("", result.completedDate);
    }

    public void testSplit_withPrependedDate() {
        String input = "2011-01-02 test";
        TextSplitter splitter = TextSplitter.getInstance();
        TextSplitter.SplitResult result = splitter.split(input);
        assertEquals(Priority.NONE, result.priority);
        assertEquals("2011-01-02", result.prependedDate);
        assertEquals("test", result.text);
        assertFalse(result.completed);
        assertEquals("", result.completedDate);
    }

    public void testSplit_withPriorityAndPrependedDate() {
        String input = "(A) 2011-01-02 test";
        TextSplitter splitter = TextSplitter.getInstance();
        TextSplitter.SplitResult result = splitter.split(input);
        assertEquals(Priority.A, result.priority);
        assertEquals("2011-01-02", result.prependedDate);
        assertEquals("test", result.text);
        assertFalse(result.completed);
        assertEquals("", result.completedDate);
    }

    public void testSplit_dateInterspersedInText() {
        String input = "Call Mom 2011-03-02";
        TextSplitter splitter = TextSplitter.getInstance();
        TextSplitter.SplitResult result = splitter.split(input);
        assertEquals(Priority.NONE, result.priority);
        assertEquals("", result.prependedDate);
        assertEquals("Call Mom 2011-03-02", result.text);
        assertFalse(result.completed);
        assertEquals("", result.completedDate);
    }

    public void testSplit_missingSpace() {
        String input = "(A)2011-01-02 test";
        TextSplitter splitter = TextSplitter.getInstance();
        TextSplitter.SplitResult result = splitter.split(input);
        assertEquals(Priority.NONE, result.priority);
        assertEquals("", result.prependedDate);
        assertEquals("(A)2011-01-02 test", result.text);
        assertFalse(result.completed);
        assertEquals("", result.completedDate);
    }

    public void testSplit_outOfOrder() {
        String input = "2011-01-02 (A) test";
        TextSplitter splitter = TextSplitter.getInstance();
        TextSplitter.SplitResult result = splitter.split(input);
        assertEquals(Priority.NONE, result.priority);
        assertEquals("2011-01-02", result.prependedDate);
        assertEquals("(A) test", result.text);
        assertFalse(result.completed);
        assertEquals("", result.completedDate);
    }

    public void testSplit_completed() {
        String input = "x 2011-01-02 test 123";
        TextSplitter splitter = TextSplitter.getInstance();
        TextSplitter.SplitResult result = splitter.split(input);
        assertEquals(Priority.NONE, result.priority);
        assertEquals("", result.prependedDate);
        assertEquals("test 123", result.text);
        assertTrue(result.completed);
        assertEquals("2011-01-02", result.completedDate);
    }

    public void testSplit_completedWithPrependedDate() {
        String input = "x 2011-01-02 2011-01-01 test 123";
        TextSplitter splitter = TextSplitter.getInstance();
        TextSplitter.SplitResult result = splitter.split(input);
        assertEquals(Priority.NONE, result.priority);
        assertEquals("2011-01-01", result.prependedDate);
        assertEquals("test 123", result.text);
        assertTrue(result.completed);
        assertEquals("2011-01-02", result.completedDate);
    }
}
