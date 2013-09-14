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

package com.todotxt.todotxttouch.test;

import com.todotxt.todotxttouch.TodoTxtTouch;
import android.test.ActivityInstrumentationTestCase2;

public class TodoTxtTouchTest extends
        ActivityInstrumentationTestCase2<TodoTxtTouch> {

    @SuppressWarnings("unused")
    // TODO: Remove
    private TodoTxtTouch m_activity;

    @SuppressWarnings("deprecation")
    public TodoTxtTouchTest() {
        super("com.todotxt.todotxttouch", TodoTxtTouch.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        m_activity = this.getActivity();
    }
}
