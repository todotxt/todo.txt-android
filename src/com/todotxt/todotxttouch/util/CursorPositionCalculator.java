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

public final class CursorPositionCalculator {

	/**
	 * Calculates the cursor position when a string changes based on the cursor
	 * position prior to the change
	 * 
	 * @param priorCursorPosition
	 *            the position of the cursor prior to the change
	 * @param priorValue
	 *            the prior value of the string
	 * @param newValue
	 *            the new value of the string
	 * @return the calculated position. If priorValue is null than the
	 *         calculated position will be the position just after the string.
	 *         If the newValue is null than the position will be 0.
	 */
	public static final int calculate(int priorCursorPosition,
			String priorValue, String newValue) {
		if (newValue == null) {
			return 0;
		}

		if (priorValue == null) {
			return newValue.length();
		}

		int pos = priorCursorPosition
				+ (newValue.length() - priorValue.length());
		pos = pos < 0 ? 0 : pos;
		return pos > newValue.length() ? newValue.length() : pos;
	}
}
