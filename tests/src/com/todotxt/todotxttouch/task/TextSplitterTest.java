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
        TextSplitter splitter = new TextSplitter(input);
        assertEquals(Task.NO_PRIORITY, splitter.getPriority());
        assertEquals("", splitter.getPrependedDate());
        assertEquals("", splitter.getText());
    }

    public void testSplit_null() {
        String input = null;
        TextSplitter splitter = new TextSplitter(input);
        assertEquals(Task.NO_PRIORITY, splitter.getPriority());
        assertEquals("", splitter.getPrependedDate());
        assertEquals("", splitter.getText());
    }

    public void testSplit_withPriority() {
        String input = "(A) test";
        TextSplitter splitter = new TextSplitter(input);
        assertEquals('A', splitter.getPriority());
        assertEquals("", splitter.getPrependedDate());
        assertEquals("test", splitter.getText());
    }

    public void testSplit_withPrependedDate() {
        String input = "2011-01-02 test";
        TextSplitter splitter = new TextSplitter(input);
        assertEquals(Task.NO_PRIORITY, splitter.getPriority());
        assertEquals("2011-01-02", splitter.getPrependedDate());
        assertEquals("test", splitter.getText());
    }

    public void testSplit_withPriorityAndPrependedDate() {
        String input = "(A) 2011-01-02 test";
        TextSplitter splitter = new TextSplitter(input);
        assertEquals('A', splitter.getPriority());
        assertEquals("2011-01-02", splitter.getPrependedDate());
        assertEquals("test", splitter.getText());
    }

    //TODO what should be the output here?  Actual results seem wrong
    public void testSplit_outOfOrder() {
        String input = "2011-01-02 (A) test";
        TextSplitter splitter = new TextSplitter(input);
        assertEquals('A', splitter.getPriority());
        assertEquals("", splitter.getPrependedDate());
        assertEquals("test", splitter.getText());
    }
}
