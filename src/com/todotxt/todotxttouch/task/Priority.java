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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public enum Priority {
    NONE("-", "   ", "", ""), A("A", "A", "A", "(A)"), B("B", "B", "B", "(B)"), C(
            "C", "C", "C", "(C)"), D("D", "D", "D", "(D)"), E("E", "E", "E",
            "(E)"), F("F", "F", "F", "(F)"), G("G", "G", "G", "(G)"), H("H",
            "H", "H", "(H)"), I("I", "I", "I", "(I)"), J("J", "J", "J", "(J)"), K(
            "K", "K", "K", "(K)"), L("L", "L", "L", "(L)"), M("M", "M", "M",
            "(M)"), N("N", "N", "N", "(N)"), O("O", "O", "O", "(O)"), P("P",
            "P", "P", "(P)"), Q("Q", "Q", "Q", "(Q)"), R("R", "R", "R", "(R)"), S(
            "S", "S", "S", "(S)"), T("T", "T", "T", "(T)"), U("U", "U", "U",
            "(U)"), V("V", "V", "V", "(V)"), W("W", "W", "W", "(W)"), X("X",
            "X", "X", "(X)"), Y("Y", "Y", "Y", "(Y)"), Z("Z", "Z", "Z", "(Z)");

    private final String code;
    private final String listFormat;
    private final String detailFormat;
    private final String fileFormat;

    private Priority(String code, String listFormat, String detailFormat, String fileFormat) {
        this.code = code;
        this.listFormat = listFormat;
        this.detailFormat = detailFormat;
        this.fileFormat = fileFormat;
    }

    public String getCode() {
        return code;
    }

    public String inListFormat() {
        return listFormat;
    }

    public String inDetailFormat() {
        return detailFormat;
    }

    public String inFileFormat() {
        return fileFormat;
    }

    private static Priority[] reverseValues() {
        Priority[] values = Priority.values();
        Priority[] reversed = new Priority[values.length];

        for (int i = 0; i < values.length; i++) {
            int index = values.length - 1 - i;
            reversed[index] = values[i];
        }

        return reversed;
    }

    public static List<Priority> range(Priority p1, Priority p2) {
        ArrayList<Priority> priorities = new ArrayList<Priority>();
        boolean add = false;

        for (Priority p : (p1.ordinal() < p2.ordinal() ? Priority.values()
                : Priority.reverseValues())) {
            if (p == p1) {
                add = true;
            }

            if (add) {
                priorities.add(p);
            }

            if (p == p2) {
                break;
            }
        }

        return priorities;
    }

    public static List<String> rangeInCode(Priority p1, Priority p2) {
        List<Priority> priorities = Priority.range(p1, p2);
        List<String> result = new ArrayList<String>(priorities.size());

        for (Priority p : priorities) {
            result.add(p.getCode());
        }

        return result;
    }

    public static ArrayList<String> inCode(Collection<Priority> priorities) {
        ArrayList<String> strings = new ArrayList<String>();

        for (Priority p : priorities) {
            strings.add(p.getCode());
        }

        return strings;
    }

    public static ArrayList<Priority> toPriority(List<String> codes) {
        ArrayList<Priority> priorities = new ArrayList<Priority>();

        for (String code : codes) {
            priorities.add(Priority.toPriority(code));
        }

        return priorities;
    }

    public static Priority toPriority(String s) {
        if (s == null) {
            return NONE;
        }

        for (Priority p : Priority.values()) {
            if (p.code.equals(s.toUpperCase(Locale.US))) {
                return p;
            }
        }

        return NONE;
    }
}
