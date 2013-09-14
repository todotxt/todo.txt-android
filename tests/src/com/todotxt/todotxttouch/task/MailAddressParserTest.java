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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class MailAddressParserTest extends TestCase {
    public void test_empty() {
        String input = "";
        List<String> links = MailAddressParser.getInstance().parse(input);
        assertEquals(Collections.<URL> emptyList(), links);
    }

    public void test_null() {
        String input = null;
        List<String> links = MailAddressParser.getInstance().parse(input);
        assertEquals(Collections.<URL> emptyList(), links);
    }

    public void test_withoutMailAddress() {
        String input = "a simple string";
        List<String> links = MailAddressParser.getInstance().parse(input);
        assertEquals(Collections.<URL> emptyList(), links);
    }

    public void test_withMailAddress() throws MalformedURLException {
        String input = "a simple string with this@mail.address";
        List<String> links = MailAddressParser.getInstance().parse(input);
        assertEquals(1, links.size());
        assertTrue(links.contains("this@mail.address"));
    }

    public void test_withMailAddressContainingNumbers()
            throws MalformedURLException {
        String input = "a simple string with th15@ma4il45.address";
        List<String> links = MailAddressParser.getInstance().parse(input);
        assertEquals(1, links.size());
        assertTrue(links.contains("th15@ma4il45.address"));
    }

    public void test_withMailAddressContainingDotsAndHypens()
            throws MalformedURLException {
        String input = "a simple string with t.h-is@ma-il.address";
        List<String> links = MailAddressParser.getInstance().parse(input);
        assertEquals(1, links.size());
        assertTrue(links.contains("t.h-is@ma-il.address"));
    }

    public void test_withMailAddressContainingNumersDotsAndHypens()
            throws MalformedURLException {
        String input = "a simple string with t.h-1s@ma-1l.address";
        List<String> links = MailAddressParser.getInstance().parse(input);
        assertEquals(1, links.size());
        assertTrue(links.contains("t.h-1s@ma-1l.address"));
    }

    public void test_withMultipleMailAddresses() throws MalformedURLException {
        String input = "this is a text with an@mail.address and another@mail.address";
        List<String> links = MailAddressParser.getInstance().parse(input);
        assertEquals(2, links.size());
        assertTrue(links.contains("an@mail.address"));
        assertTrue(links.contains("another@mail.address"));
    }

    public void test_withInterspersedMailAddresses()
            throws MalformedURLException {
        String input = "this string h4s@a.variety of ma.il@addresses.all throughout th-is@te.xt";
        List<String> links = MailAddressParser.getInstance().parse(input);
        assertEquals(3, links.size());
        assertTrue(links.contains("h4s@a.variety"));
        assertTrue(links.contains("ma.il@addresses.all"));
        assertTrue(links.contains("th-is@te.xt"));
    }
}
