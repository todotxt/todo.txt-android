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

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Method;

import android.util.Log;

import junit.framework.TestCase;

public class TaskIOTest extends TestCase {
    private static final String CR = "\r";
    private static final String LF = "\n";
    private static final String CRLF = "\r\n";

    private String runReadLine(BufferedReader r) {
        try {
            Class<?> parameterTypes[] = new Class[1];
            parameterTypes[0] = BufferedReader.class;
            Method method = TaskIo.class.getDeclaredMethod(
                    "readLine", parameterTypes);
            method.setAccessible(true);
            return (String) method.invoke(null, r);
        } catch (Exception e) {
            Log.e("TEST", "Failed to run readLine", e);
            fail(e.toString());
        }
        return null;
    }

    private String runReadLine(String inStr) {
        return runReadLine(createReader(inStr));
    }

    private BufferedReader createReader(String inStr) {
        return new BufferedReader(new StringReader(inStr));
    }

    public void testReadLineEmptyInput() {
        assertEquals(null, runReadLine(""));
    }

    public void testReadLineNoLineEnding() {
        String str = "Where'd you get the coconuts?";
        assertEquals(str, runReadLine(str));
    }

    public void testReadLineLFAfter() {
        String str = "Well, she turned me into a newt." + LF;
        BufferedReader r = createReader(str);
        assertEquals(str, runReadLine(r));
        assertEquals(null, runReadLine(r));
    }

    public void testReadLineCRAfter() {
        String str = "We want a shrubbery!!" + CR;
        BufferedReader r = createReader(str);
        assertEquals(str, runReadLine(r));
        assertEquals(null, runReadLine(r));
    }

    public void testReadLineCRLFAfter() {
        String str = "Go and boil your bottoms, sons of a silly person!" + CRLF;
        BufferedReader r = createReader(str);
        assertEquals(str, runReadLine(r));
        assertEquals(null, runReadLine(r));
    }

    public void testReadLineLFBefore() {
        String str = "Found them? In Mercia?! The coconut's tropical!";
        BufferedReader r = createReader(LF + str);
        assertEquals(LF, runReadLine(r));
        assertEquals(str, runReadLine(r));
        assertEquals(null, runReadLine(r));
    }

    public void testReadLineCRBefore() {
        String str = "We shall say 'Ni' again to you, if you do not appease us.";
        BufferedReader r = createReader(CR + str);
        assertEquals(CR, runReadLine(r));
        assertEquals(str, runReadLine(r));
        assertEquals(null, runReadLine(r));
    }

    public void testReadLineCRLFBefore() {
        String str = "How do you know she is a witch?";
        BufferedReader r = createReader(CRLF + str);
        assertEquals(CRLF, runReadLine(r));
        assertEquals(str, runReadLine(r));
        assertEquals(null, runReadLine(r));
    }

    public void testReadLineLFBetween() {
        String str1 = "Listen. Strange women lying in ponds distributing swords is no basis for a system of government.";
        String str2 = "Supreme executive power derives from a mandate from the masses, not from some farcical aquatic ceremony.";
        BufferedReader r = createReader(str1 + LF + str2);
        assertEquals(str1 + LF, runReadLine(r));
        assertEquals(str2, runReadLine(r));
        assertEquals(null, runReadLine(r));
    }

    public void testReadLineCRBetween() {
        String str1 = "Come and see the violence inherent in the system!";
        String str2 = "Help, help, I'm being repressed!";
        BufferedReader r = createReader(str1 + CR + str2);
        assertEquals(str1 + CR, runReadLine(r));
        assertEquals(str2, runReadLine(r));
        assertEquals(null, runReadLine(r));
    }

    public void testReadLineCRLFBetween() {
        String str1 = "Listen. Strange women lying in ponds distributing swords is no basis for a system of government.";
        String str2 = "Supreme executive power derives from a mandate from the masses, not from some farcical aquatic ceremony.";
        BufferedReader r = createReader(str1 + CRLF + str2);
        assertEquals(str1 + CRLF, runReadLine(r));
        assertEquals(str2, runReadLine(r));
        assertEquals(null, runReadLine(r));
    }

    public void testReadLineLFLFBetween() {
        String str1 = "The swallow may fly south with the sun, ";
        String str2 = "and the house martin or the plover may seek warmer climes in winter, yet these are not strangers to our land.";
        BufferedReader r = createReader(str1 + LF + LF + str2);
        assertEquals(str1 + LF, runReadLine(r));
        assertEquals(LF, runReadLine(r));
        assertEquals(str2, runReadLine(r));
        assertEquals(null, runReadLine(r));
    }

    public void testReadLineLFCRBetween() {
        String str1 = "Who's that then?";
        String str2 = "I dunno. Must be a king.";
        BufferedReader r = createReader(str1 + LF + CR + str2);
        assertEquals(str1 + LF, runReadLine(r));
        assertEquals(CR, runReadLine(r));
        assertEquals(str2, runReadLine(r));
        assertEquals(null, runReadLine(r));
    }

    public void testReadLineCRCRBetween() {
        String str1 = "You don't vote for kings";
        String str2 = " Well, how'd you become king, then?";
        BufferedReader r = createReader(str1 + CR + CR + str2);
        assertEquals(str1 + CR, runReadLine(r));
        assertEquals(CR, runReadLine(r));
        assertEquals(str2, runReadLine(r));
        assertEquals(null, runReadLine(r));
    }

    public void testReadLineCRCRLFBetween() {
        String str1 = "Your mother was a hamster and your father smelt of elderberries!";
        String str2 = "Now leave before I am forced to taunt you a second time!";
        BufferedReader r = createReader(str1 + CR + CRLF + str2);
        assertEquals(str1 + CR, runReadLine(r));
        assertEquals(CRLF, runReadLine(r));
        assertEquals(str2, runReadLine(r));
        assertEquals(null, runReadLine(r));
    }

    public void testReadLineLFLFCRBetween() {
        String str1 = "And this isn't my nose. This is a false one.";
        String str2 = "Well, we did do the nose.";
        BufferedReader r = createReader(str1 + LF + LF + CR + str2);
        assertEquals(str1 + LF, runReadLine(r));
        assertEquals(LF, runReadLine(r));
        assertEquals(CR, runReadLine(r));
        assertEquals(str2, runReadLine(r));
        assertEquals(null, runReadLine(r));
    }

}
