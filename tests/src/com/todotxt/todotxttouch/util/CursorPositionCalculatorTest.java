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

public class CursorPositionCalculatorTest extends TestCase {
    public void testCalculate_nullPrior() {
        assertEquals(7, CursorPositionCalculator.calculate(0, null, "123test"));
    }

    public void testCalculate_nullNew() {
        assertEquals(0, CursorPositionCalculator.calculate(0, "test", null));
    }

    public void testCalculate_simpleBegin() {
        assertEquals(3,
                CursorPositionCalculator.calculate(0, "test", "123test"));
    }

    public void testCalculate_simpleEnd() {
        assertEquals(7,
                CursorPositionCalculator.calculate(4, "test", "test123"));
    }

    public void testCalculate_emptyPrior() {
        assertEquals(7, CursorPositionCalculator.calculate(0, "", "123test"));
    }

    public void testCalculate_emptyNew() {
        assertEquals(0, CursorPositionCalculator.calculate(0, "test", ""));
    }

    public void testCalculate_nonsense1() {
        assertEquals(7,
                CursorPositionCalculator.calculate(99, "test", "test123"));
    }
}
