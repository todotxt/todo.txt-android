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
 * @author Stephen Henderson <me[at]steveh[dot]ca>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 Stephen Henderson
 */
package com.todotxt.todotxttouch.test;

import com.todotxt.todotxttouch.Task;
import com.todotxt.todotxttouch.TaskHelper;
import com.todotxt.todotxttouch.TodoUtil;

import junit.framework.TestCase;

public class TaskHelperTest extends TestCase {
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
	}
	
	
	
// *** START TaskHelper.toFileFormat TESTS ***
	
	/*
	 * Ensure that a normal task is output the same way as it is read in.
	 */
	public void testToFileFormat() {
		String expectedString = "A Simple test with no curve balls";
		Task t = TodoUtil.createTask(0, expectedString);
		
		assertTrue("File output matches original input",
				expectedString.equals(TaskHelper.toFileFormat(t)));
	}
	
	/*
	 * Ensure that a task is output to a file the same way as it was read in
	 * with a priority.
	 */
	public void testToFileFormatWithPriority() {
		String expectedString = "(A) Simple test with a priority";
		Task t = TodoUtil.createTask(0, expectedString);
		
		assertTrue("File output matches original input",
				expectedString.equals(TaskHelper.toFileFormat(t)));
	}
	
	/*
	 * Ensure that a task is output to a file the same way as it was read in
	 * with a context.
	 */
	public void testToFileFormatWithContext() {
		String expectedString = "Simple test with a context @home";
		Task t = TodoUtil.createTask(0, expectedString);
		
		assertTrue("File output matches original input",
				expectedString.equals(TaskHelper.toFileFormat(t)));
	}
	
	/*
	 * Ensure that a task is output to a file the same way as it was read in
	 * with multiple context.
	 */
	public void testToFileFormatWithMultipleContexts() {
		String expectedString = "Simple @phone test @home";
		Task t = TodoUtil.createTask(0, expectedString);
		
		assertTrue("File output matches original input",
				expectedString.equals(TaskHelper.toFileFormat(t)));
	}
	
// *** END TaskHelper.toFileFormat TESTS ***
}
