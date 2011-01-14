/**
 *
 * Todo.txt Touch/tests/src/com/todotxt/todotxttouch/test/TodoUtilTest.java
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

import java.util.ArrayList;
import java.util.List;

import com.todotxt.todotxttouch.Task;
import com.todotxt.todotxttouch.TaskHelper;
import com.todotxt.todotxttouch.TodoUtil;

import junit.framework.TestCase;

public class TodoUtilTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
// *** START TodoUtil.createTask TESTS ***
	
	/**
	 * Make sure TaskUtil.createTask can properly parse a task with no priorities, contexts,
	 * projects, due dates, etc. Vanilla task.
	 */
	public void testCreateTaskPlain() {
		int expectedId = 1;
		int expectedPriority = 0;
		String expectedString = "Complete a simple task";
		List<String> expectedContexts = new ArrayList<String>();
		
		Task expectedTask = new Task(expectedId, expectedPriority, expectedString,
				expectedContexts);
		Task createdTask = TodoUtil.createTask(expectedId, expectedString);
		
		assertEquals("ID's are equal", expectedTask.id, createdTask.id);
		assertEquals("Priorities are equal", expectedTask.prio, createdTask.prio);
		assertTrue("Text is equal", expectedTask.text.equals(createdTask.text));
		
		// Should both be zero, if createdTask's contexts count is greater, something is
		// seriously wrong.
		assertTrue("Items in context lists match",
				compareTwoContextLists(expectedTask.contexts, createdTask.contexts));
	}
	
	/**
	 * Make sure that TaskUtil.createTask is capable of properly parsing tasks with a priority
	 * set in the proper position directly at the beginning of the sentence.
	 */
	public void testCreateTaskWithPriority() {
		int expectedId = 1;
		int expectedPriority = TaskHelper.parsePrio("A");
		String expectedString = "(A) Complete a simple task";
		List<String> expectedContexts = new ArrayList<String>();
		
		Task expectedTask = new Task(expectedId, expectedPriority, expectedString,
				expectedContexts);
		Task createdTask = TodoUtil.createTask(expectedId, expectedString);
		
		assertEquals("ID's are equal", expectedTask.id, createdTask.id);
		assertEquals("Priorities are equal", expectedTask.prio, createdTask.prio);
		assertTrue("Text is equal", expectedTask.text.equals(createdTask.text));
		
		// Should both be zero, if createdTask's contexts count is greater, something is
		// seriously wrong.
		assertTrue("Items in context lists match",
				compareTwoContextLists(expectedTask.contexts, createdTask.contexts));
	}
	
	/**
	 * Ensure that a single context can be grabbed at the end of a string
	 */
	public void testCreateTaskWithContext() {
		int expectedId = 1;
		int expectedPriority = 0;
		String expectedString = "Complete a simple task @phone";
		List<String> expectedContexts = new ArrayList<String>();
		expectedContexts.add("phone");
		
		Task expectedTask = new Task(expectedId, expectedPriority, expectedString,
				expectedContexts);
		Task createdTask = TodoUtil.createTask(expectedId, expectedString);
		
		assertEquals("ID's are equal", expectedTask.id, createdTask.id);
		assertEquals("Priorities are equal", expectedTask.prio, createdTask.prio);
		assertTrue("Text is equal", expectedTask.text.equals(createdTask.text));
		
		assertTrue("Items in context lists match",
				compareTwoContextLists(expectedTask.contexts, createdTask.contexts));
	}
	
	/*
	 * Ensures that multiple contexts can be grabbed from the end of a string
	 */
	public void testCreateTaskWithMultipleContext() {
		int expectedId = 1;
		int expectedPriority = 0;
		String expectedString = "Complete a simple task @phone @home";
		List<String> expectedContexts = new ArrayList<String>();
		expectedContexts.add("phone");
		expectedContexts.add("home");
		
		Task expectedTask = new Task(expectedId, expectedPriority, expectedString,
				expectedContexts);
		Task createdTask = TodoUtil.createTask(expectedId, expectedString);
		
		assertEquals("ID's are equal", expectedTask.id, createdTask.id);
		assertEquals("Priorities are equal", expectedTask.prio, createdTask.prio);
		assertTrue("Text is equal", expectedTask.text.equals(createdTask.text));
		
		assertTrue("Items in context lists match",
				compareTwoContextLists(expectedTask.contexts, createdTask.contexts));
	}
	
	/*
	 * Ensure that a context can be properly parsed out from a string if it's not at the end
	 */
	public void testCreateTaskWithContextInMiddle() {
		int expectedId = 1;
		int expectedPriority = 0;
		String expectedString = "Complete a simple @phone task";
		List<String> expectedContexts = new ArrayList<String>();
		expectedContexts.add("phone");
		
		Task expectedTask = new Task(expectedId, expectedPriority, expectedString,
				expectedContexts);
		Task createdTask = TodoUtil.createTask(expectedId, expectedString);
		
		assertEquals("ID's are equal", expectedTask.id, createdTask.id);
		assertEquals("Priorities are equal", expectedTask.prio, createdTask.prio);
		assertTrue("Text is equal", expectedTask.text.equals(createdTask.text));
		
		assertTrue("Items in context lists match",
				compareTwoContextLists(expectedTask.contexts, createdTask.contexts));
	}
	
	public void testCreateTaskDistinguishContextFromEmail() {
		int expectedId = 1;
		int expectedPriority = 0;
		String expectedString = "Email me@steveh.ca about unit testing";
		List<String> expectedContexts = new ArrayList<String>();
		
		Task expectedTask = new Task(expectedId, expectedPriority, expectedString,
				expectedContexts);
		Task createdTask = TodoUtil.createTask(expectedId, expectedString);
		
		assertEquals("ID's are equal", expectedTask.id, createdTask.id);
		assertEquals("Priorities are equal", expectedTask.prio, createdTask.prio);
		assertTrue("Text is equal", expectedTask.text.equals(createdTask.text));
		
		assertTrue("Items in context lists match",
				compareTwoContextLists(expectedTask.contexts, createdTask.contexts));
	}
	
// *** START TodoUtil.createTask TESTS ***
	
	private boolean compareTwoContextLists(List<String> expected, List<String> created)
	{
		if(expected.size() != created.size())
			return false;
		
		for(int i = 0; i < expected.size(); ++i) {
			if(!expected.get(i).equals(created.get(i)))
				return false;
		}
		
		return true;
	}
}
