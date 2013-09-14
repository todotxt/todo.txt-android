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

package com.todotxt.todotxttouch.util;

import junit.framework.TestCase;

public class StringsTest extends TestCase {
    public void testIsEmptyOrNull_null() {
        assertTrue(Strings.isEmptyOrNull(null));
    }

    public void testIsEmptyOrNull_emptyString() {
        assertTrue(Strings.isEmptyOrNull(""));
    }

    public void testIsEmptyOrNull_nonEmpty() {
        assertFalse(Strings.isEmptyOrNull("qwerty"));
    }

    public void testIsEmptyOrNull_singleSpace() {
        assertFalse(Strings.isEmptyOrNull(" "));
    }

    public void testInsertPadded_null() {
        assertEquals("thistest", Strings.insertPadded("thistest", 4, null));
    }

    public void testInsertPadded_blank() {
        assertEquals("thistest", Strings.insertPadded("thistest", 4, ""));
    }

    public void testInsertPadded_invalidInsertionPoint_toosmall() {
        try {
            assertEquals("thistest", Strings.insertPadded("thistest", -1, "is"));
            fail("Exception not thrown");
        } catch (IndexOutOfBoundsException e) {
            // success
        }
    }

    public void testInsertPadded_invalidInsertionPoint_toolarge() {
        try {
            assertEquals("thistest", Strings.insertPadded("thistest", 99, "is"));
            fail("Exception not thrown");
        } catch (IndexOutOfBoundsException e) {
            // success
        }
    }

    public void testInsertPadded_simple() {
        assertEquals("this is test", Strings.insertPadded("thistest", 4, "is"));
    }

    public void testInsertPadded_simpleBegin() {
        assertEquals("is thistest", Strings.insertPadded("thistest", 0, "is"));
    }

    public void testInsertPadded_simpleEnd() {
        assertEquals("thistest is ", Strings.insertPadded("thistest", 8, "is"));
    }

    public void testInsertPadded_prepadded() {
        assertEquals("this is test", Strings.insertPadded("this test", 4, "is"));
    }

    public void testInsertPadded_prepaddedBegin() {
        assertEquals("is this test",
                Strings.insertPadded(" this test", 0, "is"));
    }

    public void testInsertPadded_prepaddedEnd1() {
        assertEquals("this test is ",
                Strings.insertPadded("this test ", 9, "is"));
    }

    public void testInsertPadded_prepaddedEnd2() {
        assertEquals("this test is ",
                Strings.insertPadded("this test ", 10, "is"));
    }

    public void testInsertPaddedIfNeeded_intoEmpty() {
        assertEquals("thing one ",
                Strings.insertPaddedIfNeeded("", 0, "thing one"));
    }

    public void testInsertPaddedIfNeeded_alreadyThere() {
        assertEquals("@errands hi ",
                Strings.insertPaddedIfNeeded("@errands hi", 11, "@errands"));
        assertEquals("hi @errands ",
                Strings.insertPaddedIfNeeded("hi @errands", 11, "@errands"));
        assertEquals("+project hi ",
                Strings.insertPaddedIfNeeded("+project hi", 11, "+project"));
        assertEquals("hi +project ",
                Strings.insertPaddedIfNeeded("hi +project", 11, "+project"));
    }

    public void testInsertPaddedIfNeeded_supersetThere() {
        assertEquals("@errands2 hi @errands ",
                Strings.insertPaddedIfNeeded("@errands2 hi", 12, "@errands"));
        assertEquals("+project2 hi +project ",
                Strings.insertPaddedIfNeeded("+project2 hi", 12, "+project"));
        assertEquals("john@email @email ",
                Strings.insertPaddedIfNeeded("john@email", 10, "@email"));
    }
}
