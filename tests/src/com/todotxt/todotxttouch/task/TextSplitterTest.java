/**
 *
 * Todo.txt Touch/tests/src/com/todotxt/todotxttouch/test/TaskHelperTest.java
 *
 * Copyright (c) 2009-2011 Stephen Henderson
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
 * @copyright 2009-2011 Tim Barlotta
 */
package com.todotxt.todotxttouch.task;

import junit.framework.TestCase;

/**
 * A junit based test class for the TextSplitter class
 */
public class TextSplitterTest extends TestCase {
    public void testSplit_empty() {
        String input = "";
        TextSplitter splitter = TextSplitter.getInstance();
        TextSplitter.SplitResult result = splitter.split(input);
        assertEquals(Task.NO_PRIORITY, result.priority);
        assertEquals("", result.prependedDate);
        assertEquals("", result.text);
    }

    public void testSplit_null() {
        String input = null;
        TextSplitter splitter = TextSplitter.getInstance();
        TextSplitter.SplitResult result = splitter.split(input);
        assertEquals(Task.NO_PRIORITY, result.priority);
        assertEquals("", result.prependedDate);
        assertEquals("", result.text);
    }

    public void testSplit_withPriority() {
        String input = "(A) test";
        TextSplitter splitter = TextSplitter.getInstance();
        TextSplitter.SplitResult result = splitter.split(input);
        assertEquals('A', result.priority);
        assertEquals("", result.prependedDate);
        assertEquals("test", result.text);
    }

    public void testSplit_withPrependedDate() {
        String input = "2011-01-02 test";
        TextSplitter splitter = TextSplitter.getInstance();
        TextSplitter.SplitResult result = splitter.split(input);
        assertEquals(Task.NO_PRIORITY, result.priority);
        assertEquals("2011-01-02", result.prependedDate);
        assertEquals("test", result.text);
    }

    public void testSplit_withPriorityAndPrependedDate() {
        String input = "(A) 2011-01-02 test";
        TextSplitter splitter = TextSplitter.getInstance();
        TextSplitter.SplitResult result = splitter.split(input);
        assertEquals('A', result.priority);
        assertEquals("2011-01-02", result.prependedDate);
        assertEquals("test", result.text);
    }

    public void testSplit_dateInterspersedInText() {
        String input = "Call Mom 2011-03-02";
        TextSplitter splitter = TextSplitter.getInstance();
        TextSplitter.SplitResult result = splitter.split(input);
        assertEquals(Task.NO_PRIORITY, result.priority);
        assertEquals("", result.prependedDate);
        assertEquals("Call Mom 2011-03-02", result.text);
    }

    public void testSplit_missingSpace() {
        String input = "(A)2011-01-02 test";
        TextSplitter splitter = TextSplitter.getInstance();
        TextSplitter.SplitResult result = splitter.split(input);
        assertEquals(Task.NO_PRIORITY, result.priority);
        assertEquals("", result.prependedDate);
        assertEquals("(A)2011-01-02 test", result.text);
    }

    public void testSplit_outOfOrder() {
        String input = "2011-01-02 (A) test";
        TextSplitter splitter = TextSplitter.getInstance();
        TextSplitter.SplitResult result = splitter.split(input);
        assertEquals(Task.NO_PRIORITY, result.priority);
        assertEquals("2011-01-02", result.prependedDate);
        assertEquals("(A) test", result.text);
    }
}
