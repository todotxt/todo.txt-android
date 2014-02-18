/**
 * This file is part of Todo.txt for Android, an app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 *
 * LICENSE:
 *
 * Todo.txt for Android is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt for Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt for Android. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Todo.txt for Android's source code is available at https://github.com/ginatrapani/todo.txt-android
 *
 * @author Todo.txt for Android contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 */

package com.todotxt.todotxttouch.task;

import java.util.Comparator;

public enum Sort {
    /**
     * Priority descending sort should result in Tasks in the following order
     * <p>
     * (A), (B), (C), ..., (Z), (NO PRIORITY), (COMPLETED)
     * </p>
     * <p>
     * If tasks are of the same priority level, they are sorted by task id
     * ascending
     * </p>
     */
    PRIORITY_DESC(0, new Comparator<Task>() {
        @Override
        public int compare(Task t1, Task t2) {
            if ((t1 == null) || (t2 == null)) {
                throw new NullPointerException("Null task passed into comparator");
            }

            int result = t1.getSortPriority().compareTo(t2.getSortPriority());

            if (result != 0) {
                return result;
            }

            return Sort.ID_ASC.getComparator().compare(t1, t2);
        }
    }),

    /**
     * Id ascending sort should result in Tasks in the following order
     * <p>
     * 1, 2, 3, 4, ..., n
     * </p>
     */
    ID_ASC(1, new Comparator<Task>() {
        @Override
        public int compare(Task t1, Task t2) {
            if ((t1 == null) || (t2 == null)) {
                throw new NullPointerException("Null task passed into comparator");
            }

            return ((Long) t1.getId()).compareTo((Long) t2.getId());
        }
    }),

    /**
     * Id descending sort should result in Tasks in the following order
     * <p>
     * n, ..., 4, 3, 2, 1
     * </p>
     */
    ID_DESC(2, new Comparator<Task>() {
        @Override
        public int compare(Task t1, Task t2) {
            if ((t1 == null) || (t2 == null)) {
                throw new NullPointerException("Null task passed into comparator");
            }

            return ((Long) t2.getId()).compareTo((Long) t1.getId());
        }
    }),

    /**
     * Text ascending sort should result in Tasks sorting in the following order
     * <p>
     * (incomplete) a, b, c, d, e, ..., z, (completed) a, b, c, d, e, ..., z
     * </p>
     * <p>
     * If tasks are of the same text (and completion) value, they are sorted by
     * task id ascending
     * </p>
     */
    TEXT_ASC(3, new Comparator<Task>() {
        @Override
        public int compare(Task t1, Task t2) {
            if ((t1 == null) || (t2 == null)) {
                throw new NullPointerException("Null task passed into comparator");
            }

            int result = ((Boolean) t1.isCompleted()).compareTo((Boolean) t2.isCompleted());

            if (result != 0) {
                return result;
            }

            result = t1.getText().compareToIgnoreCase(t2.getText());

            if (result != 0) {
                return result;
            }

            return Sort.ID_ASC.getComparator().compare(t1, t2);
        }
    }),

    /**
     * Date ascending sort should result in Tasks ordered by creation date,
     * earliest first. Tasks with no creation date will be sorted by line number
     * in ascending order after those with a date. Followed finally by completed
     * tasks.
     * <p>
     * If tasks are of the same date sort level, they are sorted by task id
     * ascending
     * </p>
     */
    DATE_ASC(4, new Comparator<Task>() {
        @Override
        public int compare(Task t1, Task t2) {
            if ((t1 == null) || (t2 == null)) {
                throw new NullPointerException("Null task passed into comparator");
            }

            int result = t1.getAscSortDate().compareTo(t2.getAscSortDate());

            if (result != 0) {
                return result;
            }

            return Sort.ID_ASC.getComparator().compare(t1, t2);
        }
    }),

    /**
     * Date descending sort should result in Tasks ordered by creation date,
     * most recent first. Tasks with no creation date will be sorted by line
     * number in descending order before all tasks with dates.
     * <p>
     * If tasks are of the same date level, they are sorted by task id
     * descending
     * </p>
     */
    DATE_DESC(5, new Comparator<Task>() {
        @Override
        public int compare(Task t1, Task t2) {
            if ((t1 == null) || (t2 == null)) {
                throw new NullPointerException("Null task passed into comparator");
            }

            int result = t2.getDescSortDate().compareTo(t1.getDescSortDate());

            if (result != 0) {
                return result;
            }

            return Sort.ID_DESC.getComparator().compare(t1, t2);
        }
    });

    private final int id;
    private final Comparator<Task> comparator;

    private Sort(int id, Comparator<Task> comparator) {
        this.id = id;
        this.comparator = comparator;
    }

    public int getId() {
        return id;
    }

    public Comparator<Task> getComparator() {
        return comparator;
    }

    /**
     * Retrieves the sort selection by its id, default to PRIORITY_DESC if no
     * matching sort is found
     * 
     * @param id the sort id to lookup
     * @return the matching sort or PRIORITY_DESC if no match is found
     */
    public static Sort getById(int id) {
        for (Sort sort : Sort.values()) {
            if (sort.id == id) {
                return sort;
            }
        }

        return Sort.PRIORITY_DESC;
    }
}
