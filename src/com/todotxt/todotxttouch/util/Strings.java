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

package com.todotxt.todotxttouch.util;

public final class Strings {
    public static final String SINGLE_SPACE = " ";

    /**
     * Inserts a given string into another padding it with spaces. Is aware if
     * the insertion point has a space on either end and does not add extra
     * spaces.
     * 
     * @param s the string to insert into
     * @param insertAt the position to insert the string
     * @param stringToInsert the string to insert
     * @return the result of inserting the stringToInsert into the passed in
     *         string
     * @throws IndexOutOfBoundsException if the insertAt is negative, or
     *             insertAt is larger than the length of s String object
     */
    public static String insertPadded(String s, int insertAt, String stringToInsert) {
        if (Strings.isEmptyOrNull(stringToInsert)) {
            return s;
        }

        if (insertAt < 0) {
            throw new IndexOutOfBoundsException("Invalid insertAt of ["
                    + insertAt + "] for string [" + s + "]");
        }

        StringBuilder newText = new StringBuilder();

        if (insertAt > 0) {
            newText.append(s.substring(0, insertAt));

            if (newText.lastIndexOf(SINGLE_SPACE) != newText.length() - 1) {
                newText.append(SINGLE_SPACE);
            }

            newText.append(stringToInsert);
            String postItem = s.substring(insertAt);

            if (postItem.indexOf(SINGLE_SPACE) != 0) {
                newText.append(SINGLE_SPACE);
            }

            newText.append(postItem);
        } else {
            newText.append(stringToInsert);

            if (s.indexOf(SINGLE_SPACE) != 0) {
                newText.append(SINGLE_SPACE);
            }

            newText.append(s);
        }

        return newText.toString();
    }

    /**
     * Inserts a given string into another padding it with spaces. Is aware if
     * the insertion point has a space on either end and does not add extra
     * spaces. If the string-to-insert is already present (and not part of
     * another word) we return the original string unchanged.
     * 
     * @param s the string to insert into
     * @param insertAt the position to insert the string
     * @param stringToInsert the string to insert
     * @return the result of inserting the stringToInsert into the passed in
     *         string
     * @throws IndexOutOfBoundsException if the insertAt is negative, or
     *             insertAt is larger than the length of s String object
     */
    public static String insertPaddedIfNeeded(String s, int insertAt, String stringToInsert) {
        if (Strings.isEmptyOrNull(stringToInsert)) {
            return s;
        }

        boolean found = false;
        int startPos = 0;

        while ((startPos < s.length()) && (!found)) {
            int pos = s.indexOf(stringToInsert, startPos);

            if (pos < 0)
                break;

            startPos = pos + 1;
            int before = pos - 1;
            int after = pos + stringToInsert.length();

            if (((pos == 0) || (Character.isWhitespace(s.charAt(before)))) &&
                    ((after >= s.length()) || (Character.isWhitespace(s.charAt(after)))))
                found = true;
        }

        if (found) {
            StringBuilder newText = new StringBuilder(s);

            if (newText.lastIndexOf(SINGLE_SPACE) != newText.length() - 1) {
                newText.append(SINGLE_SPACE);
            }

            return (newText.toString());
        } else
            return (Strings.insertPadded(s, insertAt, stringToInsert));
    }

    /**
     * Checks the passed in string to see if it is null or an empty string
     * 
     * @param s the string to check
     * @return true if null or ""
     */
    public static boolean isEmptyOrNull(String s) {
        return s == null || s.length() == 0;
    }

    /**
     * Checks the passed in string to see if it is null, empty, or blank; where
     * 'blank' is defined as consisting entirely of whitespace.
     * 
     * @param s the string to check
     * @return true if null or "" or all whitespace
     */
    public static boolean isBlank(String s) {
        return isEmptyOrNull(s) || s.trim().length() == 0;
    }
}
