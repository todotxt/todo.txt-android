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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class for parsing a string to find all projects.  A project is any substring of
 * the input text that starts with + character and ends with a space or the end of the text.
 * e.g. +myproject
 */
class ProjectParser {
    private final static Pattern CONTEXT_PATTERN = Pattern.compile("\\+(\\S*\\w)");;
    private static final ProjectParser INSTANCE = new ProjectParser();

    private ProjectParser() {}

    public static ProjectParser getInstance() {
        return INSTANCE;
    }

    public List<String> parse(String inputText) {
        if(inputText==null) {
            return Collections.emptyList();
        }
		Matcher m = CONTEXT_PATTERN.matcher(inputText);
		List<String> projects = new ArrayList<String>();
		while (m.find()) {
			String project = m.group(1);
			projects.add(project);
		}
		return projects;
    }
}
