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

class TextSplitter {
    private final static Pattern COMPLETED_PATTERN = Pattern.compile("^([X,x] )(.*)");

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
        public final Priority priority;
        public final String text;
        public final String prependedDate;
        public final boolean completed;
        public final String completedDate;

        private SplitResult(Priority priority, String text,
                String prependedDate, boolean completed, String completedDate) {
            this.priority = priority;
            this.text = text;
            this.prependedDate = prependedDate;
            this.completed = completed;
            this.completedDate = completedDate;
        }
    }

    public SplitResult split(String inputText) {
        if (inputText == null) {
            return new SplitResult(Priority.NONE, "", "", false, "");
        }

        Matcher completedMatcher = COMPLETED_PATTERN.matcher(inputText);
        boolean completed;
        String text;

        if (completedMatcher.find()) {
            completed = true;
            text = completedMatcher.group(2);
        } else {
            completed = false;
            text = inputText;
        }

        Priority priority = Priority.NONE;

        if (!completed) {
            PriorityTextSplitter.PrioritySplitResult prioritySplitResult = PriorityTextSplitter
                    .getInstance().split(text);
            priority = prioritySplitResult.priority;
            text = prioritySplitResult.text;
        }

        String completedDate = "";
        String prependedDate = "";

        if (completed) {
            Matcher completedAndPrependedDatesMatcher = COMPLETED_PREPENDED_DATES_PATTERN
                    .matcher(text);
            if (completedAndPrependedDatesMatcher.find()) {
                completedDate = completedAndPrependedDatesMatcher.group(1);
                prependedDate = completedAndPrependedDatesMatcher.group(2);
                text = completedAndPrependedDatesMatcher.group(3);
            } else {
                Matcher completionDateMatcher = SINGLE_DATE_PATTERN
                        .matcher(text);
                if (completionDateMatcher.find()) {
                    completedDate = completionDateMatcher.group(1);
                    text = completionDateMatcher.group(2);
                }
            }
        } else {
            Matcher prependedDateMatcher = SINGLE_DATE_PATTERN.matcher(text);

            if (prependedDateMatcher.find()) {
                text = prependedDateMatcher.group(2);
                prependedDate = prependedDateMatcher.group(1);
            }
        }

        return new SplitResult(priority, text, prependedDate, completed, completedDate);
    }
}
