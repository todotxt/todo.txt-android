/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/task/MailAddressParser.java
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
 * MailAdressParser
 * A utility class for parsing a string to find all email addresses.
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailAddressParser {
	private static final Pattern MAIL_ADDRESS_PATTERN = Pattern
			.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@"
					+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\."
					+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");
	private static final MailAddressParser INSTANCE = new MailAddressParser();

	private MailAddressParser() {
	}

	public static MailAddressParser getInstance() {
		return INSTANCE;
	}

	public List<String> parse(String inputText) {
		if (inputText == null) {
			return Collections.emptyList();
		}

		Matcher m = MAIL_ADDRESS_PATTERN.matcher(inputText);
		List<String> addresses = new ArrayList<String>();
		while (m.find()) {
			addresses.add(m.group().trim());
		}
		return addresses;
	}
}
