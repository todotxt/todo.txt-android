package com.todotxt.todotxttouch.test;

import junit.framework.TestCase;

import com.todotxt.todotxttouch.Task;
import com.todotxt.todotxttouch.TaskHelper;

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
		Task t = TaskHelper.createTask(0, expectedString);
		
		assertTrue("File output matches original input",
				expectedString.equals(TaskHelper.toFileFormat(t)));
	}
	
	/*
	 * Ensure that a task is output to a file the same way as it was read in
	 * with a priority.
	 */
	public void testToFileFormatWithPriority() {
		String expectedString = "(A) Simple test with a priority";
		Task t = TaskHelper.createTask(0, expectedString);
		
		assertTrue("File output matches original input",
				expectedString.equals(TaskHelper.toFileFormat(t)));
	}
	
	/*
	 * Ensure that a task is output to a file the same way as it was read in
	 * with a context.
	 */
	public void testToFileFormatWithContext() {
		String expectedString = "Simple test with a context @home";
		Task t = TaskHelper.createTask(0, expectedString);
		
		assertTrue("File output matches original input",
				expectedString.equals(TaskHelper.toFileFormat(t)));
	}
	
	/*
	 * Ensure that a task is output to a file the same way as it was read in
	 * with multiple context.
	 */
	public void testToFileFormatWithMultipleContexts() {
		String expectedString = "Simple @phone test @home";
		Task t = TaskHelper.createTask(0, expectedString);
		
		assertTrue("File output matches original input",
				expectedString.equals(TaskHelper.toFileFormat(t)));
	}
	
// *** END TaskHelper.toFileFormat TESTS ***
}
