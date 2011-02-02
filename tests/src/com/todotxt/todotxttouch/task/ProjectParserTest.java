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
 * @author Tim Barlotta <tim[at]barlotta[dot]net>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 Tim Barlotta
 */
package com.todotxt.todotxttouch.task;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.List;

/**
 * A junit based test class for the ProjectParser
 *
 * @author Tim Barlotta
 */
public class ProjectParserTest extends TestCase {
    public void test_empty() {
        String input = "";
        List<String> strings = new ProjectParser().parse(input);
        assertEquals(Collections.<String>emptyList(), strings);
    }

    public void test_null() {
        String input = null;
        List<String> strings = new ProjectParser().parse(input);
        assertEquals(Collections.<String>emptyList(), strings);
    }

    public void test_withoutContext() {
        String input = "a simple string";
        List<String> strings = new ProjectParser().parse(input);
        assertEquals(Collections.<String>emptyList(), strings);
    }

    public void test_withContext() {
        String input = "a simple +string";
        List<String> strings = new ProjectParser().parse(input);
        assertEquals(1, strings.size());
        assertTrue(strings.contains("string"));
    }

    public void test_withMultipleContexts() {
        String input = "a simple +string +test";
        List<String> strings = new ProjectParser().parse(input);
        assertEquals(2, strings.size());
        assertTrue(strings.contains("string"));
        assertTrue(strings.contains("test"));
    }

    public void test_withInterspersedContexts() {
        String input = "+more complex +case with a +string +test";
        List<String> strings = new ProjectParser().parse(input);
        assertEquals(4, strings.size());
        assertTrue(strings.contains("more"));
        assertTrue(strings.contains("case"));
        assertTrue(strings.contains("string"));
        assertTrue(strings.contains("test"));
    }
}
