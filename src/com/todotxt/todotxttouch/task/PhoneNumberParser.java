/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/task/PhoneNumberParser.java
 *
 * Copyright (c) 2011 Florian Behr
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
 * PhoneNumberParser
 * A utility class for parsing a string to find all phone numbers. 
 *
 * @author Florian Behr <mail[at]florianbehr[dot]de>
 *
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2011 Florian Behr
 */
package com.todotxt.todotxttouch.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;

public class PhoneNumberParser {
	// private static final Pattern NUMBER_PATTERN = android.util.Patterns.
	private static final PhoneNumberParser INSTANCE = new PhoneNumberParser();

	private PhoneNumberParser() {
	}

	public static PhoneNumberParser getInstance() {
		return INSTANCE;
	}

	public List<String> parse(String inputText) {
		if (inputText == null) {
			return Collections.emptyList();
		}

		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		Iterable<PhoneNumberMatch> numbersMatch = phoneUtil.findNumbers(
				inputText, Locale.getDefault().getCountry());
		ArrayList<String> numbers = new ArrayList<String>();
		for (PhoneNumberMatch number : numbersMatch) {
			numbers.add(phoneUtil.format(number.number(),
					PhoneNumberFormat.NATIONAL));

		}

		return numbers;
	}
}
