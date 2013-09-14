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

import java.io.File;
import java.util.List;

import com.todotxt.todotxttouch.TodoApplication;
import com.todotxt.todotxttouch.remote.Client;
import com.todotxt.todotxttouch.remote.PullTodoResult;
import com.todotxt.todotxttouch.remote.RemoteClient;
import com.todotxt.todotxttouch.remote.RemoteClientManager;
import com.todotxt.todotxttouch.remote.RemoteFolder;

public class RemoteClientManagerStub extends RemoteClientManager {

    private RemoteClient remoteClient = new RemoteClientStub();

    public RemoteClientManagerStub(TodoApplication todoApplication) {
        super(todoApplication, todoApplication.m_prefs);
    }

    @Override
    public RemoteClient getRemoteClient() {
        return remoteClient;
    }

    public class RemoteClientStub implements RemoteClient {

        @Override
        public Client getClient() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean authenticate() {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public boolean startLogin() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean finishLogin() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void deauthenticate() {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isAuthenticated() {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public boolean isLoggedIn() {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public PullTodoResult pullTodo() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void pushTodo(File todoFile, File doneFile, boolean overwrite) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isAvailable() {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public List<RemoteFolder> getSubFolders(String path) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public RemoteFolder getFolder(String path) {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
