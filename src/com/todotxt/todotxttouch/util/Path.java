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
package com.todotxt.todotxttouch.util;

public class Path {
	public static String fileName(String path) {
		if (!Strings.isBlank(path)) {
			// adapted from DropboxAPI.java v1.5.4
			if (path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
			int ind = path.lastIndexOf('/');
			return path.substring(ind + 1, path.length());
		}
		return "";
	}

	public static String parentPath(String path) {
		// adapted from DropboxAPI.java v1.5.4
		if (Strings.isBlank(path) || path.equals("/")) {
			return "";
		} else {
			if (path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
			int ind = path.lastIndexOf('/');
			return path.substring(0, ind + 1);
		}
	}
}