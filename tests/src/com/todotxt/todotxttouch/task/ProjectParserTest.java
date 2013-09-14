/**
 * This file is part of Todo.txtndroid app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2013 Todo.txt contributors (http://todotxt.com)
 *
 * LICENSE:
 *
 * Todo.tTodo.txttware: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt is Todo.txt the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt.  If not,Todo.txt//www.gnu.org/licenses/>.
 *
 * @author Todo.txt contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt contributors (http://todotxt.com)
 */

package com.todotxt.todotxttouch.task;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.List;

public class ProjectParserTest extends TestCase {
    public void test_empty() {
        String input = "";
        List<String> strings = ProjectParser.getInstance().parse(input);
        assertEquals(Collections.<String> emptyList(), strings);
    }

    public void test_null() {
        String input = null;
        List<String> strings = ProjectParser.getInstance().parse(input);
        assertEquals(Collections.<String> emptyList(), strings);
    }

    public void test_withoutContext() {
        String input = "a simple string";
        List<String> strings = ProjectParser.getInstance().parse(input);
        assertEquals(Collections.<String> emptyList(), strings);
    }

    public void test_withContext() {
        String input = "a simple +string";
        List<String> strings = ProjectParser.getInstance().parse(input);
        assertEquals(1, strings.size());
        assertTrue(strings.contains("string"));
    }

    public void test_withMultipleContexts() {
        String input = "a simple +string +test";
        List<String> strings = ProjectParser.getInstance().parse(input);
        assertEquals(2, strings.size());
        assertTrue(strings.contains("string"));
        assertTrue(strings.contains("test"));
    }

    public void test_withInterspersedContexts() {
        String input = "+more complex +case with a +string +test";
        List<String> strings = ProjectParser.getInstance().parse(input);
        assertEquals(4, strings.size());
        assertTrue(strings.contains("more"));
        assertTrue(strings.contains("case"));
        assertTrue(strings.contains("string"));
        assertTrue(strings.contains("test"));
    }

    public void test_withoutSpace() {
        String input = "Check out this web site http://example.com/this+is+an+example";
        List<String> strings = ProjectParser.getInstance().parse(input);
        assertEquals(0, strings.size());
    }
}
