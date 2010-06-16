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
		
		assertEquals(expectedTask.id, createdTask.id);
		assertEquals(expectedTask.prio, createdTask.prio);
		assertEquals(expectedTask.text, createdTask.text);
		
		// Should both be zero, if createdTask's contexts count is greater, something is
		// seriously wrong.
		assertEquals(expectedTask.contexts.size(), createdTask.contexts.size());
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
		
		assertEquals(expectedTask.id, createdTask.id);
		assertEquals(expectedTask.prio, createdTask.prio);
		assertEquals(expectedTask.text, createdTask.text);
		
		// Should both be zero, if createdTask's contexts count is greater, something is
		// seriously wrong.
		assertEquals(expectedTask.contexts.size(), createdTask.contexts.size());
	}
	
}
