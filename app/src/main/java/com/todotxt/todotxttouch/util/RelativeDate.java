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
/**
 * This file is part of Todo.txt, an Android app for managing your todo.txt file (http://todotxt.com).
 * 
 * Thanks to: http://kurtischiappone.com/programming/java/relative-date
 */

package com.todotxt.todotxttouch.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.annotation.SuppressLint;
import android.content.Context;

import com.todotxt.todotxttouch.R;
import com.todotxt.todotxttouch.TodoApplication;

@SuppressLint("SimpleDateFormat")
public class RelativeDate {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private static Context context = TodoApplication.getAppContetxt();

    // Doesn't handle leap year, etc, but we don't need to be very
    // accurate. This is just for human readable date displays.
    private static final long SECOND = 1000; // milliseconds
    private static final long HOUR = 3600 * SECOND;
    private static final long DAY = 24 * HOUR;
    private static final long YEAR = 365 * DAY;

    /**
     * This method returns a String representing the relative date by comparing
     * the Calendar being passed in to the date / time that it is right now.
     * 
     * @param calendar
     * @return String representing the relative date
     */

    public static String getRelativeDate(Calendar calendar) {
        Calendar today = new GregorianCalendar();
        today.set(GregorianCalendar.HOUR_OF_DAY, 0);
        today.set(GregorianCalendar.MINUTE, 0);
        today.set(GregorianCalendar.SECOND, 0);
        today.set(GregorianCalendar.MILLISECOND, 0);

        return getRelativeDate(today, calendar);
    }

    public static String getRelativeDate(Calendar d1, Calendar d2) {
        long diff = d1.getTimeInMillis() - d2.getTimeInMillis();

        if (diff < 0 || diff >= YEAR) {
            // future or far in past,
            // just return yyyy-mm-dd
            return sdf.format(d2.getTime());
        }

        if (diff >= 60 * DAY) {
            // N months ago
            long months = diff / (30 * DAY);

            return context.getString(R.string.dates_months_ago, months);
        }

        if (diff >= 30 * DAY) {
            // 1 month ago
            return context.getString(R.string.dates_one_month_ago);
        }

        if (diff >= 2 * DAY) {
            // more than 2 days ago
            long days = diff / DAY;

            return context.getString(R.string.dates_days_ago, days);
        }

        if (diff >= 1 * DAY) {
            // 1 day ago
            return context.getString(R.string.dates_one_day_ago);
        }

        // today
        return context.getString(R.string.dates_today);
    }

    /**
     * This method returns a String representing the relative date by comparing
     * the Date being passed in to the date / time that it is right now.
     * 
     * @param date
     * @return String representing the relative date
     */
    public static String getRelativeDate(Date date) {
        Calendar converted = GregorianCalendar.getInstance();
        converted.setTime(date);

        return getRelativeDate(converted);
    }
}
