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
import java.util.Locale;

public class PhoneNumberParserTest extends TestCase {
    public void test_empty() {
        String input = "";
        List<String> numbers = PhoneNumberParser.getInstance().parse(input);
        assertEquals(Collections.<String> emptyList(), numbers);
    }

    public void test_null() {
        String input = null;
        List<String> numbers = PhoneNumberParser.getInstance().parse(input);
        assertEquals(Collections.<String> emptyList(), numbers);
    }

    public void test_withoutPhoneNumber() {
        String input = "a simple string";
        List<String> numbers = PhoneNumberParser.getInstance().parse(input);
        assertEquals(Collections.<String> emptyList(), numbers);
    }

    public void test_withUSPhoneNumber() {
        Locale.setDefault(Locale.US);
        String input = "a simple string with 408-555-1212";
        List<String> numbers = PhoneNumberParser.getInstance().parse(input);
        assertEquals(1, numbers.size());
        assertTrue(numbers.contains("(408) 555-1212"));
    }

    public void test_withGermanPhoneNumber() {
        Locale.setDefault(Locale.GERMANY);
        String input = "a simple string with 09363/5987";
        List<String> numbers = PhoneNumberParser.getInstance().parse(input);
        assertEquals(1, numbers.size());
        assertTrue(numbers.contains("09363/5987"));
    }

    public void test_withMultipleUSPhoneNumbers() {
        Locale.setDefault(Locale.US);
        String input = "this is a text with 405-555-1212 and 408-555-9898";
        List<String> numbers = PhoneNumberParser.getInstance().parse(input);
        assertEquals(2, numbers.size());
        assertTrue(numbers.contains("(405) 555-1212"));
        assertTrue(numbers.contains("(408) 555-9898"));
    }

    public void test_withMultipleGermanPhoneNumbers() {
        Locale.setDefault(Locale.GERMANY);
        String input = "this is a text with 09363/5987 and 09364/54587";
        List<String> numbers = PhoneNumberParser.getInstance().parse(input);
        assertEquals(2, numbers.size());
        assertTrue(numbers.contains("09363/5987"));
        assertTrue(numbers.contains("09364/54587"));
    }

    public void test_withFormattedUSPhoneNumbers() {
        Locale.setDefault(Locale.US);
        String input = "this string 405-555-1212 variety of Strings (408) 555-9898 the 4085554398 wtf 408 555 2485";
        List<String> numbers = PhoneNumberParser.getInstance().parse(input);
        assertEquals(4, numbers.size());
        assertTrue(numbers.contains("(405) 555-1212"));
        assertTrue(numbers.contains("(408) 555-9898"));
        assertTrue(numbers.contains("(408) 555-4398"));
        assertTrue(numbers.contains("(408) 555-2485"));
    }

    public void test_withFormattedGermanPhoneNumbers() {
        Locale.setDefault(Locale.GERMANY);
        String input = "this string 09363 5987 variety of Strings (09364) 54588 the 09364/54589 wtf 0936454590";
        List<String> numbers = PhoneNumberParser.getInstance().parse(input);
        assertEquals(4, numbers.size());
        assertTrue(numbers.contains("09363/5987"));
        assertTrue(numbers.contains("09364/54588"));
        assertTrue(numbers.contains("09364/54589"));
        assertTrue(numbers.contains("09364/54590"));
    }
}
