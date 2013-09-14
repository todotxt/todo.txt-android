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

import java.util.List;

/**
 * A junit based test class for the Priority class
 * 
 * @author Tim Barlotta
 */
public class PriorityTest extends TestCase {
    public void testAccessors_simple() {
        assertEquals("A", Priority.A.getCode());
        assertEquals("A", Priority.A.inListFormat());
        assertEquals("A", Priority.A.inDetailFormat());
        assertEquals("(A)", Priority.A.inFileFormat());
    }

    public void testToPriority_A() {
        assertSame(Priority.A, Priority.toPriority("A"));
    }

    public void testToPriority_Z() {
        assertSame(Priority.Z, Priority.toPriority("Z"));
    }

    public void testToPriority_invalid() {
        assertEquals(Priority.NONE, Priority.toPriority("9"));
    }

    public void testRange_EK() {
        List<Priority> range = Priority.range(Priority.E, Priority.K);
        assertEquals(7, range.size());
        assertSame(Priority.E, range.get(0));
        assertSame(Priority.F, range.get(1));
        assertSame(Priority.G, range.get(2));
        assertSame(Priority.H, range.get(3));
        assertSame(Priority.I, range.get(4));
        assertSame(Priority.J, range.get(5));
        assertSame(Priority.K, range.get(6));
    }

    public void testRange_ZZ() {
        List<Priority> range = Priority.range(Priority.Z, Priority.Z);
        assertEquals(1, range.size());
        assertSame(Priority.Z, range.get(0));
    }

    public void testRange_ZX() {
        List<Priority> range = Priority.range(Priority.Z, Priority.X);
        assertEquals(3, range.size());
        assertSame(Priority.Z, range.get(0));
        assertSame(Priority.Y, range.get(1));
        assertSame(Priority.X, range.get(2));
    }

    public void testRangeInCode_AD() {
        List<String> codes = Priority.rangeInCode(Priority.A, Priority.D);
        assertEquals(4, codes.size());
        assertEquals(Priority.A.getCode(), codes.get(0));
        assertEquals(Priority.B.getCode(), codes.get(1));
        assertEquals(Priority.C.getCode(), codes.get(2));
        assertEquals(Priority.D.getCode(), codes.get(3));
    }
}
