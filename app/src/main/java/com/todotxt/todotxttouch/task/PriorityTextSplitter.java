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

package com.todotxt.todotxttouch.task;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PriorityTextSplitter {
    private final static Pattern PRIORITY_PATTERN = Pattern.compile("^\\(([A-Z])\\) (.*)");

    private final static PriorityTextSplitter INSTANCE = new PriorityTextSplitter();

    private PriorityTextSplitter() {
    }

    public static PriorityTextSplitter getInstance() {
        return INSTANCE;
    }

    public static class PrioritySplitResult {
        public final Priority priority;
        public final String text;

        public PrioritySplitResult(Priority priority, String text) {
            this.priority = priority;
            this.text = text;
        }
    }

    public PrioritySplitResult split(String text) {
        if (text == null) {
            return new PrioritySplitResult(Priority.NONE, "");
        }

        Priority priority = Priority.NONE;
        Matcher priorityMatcher = PRIORITY_PATTERN.matcher(text);

        if (priorityMatcher.find()) {
            priority = Priority.toPriority(priorityMatcher.group(1));
            text = priorityMatcher.group(2);
        }

        return new PrioritySplitResult(priority, text);
    }
}
