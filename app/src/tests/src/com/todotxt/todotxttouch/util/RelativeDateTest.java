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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.test.InstrumentationTestCase;

import com.todotxt.todotxttouch.R;

public class RelativeDateTest extends InstrumentationTestCase {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private String res(int resId) {
        return getInstrumentation().getTargetContext().getString(resId);
    }

    private String res(int resId, int val) {
        return getInstrumentation().getTargetContext().getString(resId, val);
    }

    private Calendar date(String str) {
        try {
            Date d = sdf.parse(str);
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(d);
            return calendar;
        } catch (ParseException e) {
            e.printStackTrace();
            fail();
            return null;
        }
    }

    public void testNow() {
        Calendar today = new GregorianCalendar();
        today.set(GregorianCalendar.HOUR_OF_DAY, 0);
        today.set(GregorianCalendar.MINUTE, 0);
        today.set(GregorianCalendar.SECOND, 0);
        today.set(GregorianCalendar.MILLISECOND, 0);
        String actual = RelativeDate.getRelativeDate(today, today);
        assertEquals(res(R.string.dates_today), actual);
    }

    public void test1DayFromNow() {
        Calendar d1 = date("2013-01-01");
        Calendar d2 = date("2013-01-02");
        String actual = RelativeDate.getRelativeDate(d1, d2);
        assertEquals("2013-01-02", actual);
    }

    public void testToday() {
        Calendar d1 = date("2013-01-01");
        Calendar d2 = date("2013-01-01");
        String actual = RelativeDate.getRelativeDate(d1, d2);
        assertEquals(res(R.string.dates_today), actual);
    }

    public void test1DayAgo() {
        Calendar d1 = date("2013-01-01");
        Calendar d2 = date("2012-12-31");
        String actual = RelativeDate.getRelativeDate(d1, d2);
        assertEquals(res(R.string.dates_one_day_ago), actual);
    }

    public void test2DaysAgo() {
        Calendar d1 = date("2013-01-01");
        Calendar d2 = date("2012-12-30");
        String actual = RelativeDate.getRelativeDate(d1, d2);
        assertEquals(res(R.string.dates_days_ago, 2), actual);
    }

    public void test29DaysAgo() {
        Calendar d1 = date("2013-01-01");
        Calendar d2 = date("2012-12-03");
        String actual = RelativeDate.getRelativeDate(d1, d2);
        assertEquals(res(R.string.dates_days_ago, 29), actual);
    }

    public void test30DaysAgo() {
        Calendar d1 = date("2013-01-01");
        Calendar d2 = date("2012-12-02");
        String actual = RelativeDate.getRelativeDate(d1, d2);
        assertEquals(res(R.string.dates_one_month_ago), actual);
    }

    public void test59DaysAgo() {
        Calendar d1 = date("2013-01-01");
        Calendar d2 = date("2012-11-03");
        String actual = RelativeDate.getRelativeDate(d1, d2);
        assertEquals(res(R.string.dates_one_month_ago), actual);
    }

    public void test60DaysAgo() {
        Calendar d1 = date("2013-01-01");
        Calendar d2 = date("2012-11-02");
        String actual = RelativeDate.getRelativeDate(d1, d2);
        assertEquals(res(R.string.dates_months_ago, 2), actual);
    }

    public void test364DaysAgo() {
        Calendar d1 = date("2013-01-01");
        Calendar d2 = date("2012-01-03");
        String actual = RelativeDate.getRelativeDate(d1, d2);
        assertEquals(res(R.string.dates_months_ago, 12), actual);
    }

    public void test365DaysAgo() {
        Calendar d1 = date("2013-01-01");
        Calendar d2 = date("2012-01-02");
        String actual = RelativeDate.getRelativeDate(d1, d2);
        assertEquals("2012-01-02", actual);
    }

}
