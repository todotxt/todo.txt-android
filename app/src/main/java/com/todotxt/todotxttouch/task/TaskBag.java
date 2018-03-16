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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Interface for interacting with the tasks in aggregate
 *
 * @author Tim Barlotta
 */
public interface TaskBag {
    void archive();

    void unarchive(Task task);

    void reload();

    void clear();

    void addAsTask(String input);

    void update(Task task);

    void delete(Task task);

    List<Task> getTasks();

    List<Task> getTasks(Filter<Task> filter, Comparator<Task> comparator);

    int size();

    ArrayList<String> getProjects(boolean includeNone);

    ArrayList<String> getContexts(boolean includeNone);

    ArrayList<Priority> getPriorities();

    /* REMOTE APIs */
    // FUTURE make this syncWithRemote()

    /**
     * Push tasks in localRepository into remoteRepository if you're not working
     * offline
     */
    void pushToRemote(boolean overwrite);

    /**
     * Force-push tasks in localRepository into remoteRepository disregarding
     * Work Offline status
     */
    void pushToRemote(boolean overridePreference, boolean overwrite);

    /**
     * Pulls tasks from remoteRepository, stores in localRepository
     */
    void pullFromRemote();

    /**
     * Force-pull tasks from remoteRepository into localRepository disregarding
     * Work Offline status
     */
    void pullFromRemote(boolean overridePreference);

    /* END REMOTE APIs */
}
