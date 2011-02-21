/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/remote/RemoteFactory.java
 *
 * Copyright (c) 2009-2011 Tormod Haugen
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
 * @copyright 2009-2011 Tormod Haugen
 */
package com.todotxt.todotxttouch.remote;

import com.todotxt.todotxttouch.TodoApplication;
import com.todotxt.todotxttouch.remote.dropbox.DropboxRemoteTaskRepository;
import com.todotxt.todotxttouch.remote.dropbox.DropboxRemoteClient;

public class RemoteFactory {

	public static RemoteClient getRemoteClient(TodoApplication todoApplication) {
		return new DropboxRemoteClient(todoApplication);
	}

	public static RemoteTaskRepository getRemoteTaskRepository(TodoApplication todoApplication) {
		return new DropboxRemoteTaskRepository(todoApplication);
	}
}
