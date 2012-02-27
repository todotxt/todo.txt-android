/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 * 
 * Thanks to: http://kurtischiappone.com/programming/java/relative-date
 */
package com.todotxt.todotxttouch.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;

import com.todotxt.todotxttouch.R;
import com.todotxt.todotxttouch.TodoApplication;

public class RelativeDate {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private static Context context = TodoApplication.getAppContetxt();
	
	
	/**
	 * This method computes the relative date according to the Calendar being
	 * passed in and the number of years, months, days, etc. that differ. This
	 * will compute both past and future relative dates. E.g., "one day ago" and
	 * "one day from now".
	 * <p>
	 * <strong>NOTE:</strong> If the calendar date relative to "now" is older
	 * than one day, we display the actual date in its default format as
	 * specified by this class. The date format may be changed by calling
	 * {@link RelativeDate#setDateFormat(SimpleDateFormat)} If you don't want to
	 * show the actual date, but you want to show the relative date for days,
	 * months, and years, you can add the other cases in by copying the logic
	 * for hours, minutes, seconds.
	 * 
	 * @param calendar
	 * @param years
	 * @param months
	 * @param days
	 * @param hours
	 * @param minutes
	 * @param seconds
	 * @return String representing the relative date
	 */

	private static String computeRelativeDate(Calendar calendar, int years,
			int months, int days, int hours, int minutes, int seconds) {
		
		String date = sdf.format(calendar.getTime());

		if (years == 0 && months == 0) {
			if (days < -1)
				return context.getString(R.string.dates_days_ago, Math.abs(days));
			else if (days == -1)
				return context.getString(R.string.dates_one_day_ago);
			else if (days == 0)
				return context.getString(R.string.dates_today);		} else if (years == 0 || years == -1) {
			if (years == -1) {
				months = 11 - months
						+ Calendar.getInstance().get(Calendar.MONTH);
				if (months == 1)
					return context.getString(R.string.dates_one_month_ago);
				else
					return context.getString(R.string.dates_months_ago, months);
			} else {
				if (months != -1)
					return context.getString(R.string.dates_months_ago,Math.abs(months));
				else
					return context.getString(R.string.dates_one_month_ago);
			}
		} else {
			return date;
		}
		return date;

	}

	/**
	 * This method returns a String representing the relative date by comparing
	 * the Calendar being passed in to the date / time that it is right now.
	 * 
	 * @param calendar
	 * @return String representing the relative date
	 */

	public static String getRelativeDate(Calendar calendar) {

		Calendar now = GregorianCalendar.getInstance();

		int years = calendar.get(Calendar.YEAR) - now.get(Calendar.YEAR);
		int months = calendar.get(Calendar.MONTH) - now.get(Calendar.MONTH);
		int days = calendar.get(Calendar.DAY_OF_MONTH)
				- now.get(Calendar.DAY_OF_MONTH);
		int hours = calendar.get(Calendar.HOUR_OF_DAY)
				- now.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE) - now.get(Calendar.MINUTE);
		int seconds = calendar.get(Calendar.SECOND) - now.get(Calendar.SECOND);

		return computeRelativeDate(calendar, years, months, days, hours,
				minutes, seconds);

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

	/**
	 * This method sets the date format. This is used when the relative date is
	 * beyond one day. E.g., if the relative date is > 1 day, we will display
	 * the date in the format: h:mm a MMM dd, yyyy
	 * <p>
	 * This can be changed by passing in a new simple date format and then
	 * calling {@link RelativeDate#getRelativeDate(Calendar)} or
	 * {@link RelativeDate#getRelativeDate(Date)}.
	 * 
	 * @param dateFormat
	 */

	public static void setDateFormat(SimpleDateFormat dateFormat) {
		sdf = dateFormat;
	}

}