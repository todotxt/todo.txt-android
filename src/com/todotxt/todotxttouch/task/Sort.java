/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2013 Todo.txt contributors (http://todotxt.com)
 *
 * LICENSE:
 *
 * Todo.txt Touch is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt Touch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt Touch.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * @author Todo.txt contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt contributors (http://todotxt.com)
 */
package com.todotxt.todotxttouch.task;

import java.util.Comparator;

import com.todotxt.todotxttouch.util.Strings;

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
	 * <p>
	 * Note: this comparator imposes orderings that are inconsistent with
	 * equals.
	 * </p>
	 */
	PRIORITY_DESC(0, new Comparator<Task>() {
		@Override
		public int compare(Task t1, Task t2) {
			if (t1 == null || t2 == null) {
				throw new NullPointerException(
						"Null task passed into comparator");
			}

			if (t1.isCompleted() && t2.isCompleted()) {
				return Sort.ID_ASC.getComparator().compare(t1, t2);
			}

			if (t1.isCompleted() || t2.isCompleted()) {
				if (t1.isCompleted()) {
					return 1;
				} else {
					return -1;
				}
			}

			if (t1.getPriority() == Priority.NONE
					&& t2.getPriority() == Priority.NONE) {
				return Sort.ID_ASC.getComparator().compare(t1, t2);
			}

			if (t1.getPriority() == Priority.NONE
					|| t2.getPriority() == Priority.NONE) {
				if (t1.getPriority() == Priority.NONE) {
					return 1;
				} else {
					return -1;
				}
			}

			int result = t1.getPriority().compareTo(t2.getPriority());
			if (result == 0) {
				result = Sort.ID_ASC.getComparator().compare(t1, t2);
			}
			return result;
		}
	}),

	/**
	 * Id ascending sort should result in Tasks in the following order
	 * <p>
	 * 1, 2, 3, 4, ..., n
	 * </p>
	 * <p>
	 * Note: this comparator imposes orderings that are inconsistent with
	 * equals.
	 * </p>
	 */
	ID_ASC(1, new Comparator<Task>() {
		@Override
		public int compare(Task t1, Task t2) {
			if (t1 == null || t2 == null) {
				throw new NullPointerException(
						"Null task passed into comparator");
			}

			return Long.valueOf(t1.getId()).compareTo(t2.getId());
		}
	}),

	/**
	 * Id ascending sort should result in Tasks in the following order
	 * <p>
	 * n, ..., 4, 3, 2, 1
	 * </p>
	 * <p>
	 * Note: this comparator imposes orderings that are inconsistent with
	 * equals.
	 * </p>
	 */
	ID_DESC(2, new Comparator<Task>() {
		@Override
		public int compare(Task t1, Task t2) {
			if (t1 == null || t2 == null) {
				throw new NullPointerException(
						"Null task passed into comparator");
			}

			return Long.valueOf(t2.getId()).compareTo(t1.getId());
		}
	}),

	/**
	 * Text ascending sort should result in Tasks sorting in the following order
	 * <p>
	 * a, b, c, d, e, ..., z
	 * </p>
	 * <p>
	 * If tasks are of the same priority level, they are sorted by task id
	 * ascending
	 * </p>
	 * <p>
	 * Note: this comparator imposes orderings that are inconsistent with
	 * equals.
	 * </p>
	 */
	TEXT_ASC(3, new Comparator<Task>() {
		@Override
		public int compare(Task t1, Task t2) {
			if (t1 == null || t2 == null) {
				throw new NullPointerException(
						"Null task passed into comparator");
			}

			int result = t1.getText().compareToIgnoreCase(t2.getText());
			if (result == 0) {
				result = Sort.ID_ASC.getComparator().compare(t1, t2);
			}
			return result;
		}
	}),

	/**
	 * Date ascending sort should result in Tasks ordered by creation date,
	 * earliest first. Tasks with no creation date will be sorted by line number
	 * in ascending order
	 * <p>
	 * Note: this comparator imposes orderings that are inconsistent with
	 * equals.
	 * </p>
	 */
	DATE_ASC(4, new Comparator<Task>() {
		@Override
		public int compare(Task t1, Task t2) {
			if (t1 == null || t2 == null) {
				throw new NullPointerException(
						"Null task passed into comparator");
			}

			int result = 0;
			if (!Strings.isEmptyOrNull(t1.getPrependedDate())
					&& !Strings.isEmptyOrNull(t2.getPrependedDate())) {
				result = t1.getPrependedDate().compareTo(t2.getPrependedDate());
			}
			if (result == 0) {
				result = Sort.ID_ASC.getComparator().compare(t1, t2);
			}
			return result;
		}
	}),

	/**
	 * Date descending sort should result in Tasks ordered by creation date,
	 * most recent first. Tasks with no creation date will be sorted by line
	 * number in descending order
	 * <p>
	 * Note: this comparator imposes orderings that are inconsistent with
	 * equals.
	 * </p>
	 */
	DATE_DESC(5, new Comparator<Task>() {
		@Override
		public int compare(Task t1, Task t2) {
			if (t1 == null || t2 == null) {
				throw new NullPointerException(
						"Null task passed into comparator");
			}

			int result = 0;
			if (!Strings.isEmptyOrNull(t1.getPrependedDate())
					&& !Strings.isEmptyOrNull(t2.getPrependedDate())) {
				result = t2.getPrependedDate().compareTo(t1.getPrependedDate());
			}
			if (result == 0) {
				result = Sort.ID_DESC.getComparator().compare(t1, t2);
			}
			return result;
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
	 * @param id
	 *            the sort id to lookup
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
