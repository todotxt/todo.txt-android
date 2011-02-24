/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/remote/RemoteClient.java
 *
 * Copyright (c) 2011 Tormod Haugen
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
 * @author Tormod Haugen <tormodh[at]gmail[dot]com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2011 Tormod Haugen
 */
package com.todotxt.todotxttouch.remote;

import java.io.File;

public interface RemoteClient {

    Client getClient();

	/**
	 * Attempts to authenticate with remote api
	 *
	 * @return true if successful
	 */
	boolean authenticate();

	/**
	 * Attempts to deauthenticate with remote api
	 */
	void deauthenticate();

	/**
	 * Check to see if we are authenticated with remote api
	 *
	 * @return true if authenticated
	 */
	boolean isAuthenticated();

	/**
	 * Check to see if we have enough information to authenticate with remote
	 * api
	 *
	 * @return true if we have authToken, false if we need login information
	 */
	boolean isLoggedIn();

	/**
	 * Get a login task that can display and handle a login dialog
	 * @return
	 */
	RemoteLoginTask getLoginTask();

    /**
     * Pull the remote Todo.txt file
     * @return
     */
    File pullTodo();

    /**
     * Push mobile
     * @param file
     */
    void pushTodo(File file);

}
