/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/task/Task.java
 *
 * Copyright (c) 2011 Tim Barlotta
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
 * @author Tim Barlotta <tim[at]barlotta[dot]net>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2011 Tim Barlotta
 */
package com.todotxt.todotxttouch.util;

/**
 * A utility class for manipulating strings
 *
 * @author Tim Barlotta
 */
public final class Strings {
    public static final String SINGLE_SPACE = " ";

    /**
     * Inserts a given string into another padding it with spaces.  Is aware if
     * the insertion point has a space on either end and does not add extra
     * spaces.
     * @param s the string to insert into
     * @param insertAt the position to insert the string
     * @param stringToInsert the string to insert
     * @return the result of inserting the stringToInsert into the passed in string
     * @throws IndexOutOfBoundsException if the insertAt is negative, or insertAt
     * is larger than the length of s String object
     */
    public static String insertPadded(String s, int insertAt, String stringToInsert) {
        if(Strings.isEmptyOrNull(stringToInsert)) {
            return s;
        }

        if(insertAt<0) {
            throw new IndexOutOfBoundsException("Invalid insertAt of ["+insertAt+"] for string ["+s+"]");
        }

        StringBuilder newText = new StringBuilder();
        if(insertAt > 0) {
            newText.append(s.substring(0, insertAt));
            if(newText.lastIndexOf(SINGLE_SPACE) != newText.length() - 1) {
                newText.append(SINGLE_SPACE);
            }
            newText.append(stringToInsert);
            String postItem = s.substring(insertAt);
            if(postItem.indexOf(SINGLE_SPACE) != 0) {
                newText.append(SINGLE_SPACE);
            }
            newText.append(postItem);
        }
        else {
            newText.append(stringToInsert);
            if(s.indexOf(SINGLE_SPACE) != 0) {
                newText.append(SINGLE_SPACE);
            }
            newText.append(s);
        }
        return newText.toString();
    }

    /**
     * Checks the passed in string to see if it is null or an blank string
     * @param s the string to check
     * @return true if null or ""
     */
    public static boolean isEmptyOrNull(String s) {
        return s == null || s.length() == 0;
    }
}
