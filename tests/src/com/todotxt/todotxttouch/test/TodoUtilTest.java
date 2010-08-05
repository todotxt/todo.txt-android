package com.todotxt.todotxttouch.test;

import junit.framework.TestCase;

import com.todotxt.todotxttouch.Task;
import com.todotxt.todotxttouch.TaskHelper;

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
		char expectedPriority = '-';
		String expectedString = "Complete a simple task";
		
		Task createdTask = TaskHelper.createTask(expectedId, expectedString);
		
		assertEquals("ID's are not equal", expectedId, createdTask.id);
		assertEquals("Priorities are not equal", expectedPriority, createdTask.prio);
		assertEquals("Text is not equal", expectedString, createdTask.text);
		
		// Should both be zero, if createdTask's contexts count is greater, something is
		// seriously wrong.
		assertTrue("Task has context even when none given", createdTask.contexts.isEmpty());
	}
	
	/**
	 * Make sure that TaskUtil.createTask is capable of properly parsing tasks with a priority
	 * set in the proper position directly at the beginning of the sentence.
	 */
	public void testCreateTaskWithPriority() {
		int expectedId = 1;
		char expectedPriority = 'A';
		String expectedString = "Complete a simple task";
		String inputString = "(A) Complete a simple task";
		
		Task createdTask = TaskHelper.createTask(expectedId, inputString);
		
		assertEquals("ID's are not equal", expectedId, createdTask.id);
		assertEquals("Priorities are not equal", expectedPriority, createdTask.prio);
		assertEquals("Text is not equal", expectedString, createdTask.text);
		
		// Should both be zero, if createdTask's contexts count is greater, something is
		// seriously wrong.
		assertEquals("Task has contexts when none given", 0, createdTask.contexts.size());
	}
	
	/**
	 * Ensure that a single context can be grabbed at the end of a string
	 */
	public void testCreateTaskWithContext() {
		int expectedId = 1;
		char expectedPriority = '-';
		String expectedString = "Complete a simple task @phone";
		
		Task createdTask = TaskHelper.createTask(expectedId, expectedString);
		
		assertEquals("ID's are not equal", expectedId, createdTask.id);
		assertEquals("Priorities are not equal", expectedPriority, createdTask.prio);
		assertEquals("Text is not equal", expectedString, createdTask.text);
		
		assertEquals("Wrong number of contexts", 1, createdTask.contexts.size());
		assertEquals("Wrong context", "phone", createdTask.contexts.get(0));
	}
	
	/*
	 * Ensures that multiple contexts can be grabbed from the end of a string
	 */
	public void testCreateTaskWithMultipleContext() {
		int expectedId = 1;
		char expectedPriority = '-';
		String expectedString = "Complete a simple task @phone @home";
		
		Task createdTask = TaskHelper.createTask(expectedId, expectedString);
		
		assertEquals("ID's are not equal", expectedId, createdTask.id);
		assertEquals("Priorities are not equal", expectedPriority, createdTask.prio);
		assertEquals("Text is not equal", expectedString, createdTask.text);
		
		assertEquals("Wrong number of context", 2, createdTask.contexts.size());
		assertEquals("Wrong first context", "phone", createdTask.contexts.get(0));
		assertEquals("Wrong second context", "home", createdTask.contexts.get(1));

	}
	
	/*
	 * Ensure that a context can be properly parsed out from a string if it's not at the end
	 */
	public void testCreateTaskWithContextInMiddle() {
		int expectedId = 1;
		char expectedPriority = '-';
		String expectedString = "Complete a simple @phone task";
		
		Task createdTask = TaskHelper.createTask(expectedId, expectedString);
		
		assertEquals("ID's are equal", expectedId, createdTask.id);
		assertEquals("Priorities are equal", expectedPriority, createdTask.prio);
		assertEquals("Text is equal", expectedString, createdTask.text);
		
		assertEquals("Wrong number of contexts", 1, createdTask.contexts.size());
		assertEquals("Wrong context", "phone", createdTask.contexts.get(0));
	}
	
	public void testCreateTaskDistinguishContextFromEmail() {
		int expectedId = 1;
		char expectedPriority = '-';
		String expectedString = "Email me@steveh.ca about unit testing";
		
		Task createdTask = TaskHelper.createTask(expectedId, expectedString);
		
		assertEquals("ID's are equal", expectedId, createdTask.id);
		assertEquals("Priorities are equal", expectedPriority, createdTask.prio);
		assertEquals("Text is equal", expectedString, createdTask.text);
		
		assertEquals("Wrong number of contexts", 0, createdTask.contexts.size());
	}
	
// *** START TodoUtil.createTask TESTS ***
	
}
