/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2012 Todo.txt contributors (http://todotxt.com)
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
 * @copyright 2009-2012 Todo.txt contributors (http://todotxt.com)
 */
package nl.mpcjanssen.todotxtholo.task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import nl.mpcjanssen.todotxtholo.TodoApplication;
import nl.mpcjanssen.todotxtholo.TodoException;
import nl.mpcjanssen.todotxtholo.util.TaskIo;
import nl.mpcjanssen.todotxtholo.util.Util;
import android.util.Log;


/**
 * A task repository for interacting with the local file system
 * 
 * @author Tim Barlotta
 */
public class LocalFileTaskRepository {
	private static final String TAG = LocalFileTaskRepository.class
			.getSimpleName();
	final static File TODO_TXT_FILE = new File(
			TodoApplication.appContext.getFilesDir(),
			"todo.txt");
	final static File DONE_TXT_FILE = new File(
			TodoApplication.appContext.getFilesDir(),
			"done.txt");
	private final TaskBag.Preferences preferences;

	public LocalFileTaskRepository(TaskBag.Preferences m_prefs) {
		this.preferences = m_prefs;
	}

	public void init() {
		try {
			if (!TODO_TXT_FILE.exists()) {
				Util.createParentDirectory(TODO_TXT_FILE);
				TODO_TXT_FILE.createNewFile();
			}
		} catch (IOException e) {
			throw new TodoException("Error initializing LocalFile", e);
		}
	}

	public void purge() {
		TODO_TXT_FILE.delete();
	}

	public ArrayList<Task> load() {
		init();
		if (!TODO_TXT_FILE.exists()) {
			Log.w(TAG, TODO_TXT_FILE.getAbsolutePath() + " does not exist!");
			throw new TodoException(TODO_TXT_FILE.getAbsolutePath()
					+ " does not exist!");
		} else {
			try {
				return TaskIo.loadTasksFromFile(TODO_TXT_FILE);
			} catch (IOException e) {
				throw new TodoException("Error loading from local file", e);
			}
		}
	}

	public void store(ArrayList<Task> tasks) {
		TaskIo.writeToFile(tasks, TODO_TXT_FILE,
				preferences.isUseWindowsLineBreaksEnabled());
	}

	public void archive(ArrayList<Task> tasks) {
		boolean windowsLineBreaks = preferences.isUseWindowsLineBreaksEnabled();

		ArrayList<Task> completedTasks = new ArrayList<Task>(tasks.size());
		ArrayList<Task> incompleteTasks = new ArrayList<Task>(tasks.size());

		for (Task task : tasks) {
			if (task.isCompleted()) {
				completedTasks.add(task);
			} else {
				incompleteTasks.add(task);
			}
		}

		// append completed tasks to done.txt
		TaskIo.writeToFile(completedTasks, DONE_TXT_FILE, true,
				windowsLineBreaks);

		// write incomplete tasks back to todo.txt
		// TODO: remove blank lines (if we ever add support for
		// PRESERVE_BLANK_LINES)
		TaskIo.writeToFile(incompleteTasks, TODO_TXT_FILE, false,
				windowsLineBreaks);
	}

	public void loadDoneTasks(File file) {
		Util.renameFile(file, DONE_TXT_FILE, true);
	}

	public boolean todoFileModifiedSince(Date date) {
		long date_ms = 0l;
		if (date != null) {
			date_ms = date.getTime();
		}
		return date_ms < TODO_TXT_FILE.lastModified();
	}

	public boolean doneFileModifiedSince(Date date) {
		long date_ms = 0l;
		if (date != null) {
			date_ms = date.getTime();
		}
		return date_ms < DONE_TXT_FILE.lastModified();
	}
}
