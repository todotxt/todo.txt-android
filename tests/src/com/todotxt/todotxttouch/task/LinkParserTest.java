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

public class LinkParserTest extends TestCase {
    public void test_empty() {
        String input = "";
        List<URL> links = LinkParser.getInstance().parse(input);
        assertEquals(Collections.<URL> emptyList(), links);
    }

    public void test_null() {
        String input = null;
        List<URL> links = LinkParser.getInstance().parse(input);
        assertEquals(Collections.<URL> emptyList(), links);
    }

    public void test_withoutLink() {
        String input = "a simple string";
        List<URL> links = LinkParser.getInstance().parse(input);
        assertEquals(Collections.<URL> emptyList(), links);
    }

    public void test_withLinkHttp() throws MalformedURLException {
        String input = "a simple string with http://i.am.url";
        List<URL> links = LinkParser.getInstance().parse(input);
        assertEquals(1, links.size());
        assertTrue(links.contains(new URL("http://i.am.url")));
    }

    public void test_withLinkHttps() throws MalformedURLException {
        String input = "a simple string with https://i.am.url";
        List<URL> links = LinkParser.getInstance().parse(input);
        assertEquals(1, links.size());
        assertTrue(links.contains(new URL("https://i.am.url")));
    }

    public void test_withMultipleLinks() throws MalformedURLException {
        String input = "this is a text with http://a.url and https://a.nother.url";
        List<URL> links = LinkParser.getInstance().parse(input);
        assertEquals(2, links.size());
        assertTrue(links.contains(new URL("http://a.url")));
        assertTrue(links.contains(new URL("https://a.nother.url")));
    }

    public void test_withInterspersedLinks() throws MalformedURLException {
        String input = "this string http://has.a variety of urls https://beginning.with the http://and.also.the https://protocol.eof";
        List<URL> links = LinkParser.getInstance().parse(input);
        assertEquals(4, links.size());
        assertTrue(links.contains(new URL("http://has.a")));
        assertTrue(links.contains(new URL("https://beginning.with")));
        assertTrue(links.contains(new URL("http://and.also.the")));
        assertTrue(links.contains(new URL("https://protocol.eof")));
    }
}
