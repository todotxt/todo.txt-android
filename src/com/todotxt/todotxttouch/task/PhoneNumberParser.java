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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.os.Build;

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

        // Only run the phone number parser if Android version is not Honeycomb
        // API level 11 - 13
        int sdk = Build.VERSION.SDK_INT;

        if (sdk >= 11 && sdk <= 13) {
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
