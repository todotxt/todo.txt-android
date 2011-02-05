/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/task/TextSplitter.java
 *
 * Copyright (c) 2009-2011 mathias, Gina Trapani, Tim Barlotta
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
 * TextSplitter
 * A utility class for splitting a string into smaller components; priority,
 * prependedDate and the rest.
 *
 * @author mathias <mathias[at]x2[dot](none)>
 * @author Gina Trapani <ginatrapani[at]gmail[dot]com>
 * @author Tim Barlotta <tim[at]barlotta[dot]net>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 mathias, Gina Trapani, Tim Barlotta
 */
package com.todotxt.todotxttouch.task;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TextSplitter {
    private final static Pattern COMPLETED_PATTERN = Pattern.compile("^([X,x] )(.*)");

	private final static Pattern PRIORITY_PATTERN = Pattern
			.compile("^\\(([A-Z])\\) (.*)");

    private final static Pattern COMPLETED_PREPENDED_DATES_PATTERN = Pattern
            .compile("^(\\d{4}-\\d{2}-\\d{2}) (\\d{4}-\\d{2}-\\d{2}) (.*)");

	private final static Pattern SINGLE_DATE_PATTERN = Pattern
			.compile("^(\\d{4}-\\d{2}-\\d{2}) (.*)");

	private final static TextSplitter INSTANCE = new TextSplitter();

	private TextSplitter() {
	}

	public static TextSplitter getInstance() {
		return INSTANCE;
	}

	static class SplitResult {
		public final char priority;
		public final String text;
		public final String prependedDate;
        public final boolean completed;
        public final String completedDate;

		private SplitResult(char priority, String text, String prependedDate, boolean completed, String completedDate) {
			this.priority = priority;
			this.text = text;
			this.prependedDate = prependedDate;
            this.completed = completed;
            this.completedDate = completedDate;
		}
	}

	public SplitResult split(String inputText) {
		if (inputText == null) {
			return new SplitResult(Task.NO_PRIORITY, "", "", false, "");
		}

        Matcher completedMatcher = COMPLETED_PATTERN.matcher(inputText);
        boolean completed;
        String text;
        if(completedMatcher.find()) {
            completed = true;
            text = completedMatcher.group(2);
        }
        else {
            completed = false;
            text = inputText;
        }

        char priority = Task.NO_PRIORITY;
        if(!completed) {
            Matcher priorityMatcher = PRIORITY_PATTERN.matcher(text);
            if (priorityMatcher.find()) {
                priority = priorityMatcher.group(1).charAt(0);
                text = priorityMatcher.group(2);
            }
        }

        String completedDate = "";
        String prependedDate = "";
        if(completed) {
            Matcher completedAndPrependedDatesMatcher = COMPLETED_PREPENDED_DATES_PATTERN.matcher(text);
            if(completedAndPrependedDatesMatcher.find()) {
                completedDate = completedAndPrependedDatesMatcher.group(1);
                prependedDate = completedAndPrependedDatesMatcher.group(2);
                text = completedAndPrependedDatesMatcher.group(3);
            }
            else {
                Matcher completionDateMatcher = SINGLE_DATE_PATTERN.matcher(text);
                if(completionDateMatcher.find()) {
                    completedDate = completionDateMatcher.group(1);
                    text = completionDateMatcher.group(2);
                }
            }
        }
        else {
            Matcher prependedDateMatcher = SINGLE_DATE_PATTERN.matcher(text);
            if (prependedDateMatcher.find()) {
                text = prependedDateMatcher.group(2);
                prependedDate = prependedDateMatcher.group(1);
            }
        }

        return new SplitResult(priority, text, prependedDate, completed, completedDate);
	}
}
