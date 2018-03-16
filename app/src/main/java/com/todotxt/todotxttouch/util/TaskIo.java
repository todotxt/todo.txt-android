/**
 * This file is part of Todo.txt for Android, an app for managing your todo.txt file (http://todotxt.com).
 * <p>
 * Copyright (c) 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 * <p>
 * LICENSE:
 * <p>
 * Todo.txt for Android is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 * <p>
 * Todo.txt for Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with Todo.txt for Android. If not, see
 * <http://www.gnu.org/licenses/>.
 * <p>
 * Todo.txt for Android's source code is available at https://github.com/ginatrapani/todo.txt-android
 *
 * @author Todo.txt for Android contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 */

package com.todotxt.todotxttouch.util;

import android.util.Log;

import com.todotxt.todotxttouch.task.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for performing Task level I/O
 *
 * @author Tim Barlotta
 */
public class TaskIo {
    private final static String TAG = TaskIo.class.getSimpleName();

    private static boolean sWindowsLineBreaks = false;

    private static String readLine(BufferedReader r) throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean eol = false;
        int c;

        while (!eol && (c = r.read()) >= 0) {
            sb.append((char) c);
            eol = (c == '\r' || c == '\n');

            // check for \r\n
            if (c == '\r') {
                r.mark(1);
                c = r.read();

                if (c != '\n') {
                    r.reset();
                } else {
                    sWindowsLineBreaks = true;
                    sb.append((char) c);
                }
            }
        }

        return sb.length() == 0 ? null : sb.toString();
    }

    public static ArrayList<Task> loadTasksFromFile(File file) throws IOException {
        ArrayList<Task> items = new ArrayList<Task>();
        BufferedReader in = null;

        if (!file.exists()) {
            Log.w(TAG, file.getAbsolutePath() + " does not exist!");
        } else {
            InputStream is = new FileInputStream(file);

            try {
                in = new BufferedReader(new InputStreamReader(is));
                String line;
                long counter = 0L;
                sWindowsLineBreaks = false;

                while ((line = readLine(in)) != null) {
                    line = line.trim();

                    if (line.length() > 0) {
                        items.add(new Task(counter, line));
                    }

                    counter++;
                }
            } finally {
                Util.closeStream(in);
                Util.closeStream(is);
            }
        }

        return items;
    }

    public static void writeToFile(List<Task> tasks, File file) {
        writeToFile(tasks, file, false);
    }

    public static void writeToFile(List<Task> tasks, File file, boolean append) {
        try {
            if (!Util.isDeviceWritable()) {
                throw new IOException("Device is not writable!");
            }

            Util.createParentDirectory(file);
            FileWriter fw = new FileWriter(file, append);

            for (int i = 0; i < tasks.size(); ++i) {
                String fileFormat = tasks.get(i).inFileFormat();
                fw.write(fileFormat);

                if (sWindowsLineBreaks) {
                    // Log.v(TAG, "Using Windows line breaks");
                    fw.write("\r\n");
                } else {
                    // Log.v(TAG, "NOT using Windows line breaks");
                    fw.write("\n");
                }
            }

            fw.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
