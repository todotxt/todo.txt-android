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
    private final static Pattern PRIORITY_PATTERN = Pattern.compile("^\\(([A-Z])\\) (.*)");
    private final static Pattern PREPENDED_DATE_PATTERN = Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2}) (.*)");
    private final static TextSplitter INSTANCE = new TextSplitter();

    private TextSplitter() {}

    public static TextSplitter getInstance() {
        return INSTANCE;
    }

    static class SplitResult {
        public final char priority;
        public final String text;
        public final String prependedDate;

        private SplitResult(char priority, String text, String prependedDate) {
            this.priority = priority;
            this.text = text;
            this.prependedDate = prependedDate;
        }
    }

    public SplitResult split(String inputText) {
        if(inputText==null) {
            return new SplitResult(Task.NO_PRIORITY, "", "");
        }

        Matcher priorityMatcher = PRIORITY_PATTERN.matcher(inputText);
        char priority;
        String text;
        if(priorityMatcher.find()) {
            priority = priorityMatcher.group(1).charAt(0);
            text = priorityMatcher.group(2);
        }
        else {
            priority = Task.NO_PRIORITY;
            text = inputText;
        }

		Matcher prependedDateMatcher = PREPENDED_DATE_PATTERN.matcher(text);
		if (prependedDateMatcher.find()) {
			text = prependedDateMatcher.group(0).substring(11);
            return new SplitResult(priority, text, prependedDateMatcher.group(0).substring(0, 10));
		}
        else {
            return new SplitResult(priority, text, "");
        }
    }
}
