/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/task/Task.java
 *
 * Copyright (c) 2009-2011 mathias, Gina Trapani, Tim Barlotta
 *
 * LICENSE:
 *
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
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
 * @author Tim Barlotta <tim[at]barlotta[dot]net>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2011 Tim Barlotta
 */
package com.todotxt.todotxttouch.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Values representing a Task's priority and associated utility methods
 *
 * @author Tim Barlotta
 */
public enum Priority {
    NONE("-", "", ""),
    A("A", "A", "(A)"),
    B("B", "B", "(B)"),
    C("C", "C", "(C)"),
    D("D", "D", "(D)"),
    E("E", "E", "(E)"),
    F("F", "F", "(F)"),
    G("G", "G", "(G)"),
    H("H", "H", "(H)"),
    I("I", "I", "(I)"),
    J("J", "J", "(J)"),
    K("K", "K", "(K)"),
    L("L", "L", "(L)"),
    M("M", "M", "(M)"),
    N("N", "N", "(N)"),
    O("O", "O", "(O)"),
    P("P", "P", "(P)"),
    Q("Q", "Q", "(Q)"),
    R("R", "R", "(R)"),
    S("S", "S", "(S)"),
    T("T", "T", "(T)"),
    U("U", "U", "(U)"),
    V("V", "V", "(V)"),
    W("W", "W", "(W)"),
    X("X", "X", "(X)"),
    Y("Y", "Y", "(Y)"),
    Z("Z", "Z", "(Z)");

    private final String code;
    private final String screenFormat;
    private final String fileFormat;

    private Priority(String code, String screenFormat, String fileFormat) {
        this.code = code;
        this.screenFormat = screenFormat;
        this.fileFormat = fileFormat;
    }

    public String getCode() {
        return code;
    }

    public String inScreenFormat() {
        return screenFormat;
    }

    public String inFileFormat() {
        return fileFormat;
    }

    private static Priority[] reverseValues() {
        Priority[] values = Priority.values();
        Priority[] reversed = new Priority[values.length];
        for(int i=0;i<values.length;i++) {
            int index = values.length-1-i;
            reversed[index] = values[i];
        }
        return reversed;
    }

    public static List<Priority> range(Priority p1, Priority p2) {
        ArrayList<Priority> priorities = new ArrayList<Priority>();
        boolean add = false;

        for(Priority p : (p1.ordinal()<p2.ordinal() ? Priority.values() : Priority.reverseValues())) {
            if(p==p1) {
                add = true;
            }
            if(add) {
                priorities.add(p);
            }
            if(p==p2) {
                break;
            }
        }
        return priorities;
    }

    public static List<String> rangeInCode(Priority p1, Priority p2) {
        List<Priority> priorities = Priority.range(p1, p2);
        List<String> result = new ArrayList<String>(priorities.size());
        for(Priority p : priorities) {
            result.add(p.getCode());
        }
        return result;
    }

    public static Priority toPriority(char c) {
        String s = Character.toString(c).toUpperCase();
        for(Priority p : Priority.values()) {
            if(p.code.equals(s)) {
                return p;
            }
        }
        return NONE;
    }
}
