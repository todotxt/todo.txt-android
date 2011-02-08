/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/task/Task.java
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
 * A junit based test class for the PriorityTextSplitter class
 *
 * @author Tim Barlotta
 */
public class PriorityTextSplitterTest extends TestCase {
	public void testSplit_empty() {
		String input = "";
		PriorityTextSplitter splitter = PriorityTextSplitter.getInstance();
		PriorityTextSplitter.PrioritySplitResult result = splitter.split(input);
		assertEquals(Priority.NONE, result.priority);
		assertEquals("", result.text);
	}

	public void testSplit_null() {
		String input = null;
		PriorityTextSplitter splitter = PriorityTextSplitter.getInstance();
		PriorityTextSplitter.PrioritySplitResult result = splitter.split(input);
		assertEquals(Priority.NONE, result.priority);
		assertEquals("", result.text);
	}

	public void testSplit_withPriority() {
		String input = "(A) test";
		PriorityTextSplitter splitter = PriorityTextSplitter.getInstance();
		PriorityTextSplitter.PrioritySplitResult result = splitter.split(input);
		assertEquals(Priority.A, result.priority);
		assertEquals("test", result.text);
	}

	public void testSplit_withPrependedDate() {
		String input = "2011-01-02 test";
		PriorityTextSplitter splitter = PriorityTextSplitter.getInstance();
		PriorityTextSplitter.PrioritySplitResult result = splitter.split(input);
		assertEquals(Priority.NONE, result.priority);
		assertEquals("2011-01-02 test", result.text);
	}

	public void testSplit_withPriorityAndPrependedDate() {
		String input = "(A) 2011-01-02 test";
		PriorityTextSplitter splitter = PriorityTextSplitter.getInstance();
		PriorityTextSplitter.PrioritySplitResult result = splitter.split(input);
		assertEquals(Priority.A, result.priority);
		assertEquals("2011-01-02 test", result.text);
	}

	public void testSplit_dateInterspersedInText() {
		String input = "Call Mom 2011-03-02";
		PriorityTextSplitter splitter = PriorityTextSplitter.getInstance();
		PriorityTextSplitter.PrioritySplitResult result = splitter.split(input);
		assertEquals(Priority.NONE, result.priority);
		assertEquals("Call Mom 2011-03-02", result.text);
	}

	public void testSplit_missingSpace() {
		String input = "(A)2011-01-02 test";
		PriorityTextSplitter splitter = PriorityTextSplitter.getInstance();
		PriorityTextSplitter.PrioritySplitResult result = splitter.split(input);
		assertEquals(Priority.NONE, result.priority);
		assertEquals("(A)2011-01-02 test", result.text);
	}

	public void testSplit_outOfOrder() {
		String input = "2011-01-02 (A) test";
		PriorityTextSplitter splitter = PriorityTextSplitter.getInstance();
		PriorityTextSplitter.PrioritySplitResult result = splitter.split(input);
		assertEquals(Priority.NONE, result.priority);
		assertEquals("2011-01-02 (A) test", result.text);
	}

	public void testSplit_completed() {
		String input = "x 2011-01-02 test 123";
		PriorityTextSplitter splitter = PriorityTextSplitter.getInstance();
		PriorityTextSplitter.PrioritySplitResult result = splitter.split(input);
		assertEquals(Priority.NONE, result.priority);
		assertEquals("x 2011-01-02 test 123", result.text);
	}

	public void testSplit_completedWithPrependedDate() {
		String input = "x 2011-01-02 2011-01-01 test 123";
		PriorityTextSplitter splitter = PriorityTextSplitter.getInstance();
		PriorityTextSplitter.PrioritySplitResult result = splitter.split(input);
		assertEquals(Priority.NONE, result.priority);
		assertEquals("x 2011-01-02 2011-01-01 test 123", result.text);
	}
}
