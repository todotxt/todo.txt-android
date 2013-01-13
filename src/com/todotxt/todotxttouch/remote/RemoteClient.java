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
	 * Starts the login with remote api
	 * 
	 * @return true if successful
	 */
	boolean startLogin();

	/**
	 * Implement this for two-step oAuth type login to finish the process. Call
	 * this from the onResume() of your activity
	 * 
	 * @return true if successful
	 */
	boolean finishLogin();

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
	 * @deprecated This is information internal to the remote service. Will be
	 *             removed. Use {@link RemoteClient#isAuthenticated()} instead.
	 * @return true if we have authToken, false if we need login information
	 */
	boolean isLoggedIn();

	/**
	 * Pull the remote Todo.txt file
	 * 
	 * @return
	 */
	PullTodoResult pullTodo();

	/**
	 * Push mobile
	 * 
	 * @param todoFile
	 * @param doneFile
	 * @param overwrite if true, upload the files even if there
	 * 	is a remote conflict.
	 */
	void pushTodo(File todoFile, File doneFile, boolean overwrite);

	/**
	 * A method to check if the remote service is available (network, sd-card,
	 * etc)
	 * 
	 * @return true if available, false if not
	 */
	boolean isAvailable();

}
