/**
 * This file is part of Todo.txt for Android, an app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 *
 * LICENSE:
 *
 * Todo.txt for Android is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt for Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt for Android. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Todo.txt for Android's source code is available at https://github.com/ginatrapani/todo.txt-android
 *
 * @author Todo.txt for Android contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 */

package com.todotxt.todotxttouch.test;

import com.todotxt.todotxttouch.task.Filter;
import com.todotxt.todotxttouch.task.Priority;
import com.todotxt.todotxttouch.task.Task;
import com.todotxt.todotxttouch.task.TaskBag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TaskBagStub implements TaskBag {

    @Override
    public void archive() {
        // TODO Auto-generated method stub

    }

    @Override
    public void reload() {
        // TODO Auto-generated method stub

    }

    @Override
    public void addAsTask(String input) {
        // TODO Auto-generated method stub

    }

    @Override
    public void update(Task task) {
        // TODO Auto-generated method stub

    }

    @Override
    public void delete(Task task) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Task> getTasks() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Task> getTasks(Filter<Task> filter, Comparator<Task> comparator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ArrayList<String> getProjects(boolean includeNone) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ArrayList<String> getContexts(boolean includeNone) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ArrayList<Priority> getPriorities() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void pushToRemote(boolean overwrite) {
        // TODO Auto-generated method stub
        pushToRemote(false, overwrite);
    }

    @Override
    public void pushToRemote(boolean overridePreference, boolean overwrite) {
        // TODO Auto-generated method stub
        ++pushToRemoteCalled;

    }

    public int pushToRemoteCalled = 0;

    @Override
    public void pullFromRemote() {
        // TODO Auto-generated method stub
        pullFromRemote(false);
    }

    @Override
    public void pullFromRemote(boolean overridePreference) {
        // TODO Auto-generated method stub
        ++pullFromRemoteCalled;
    }

    public int pullFromRemoteCalled = 0;

    @Override
    public void unarchive(Task task) {
        // TODO Auto-generated method stub

    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

}
