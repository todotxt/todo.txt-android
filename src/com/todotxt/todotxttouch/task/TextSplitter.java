/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/TaskHelper.java
 *
 * Copyright (c) 2009-2011 mathias, Gina Trapani
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
 * @author mathias <mathias[at]ws7862[dot](none)>
 * @author Gina Trapani <ginatrapani[at]gmail[dot]com>
 * @author mathias <mathias[at]x2[dot](none)>
 * @author Tim Barlotta <tim[at]barlotta[dot]net>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 mathias, Gina Trapani, Tim Barlotta
 */
package com.todotxt.todotxttouch.task;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class for splitting a string into smaller components; priority, prependedDate and the rest.
 */
class TextSplitter {
    private final static Pattern PRIORITY_PATTERN = Pattern.compile("\\(([A-Z])\\) (.*)");
    private final static Pattern PREPENDED_DATE_PATTERN = Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2}) (.*)");
    private final char priority;
    private final String text;
    private final String prependedDate;

    public TextSplitter(String inputText) {
        if(inputText==null) {
            inputText = "";
        }

        Matcher priorityMatcher = PRIORITY_PATTERN.matcher(inputText);
        String tempText;
        if(priorityMatcher.find()) {
            priority = priorityMatcher.group(1).charAt(0);
            tempText = priorityMatcher.group(2);
        }
        else {
            priority = Task.NO_PRIORITY;
            tempText = inputText;
        }

		Matcher prependedDateMatcher = PREPENDED_DATE_PATTERN.matcher(tempText);
		if (prependedDateMatcher.find()) {
			this.text = prependedDateMatcher.group(0).substring(11);
			this.prependedDate = prependedDateMatcher.group(0).substring(0, 10);
		}
        else {
            this.text = tempText;
            this.prependedDate = "";
        }
    }

    public char getPriority() {
        return priority;
    }

    public String getText() {
        return text;
    }

    public String getPrependedDate() {
        return prependedDate;
    }
}
