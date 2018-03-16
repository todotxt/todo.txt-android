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

package com.todotxt.todotxttouch.remote;

import android.content.SharedPreferences;

import com.todotxt.todotxttouch.TodoApplication;
import com.todotxt.todotxttouch.TodoPreferences;

/**
 * Manager for obtaining, switching, etc. remote clients
 *
 * @author Tim Barlotta
 */
public class RemoteClientManager implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    // private final static String TAG =
    // RemoteClientManager.class.getSimpleName();
    @SuppressWarnings("unused")
    private Client currentClientToken;
    private RemoteClient currentClient;
    private TodoApplication todoApplication;
    private TodoPreferences sharedPreferences;

    public RemoteClientManager(TodoApplication todoApplication, TodoPreferences m_prefs) {
        this.todoApplication = todoApplication;
        this.sharedPreferences = m_prefs;
        calculateRemoteClient(m_prefs);
        currentClient.authenticate();
    }

    public RemoteClient getRemoteClient() {
        return currentClient;
    }

    /**
     * Returns the client associated with the passed in token does not switch
     * the client
     *
     * @param clientToken
     * @return
     */
    private RemoteClient getRemoteClient(Client clientToken) {
        return new DropboxRemoteClient(todoApplication, sharedPreferences);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // TODO later
    }

    private void calculateRemoteClient(TodoPreferences sharedPreferences) {
        currentClient = getRemoteClient(Client.DROPBOX);
        currentClientToken = Client.DROPBOX;
    }
}
