/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/task/TaskBag.java
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Interface for interacting with the tasks in aggregate
 * 
 * @author Tim Barlotta
 */
public interface TaskBag {
	void reload();

	void addAsTask(String input);

	void update(Task task);

	void delete(Task task);

	void updatePreferences(TaskBagImpl.Preferences preferences);

	List<Task> getTasks();

	List<Task> getTasks(Filter<Task> filter, Comparator<Task> comparator);

	int size();

	ArrayList<String> getProjects();

	ArrayList<String> getContexts();

	ArrayList<Priority> getPriorities();

	/* REMOTE APIs */
	void initRemote();

	void disconnectFromRemote();

	// FUTURE make this syncWithRemote()

	/**
	 * Push tasks in localRepository into remoteRepository
	 */
	void pushToRemote();

	/**
	 * Pulls tasks from remoteRepository, stores in localRepository
	 */
	void pullFromRemote();

	/* END REMOTE APIs */
}
