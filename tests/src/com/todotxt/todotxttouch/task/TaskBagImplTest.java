/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2013 Todo.txt contributors (http://todotxt.com)
 *
 * LICENSE:
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
 * @author Todo.txt contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt contributors (http://todotxt.com)
 */
package com.todotxt.todotxttouch.task;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;

import android.preference.PreferenceManager;
import android.test.AndroidTestCase;

public class TaskBagImplTest extends AndroidTestCase {

	class TestLocalTaskRepository implements LocalTaskRepository {

		@Override
		public boolean todoFileModifiedSince(Date date) {
			return false;
		}

		@Override
		public void store(ArrayList<Task> tasks) {
		}

		@Override
		public void purge() {
		}

		@Override
		public void loadDoneTasks(File file) {
		}

		@Override
		public ArrayList<Task> load() {
			return null;
		}

		@Override
		public void init() {
		}

		@Override
		public boolean doneFileModifiedSince(Date date) {
			return false;
		}

		@Override
		public void archive(ArrayList<Task> tasks) {
		}
	};

	private String input1 = "A Simple test with no curve balls";
	private String input2 = "Another test with no curve balls";
	private Task task1;
	private Task task2;
	ArrayList<Task> list1;
	ArrayList<Task> list2;
	TaskBagImpl.Preferences prefs;
	
	protected void setUp() throws Exception {
		task1 = new Task(1, input1);
		task2 = new Task(2, input2);
		list1 = new ArrayList<Task>(2);
		list2 = new ArrayList<Task>(2);
		prefs = new TaskBagImpl.Preferences(PreferenceManager.getDefaultSharedPreferences(getContext()));
	}

	public void testReload() {
		LocalTaskRepository repo = new TestLocalTaskRepository() {
			public ArrayList<Task> load() {
				return list1;
			}
		};

		list1.add(task1);
		list1.add(task2);

		TaskBagImpl taskBag = new TaskBagImpl(null, repo, null);

		assertEquals(0, taskBag.size());

		taskBag.reload();

		assertEquals(2, taskBag.size());

		assertEquals(taskBag.getTasks().get(0), task1);
		assertEquals(taskBag.getTasks().get(1), task2);

	}

	public void testReloadModified() {
		LocalTaskRepository repo = new TestLocalTaskRepository() {
			boolean first = true;

			public boolean todoFileModifiedSince(Date date) {
				return !first;
			}

			public ArrayList<Task> load() {
				if (first) {
					first = false;
					return list1;
				} else {
					return list2;
				}
			}
		};

		list1.add(task1);
		list2.add(task2);

		TaskBagImpl taskBag = new TaskBagImpl(null, repo, null);

		assertEquals(0, taskBag.size());

		taskBag.reload();

		assertEquals(1, taskBag.size());

		taskBag.reload();

		assertEquals(1, taskBag.size());

		assertEquals(taskBag.getTasks().get(0), task2);

	}

	public void testReloadNotModified() {
		LocalTaskRepository repo = new TestLocalTaskRepository() {
			public ArrayList<Task> load() {
				return list1;
			}
		};

		list1.add(task1);
		list1.add(task2);

		TaskBagImpl taskBag = new TaskBagImpl(null, repo, null);

		assertEquals(0, taskBag.size());

		taskBag.reload();

		assertEquals(2, taskBag.size());

		assertEquals(taskBag.getTasks().get(0), task1);
		assertEquals(taskBag.getTasks().get(1), task2);

		taskBag.reload();

		try {
			Field listField = TaskBagImpl.class.getDeclaredField("tasks");
			listField.setAccessible(true);
			assertEquals(list1, listField.get(taskBag));
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testAddAsTaskToNewList() {
		LocalTaskRepository repo = new TestLocalTaskRepository() {
			public ArrayList<Task> load() {
				return list1;
			}
		};

		TaskBagImpl taskBag = new TaskBagImpl(prefs, repo, null);

		assertEquals(0, taskBag.size());

		taskBag.addAsTask(input1);

		assertEquals(1, taskBag.size());

		assertEquals(taskBag.getTasks().get(0).getText(), input1);

	}

	public void testAddAsTaskToExistingList() {
		LocalTaskRepository repo = new TestLocalTaskRepository() {
			public ArrayList<Task> load() {
				return list1;
			}
		};

		list1.add(task1);
		
		TaskBagImpl taskBag = new TaskBagImpl(prefs, repo, null);

		assertEquals(0, taskBag.size());

		taskBag.addAsTask(input2);

		assertEquals(2, taskBag.size());

		assertEquals(taskBag.getTasks().get(0).getText(), input1);
		assertEquals(taskBag.getTasks().get(1).getText(), input2);

	}

	public void testUpdate() {
		String newText = "new text";
		LocalTaskRepository repo = new TestLocalTaskRepository() {
			public ArrayList<Task> load() {
				return list1;
			}
		};

		list1.add(task1);
		list1.add(task2);
		
		TaskBagImpl taskBag = new TaskBagImpl(prefs, repo, null);

		assertEquals(0, taskBag.size());

		task2.update(newText);
		taskBag.update(task2);

		assertEquals(2, taskBag.size());

		assertEquals(taskBag.getTasks().get(0).getText(), input1);
		assertEquals(taskBag.getTasks().get(1).getText(), newText);

	}

	public void testUpdateFailsOnNonExistentTask() {
		String newText = "new text";
		LocalTaskRepository repo = new TestLocalTaskRepository() {
			public ArrayList<Task> load() {
				return list1;
			}
		};

		list1.add(task1);
		
		TaskBagImpl taskBag = new TaskBagImpl(prefs, repo, null);

		assertEquals(0, taskBag.size());

		task2.update(newText);
		
		boolean thrown = false;
		try {
			taskBag.update(task2);
		} catch (TaskPersistException e) {
			thrown = true;
		}
		assertTrue("TaskPersistException should be thrown", thrown);
		assertEquals(1, taskBag.size());

		assertEquals(taskBag.getTasks().get(0).getText(), input1);

	}

	public void testRemove() {
		LocalTaskRepository repo = new TestLocalTaskRepository() {
			public ArrayList<Task> load() {
				return list1;
			}
		};

		list1.add(task1);
		list1.add(task2);
		
		TaskBagImpl taskBag = new TaskBagImpl(prefs, repo, null);

		assertEquals(0, taskBag.size());

		taskBag.delete(task2);

		assertEquals(1, taskBag.size());

		assertEquals(taskBag.getTasks().get(0).getText(), input1);

	}

	public void testRemoveFailsOnNonExistentTask() {
		LocalTaskRepository repo = new TestLocalTaskRepository() {
			public ArrayList<Task> load() {
				return list1;
			}
		};

		list1.add(task1);
		
		TaskBagImpl taskBag = new TaskBagImpl(prefs, repo, null);

		assertEquals(0, taskBag.size());

		boolean thrown = false;
		try {
			taskBag.delete(task2);
		} catch (TaskPersistException e) {
			thrown = true;
		}
		assertTrue("TaskPersistException should be thrown", thrown);

		assertEquals(1, taskBag.size());

		assertEquals(taskBag.getTasks().get(0).getText(), input1);

	}

}
