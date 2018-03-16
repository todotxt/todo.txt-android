/**
 * This file is part of Todo.txt for Android, an app for managing your todo.txt file (http://todotxt.com).
 * <p>
 * Copyright (c) 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 * <p>
 * LICENSE:
 * <p>
 * Todo.txt for Android is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 * <p>
 * Todo.txt for Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with Todo.txt for Android. If not, see
 * <http://www.gnu.org/licenses/>.
 * <p>
 * Todo.txt for Android's source code is available at https://github.com/ginatrapani/todo.txt-android
 *
 * @author Todo.txt for Android contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 */

package com.todotxt.todotxttouch.task;

import com.todotxt.todotxttouch.TodoPreferences;
import com.todotxt.todotxttouch.remote.PullTodoResult;
import com.todotxt.todotxttouch.remote.RemoteClientManager;
import com.todotxt.todotxttouch.util.TaskIo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of the TaskBag interface
 *
 * @author Tim Barlotta
 */
class TaskBagImpl implements TaskBag {
    private final LocalTaskRepository localRepository;
    private final RemoteClientManager remoteClientManager;
    private TodoPreferences preferences;
    private ArrayList<Task> tasks = new ArrayList<Task>();
    private Date lastReload = null;
    private Date lastSync = null;

    public TaskBagImpl(TodoPreferences preferences,
                       LocalTaskRepository localRepository,
                       RemoteClientManager remoteClientManager) {
        this.preferences = preferences;
        this.localRepository = localRepository;
        this.remoteClientManager = remoteClientManager;
    }

    private static Task find(List<Task> tasks, Task task) {
        Task partialMatch = null;

        for (Task task2 : tasks) {
            if (task2 == task) {
                return task2;
            }

            if (task2.getText().equals(task.getOriginalText())) {
                if (task2.getPriority() == task.getOriginalPriority()) {
                    return task2;
                }

                // We prefer to find an exact match (both text and priority are
                // the same), but it is possible that priority has been lost
                // because the task has been completed, so we will consider
                // partial matches as a last resort.
                partialMatch = task2;
            }
        }

        return partialMatch;
    }

    private void store(ArrayList<Task> tasks) {
        localRepository.store(tasks);
        lastReload = null;
    }

    private void store() {
        store(this.tasks);
    }

    @Override
    public void archive() {
        try {
            reload();
            localRepository.archive(tasks);
            lastReload = null;
            reload();
        } catch (Exception e) {
            throw new TaskPersistException("An error occurred while archiving",
                    e);
        }
    }

    @Override
    public void unarchive(Task task) {
        try {
            reload();
            int index = (int) task.getId();

            if (index >= tasks.size()) {
                index = tasks.size() - 1;
            }

            if (index < 0) {
                index = 0;
            }

            tasks.add(index, task);
            store();
            removeArchivedTask(task);
        } catch (Exception e) {
            throw new TaskPersistException("An error occurred while adding {" + task + "}", e);
        }
    }

    private void removeArchivedTask(Task task) {
        ArrayList<Task> doneTasks = localRepository.loadDoneTasks();
        Task found = find(doneTasks, task);

        if (found != null) {
            doneTasks.remove(found);
            localRepository.storeDoneTasks(doneTasks);
        }
    }

    @Override
    public void reload() {
        if (lastReload == null
                || localRepository.todoFileModifiedSince(lastReload)) {
            localRepository.init();
            this.tasks = localRepository.load();
            lastReload = new Date();
        }
    }

    @Override
    public void clear() {
        this.tasks = new ArrayList<Task>();
        localRepository.purge();
        lastReload = null;
    }

    @Override
    public int size() {
        return tasks.size();
    }

    @Override
    public List<Task> getTasks() {
        return getTasks(null, null);
    }

    @Override
    public List<Task> getTasks(Filter<Task> filter, Comparator<Task> comparator) {
        ArrayList<Task> localTasks = new ArrayList<Task>();
        if (filter != null) {
            for (Task t : tasks) {
                if (filter.apply(t)) {
                    localTasks.add(t);
                }
            }
        } else {
            localTasks.addAll(tasks);
        }

        if (comparator == null) {
            comparator = Sort.PRIORITY_DESC.getComparator();
        }

        Collections.sort(localTasks, comparator);

        return localTasks;
    }

    @Override
    public void addAsTask(String input) {
        try {
            reload();
            Task task = new Task(tasks.size(), input,
                    (preferences.isPrependDateEnabled() ? new Date() : null));
            tasks.add(task);
            store();
        } catch (Exception e) {
            throw new TaskPersistException("An error occurred while adding {" + input + "}", e);
        }
    }

    @Override
    public void update(Task task) {
        try {
            reload();
            Task found = TaskBagImpl.find(tasks, task);

            if (found != null) {
                task.copyInto(found);
                // Log.i(TAG, "copied into found {" + found + "}");
                store();
            } else {
                throw new TaskPersistException("Task not found, not updated");
            }
        } catch (Exception e) {
            throw new TaskPersistException("An error occurred while updating Task {" + task + "}",
                    e);
        }
    }

    @Override
    public void delete(Task task) {
        try {
            reload();
            Task found = TaskBagImpl.find(tasks, task);

            if (found != null) {
                tasks.remove(found);
                store();
            } else {
                throw new TaskPersistException("Task not found, not deleted");
            }
        } catch (Exception e) {
            throw new TaskPersistException(
                    "An error occurred while deleting Task {" + task + "}", e);
        }
    }

    /* REMOTE APIS */
    @Override
    public void pushToRemote(boolean overwrite) {
        pushToRemote(false, overwrite);
    }

    @Override
    public void pushToRemote(boolean overridePreference, boolean overwrite) {
        if (!this.preferences.isManualModeEnabled() || overridePreference) {
            File doneFile = null;

            if (localRepository.doneFileModifiedSince(lastSync)) {
                doneFile = LocalFileTaskRepository.DONE_TXT_FILE;
            }

            remoteClientManager.getRemoteClient().pushTodo(
                    LocalFileTaskRepository.TODO_TXT_FILE, doneFile, overwrite);
            lastSync = new Date();
        }
    }

    @Override
    public void pullFromRemote() {
        pullFromRemote(false);
    }

    @Override
    public void pullFromRemote(boolean overridePreference) {
        try {
            if (!this.preferences.isManualModeEnabled() || overridePreference) {
                PullTodoResult result = remoteClientManager.getRemoteClient().pullTodo();
                File todoFile = result.getTodoFile();

                if (todoFile != null && todoFile.exists()) {
                    ArrayList<Task> remoteTasks = TaskIo.loadTasksFromFile(todoFile);
                    store(remoteTasks);
                    reload();
                }

                File doneFile = result.getDoneFile();

                if (doneFile != null && doneFile.exists()) {
                    localRepository.storeDoneTasks(doneFile);
                }

                lastSync = new Date();
            }
        } catch (IOException e) {
            throw new TaskPersistException("Error loading tasks from remote file", e);
        }
    }

    @Override
    public ArrayList<Priority> getPriorities() {
        // TODO cache this after reloads?
        Set<Priority> res = new HashSet<Priority>();

        for (Task item : tasks) {
            res.add(item.getPriority());
        }

        ArrayList<Priority> ret = new ArrayList<Priority>(res);
        Collections.sort(ret);

        return ret;
    }

    @Override
    public ArrayList<String> getContexts(boolean includeNone) {
        // TODO cache this after reloads?
        Set<String> res = new HashSet<String>();

        for (Task item : tasks) {
            res.addAll(item.getContexts());
        }

        ArrayList<String> ret = new ArrayList<String>(res);
        Collections.sort(ret);

        if (includeNone) {
            ret.add(0, "-");
        }

        return ret;
    }

    @Override
    public ArrayList<String> getProjects(boolean includeNone) {
        // TODO cache this after reloads?
        Set<String> res = new HashSet<String>();

        for (Task item : tasks) {
            res.addAll(item.getProjects());
        }

        ArrayList<String> ret = new ArrayList<String>(res);
        Collections.sort(ret);

        if (includeNone) {
            ret.add(0, "-");
        }

        return ret;
    }
}
