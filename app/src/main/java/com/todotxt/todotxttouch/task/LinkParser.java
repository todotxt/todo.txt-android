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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.todotxt.todotxttouch.TodoException;

public class LinkParser {
    private static final Pattern LINK_PATTERN = Pattern
            .compile("(http|https)://[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?");
    private static final LinkParser INSTANCE = new LinkParser();

    private LinkParser() {
    }

    public static LinkParser getInstance() {
        return INSTANCE;
    }

    public List<URL> parse(String inputText) {
        if (inputText == null) {
            return Collections.emptyList();
        }

        Matcher m = LINK_PATTERN.matcher(inputText);
        List<URL> links = new ArrayList<URL>();

        while (m.find()) {
            URL link;

            try {
                link = new URL(m.group());
                links.add(link);
            } catch (MalformedURLException e) {
                throw new TodoException("Malformed URL matched the regex", e);
            }
        }

        return links;
    }
}
