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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

public class SortTest extends TestCase {
    private List<Task> unsortedTasks = new ArrayList<Task>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // id=1, priority=E, date='2013-09-01'
        // text='a test task'
        unsortedTasks.add(new Task(1, "(E) 2013-09-01 a test task"));
        // id=55, priority=None, date='2013-09-01'
        // text='z ultimate task'
        unsortedTasks.add(new Task(55, "2013-09-01 z ultimate task"));
        // id=99, priority=A, date='2013-09-01'
        // text='awesome task'
        unsortedTasks.add(new Task(99, "(A) 2013-09-01 awesome task"));
        // id=10, priority=E, date='2013-08-01'
        // text='another test task'
        unsortedTasks.add(new Task(10, "(E) 2013-08-01 another test task"));
        // id=140, priority=E, date=null
        // text='a third test task this time with no creation date'
        unsortedTasks.add(new Task(140, "(E) a third test task this time with no creation date"));
        // id=20, priority=None (completed), date='2013-09-01'
        // text='a completed task with a creation date'
        unsortedTasks.add(new Task(20, "x 2013-09-01 the completed task with a creation date"));
        // id=5, priority=None, date=null
        // text='A capitalized task'
        unsortedTasks.add(new Task(5, "A capitalized task"));
        // id=9999, priority=None (completed), date=null
        // text='a completed task without a creation date'
        unsortedTasks.add(new Task(9999, "x a completed task without a creation date"));
        // id=30, priority=None (completed), date=null
        // text='a completed task without a creation date'
        unsortedTasks.add(new Task(30, "x a completed task without a creation date"));
    }

    /*
     * Priority descending should list all of the tasks with the primary sort on
     * Priority (A -> Z, None, Completed) and the secondary sort on Line ID
     * ascending.
     */
    public void testSort_priorityDescending() {
        // Sequences for the following data should be:
        // priorityDescending: 99, 1, 10, 140, 5, 55, 20, 30, 9999

        Sort sort = Sort.PRIORITY_DESC;
        Collections.sort(unsortedTasks, sort.getComparator());

        assertEquals(99, unsortedTasks.get(0).getId());
        assertEquals(1, unsortedTasks.get(1).getId());
        assertEquals(10, unsortedTasks.get(2).getId());
        assertEquals(140, unsortedTasks.get(3).getId());
        assertEquals(5, unsortedTasks.get(4).getId());
        assertEquals(55, unsortedTasks.get(5).getId());
        assertEquals(20, unsortedTasks.get(6).getId());
        assertEquals(30, unsortedTasks.get(7).getId());
        assertEquals(9999, unsortedTasks.get(8).getId());
    }

    /*
     * ID descending sort is simply the line ID in descending order.
     */
    public void testSort_idDescending() {
        // Sequences for the following data should be:
        // idDescending: 9999, 140, 99, 55, 30, 20, 10, 5, 1

        Sort sort = Sort.ID_DESC;
        Collections.sort(unsortedTasks, sort.getComparator());

        assertEquals(9999, unsortedTasks.get(0).getId());
        assertEquals(140, unsortedTasks.get(1).getId());
        assertEquals(99, unsortedTasks.get(2).getId());
        assertEquals(55, unsortedTasks.get(3).getId());
        assertEquals(30, unsortedTasks.get(4).getId());
        assertEquals(20, unsortedTasks.get(5).getId());
        assertEquals(10, unsortedTasks.get(6).getId());
        assertEquals(5, unsortedTasks.get(7).getId());
        assertEquals(1, unsortedTasks.get(8).getId());
    }

    public void testSort_idAscending() {
        // Sequences for the following data should be:
        // idAscending: 1, 5, 10, 20, 30, 55, 99, 140, 9999

        Sort sort = Sort.ID_ASC;
        Collections.sort(unsortedTasks, sort.getComparator());

        assertEquals(1, unsortedTasks.get(0).getId());
        assertEquals(5, unsortedTasks.get(1).getId());
        assertEquals(10, unsortedTasks.get(2).getId());
        assertEquals(20, unsortedTasks.get(3).getId());
        assertEquals(30, unsortedTasks.get(4).getId());
        assertEquals(55, unsortedTasks.get(5).getId());
        assertEquals(99, unsortedTasks.get(6).getId());
        assertEquals(140, unsortedTasks.get(7).getId());
        assertEquals(9999, unsortedTasks.get(8).getId());
    }

    public void testSort_textAscending() {
        // Sequences for the following data should be:
        // textAscending: 5, 1, 140, 10, 99, 55, 30, 9999, 20

        Sort sort = Sort.TEXT_ASC;
        Collections.sort(unsortedTasks, sort.getComparator());

        assertEquals(5, unsortedTasks.get(0).getId());
        assertEquals(1, unsortedTasks.get(1).getId());
        assertEquals(140, unsortedTasks.get(2).getId());
        assertEquals(10, unsortedTasks.get(3).getId());
        assertEquals(99, unsortedTasks.get(4).getId());
        assertEquals(55, unsortedTasks.get(5).getId());
        assertEquals(30, unsortedTasks.get(6).getId());
        assertEquals(9999, unsortedTasks.get(7).getId());
        assertEquals(20, unsortedTasks.get(8).getId());
    }

    public void testSort_dateAscending() {
        // Sequences for the following data should be:
        // dateAscending: 10, 1, 55, 99, 5, 140, 20, 30, 9999

        Sort sort = Sort.DATE_ASC;
        Collections.sort(unsortedTasks, sort.getComparator());

        assertEquals(10, unsortedTasks.get(0).getId());
        assertEquals(1, unsortedTasks.get(1).getId());
        assertEquals(55, unsortedTasks.get(2).getId());
        assertEquals(99, unsortedTasks.get(3).getId());
        assertEquals(5, unsortedTasks.get(4).getId());
        assertEquals(140, unsortedTasks.get(5).getId());
        assertEquals(20, unsortedTasks.get(6).getId());
        assertEquals(30, unsortedTasks.get(7).getId());
        assertEquals(9999, unsortedTasks.get(8).getId());
    }

    public void testSort_dateDescending() {
        // Sequences for the following data should be:
        // dateDescending: 140, 5, 99, 55, 1, 10, 9999, 30, 20

        Sort sort = Sort.DATE_DESC;
        Collections.sort(unsortedTasks, sort.getComparator());

        assertEquals(140, unsortedTasks.get(0).getId());
        assertEquals(5, unsortedTasks.get(1).getId());
        assertEquals(99, unsortedTasks.get(2).getId());
        assertEquals(55, unsortedTasks.get(3).getId());
        assertEquals(1, unsortedTasks.get(4).getId());
        assertEquals(10, unsortedTasks.get(5).getId());
        assertEquals(9999, unsortedTasks.get(6).getId());
        assertEquals(30, unsortedTasks.get(7).getId());
        assertEquals(20, unsortedTasks.get(8).getId());
    }
}
