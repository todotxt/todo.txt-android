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

package com.todotxt.todotxttouch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.todotxt.todotxttouch.util.Util;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.util.Log;

public class UpgradeHandler {
    private static final String TAG = UpgradeHandler.class.getSimpleName();

    private TodoApplication mApp;
    private int mCurVersion;
    private int mPrevVersion;
    List<UpgradeTask> mHandlers = new ArrayList<UpgradeTask>();

    public UpgradeHandler(TodoApplication application) {
        mApp = application;
    }

    public void run() throws NameNotFoundException {
        mCurVersion = mApp.getPackageManager().getPackageInfo(mApp.getPackageName(), 0).versionCode;
        mPrevVersion = mApp.m_prefs.getVersion();

        if (mPrevVersion >= mCurVersion) {
            Log.d(TAG, "No need to upgrade. Stored version is " + mPrevVersion
                    + ". Cur version is "
                    + mCurVersion);

            return;
        }

        Log.i(TAG, "Running upgrade tasks from " + mPrevVersion + " to " + mCurVersion);

        initHandlers();

        for (UpgradeTask handler : mHandlers) {
            int version = handler.getVersion();

            if (version > mPrevVersion && version <= mCurVersion) {
                Log.i(TAG, "Running upgrade task: " + handler.getDescription());
                handler.getRunnable().run();
            }
        }

        mApp.m_prefs.storeVersion(mCurVersion);

        Log.i(TAG, "Successfully upgraded to version " + mCurVersion);
    }

    final class UpgradeTask {
        private String mDescription;
        private int mVersion;
        private Runnable mRunnable;

        public UpgradeTask(String description, int version, Runnable runnable) {
            mDescription = description;
            mVersion = version;
            mRunnable = runnable;
        }

        public String getDescription() {
            return mDescription;
        }

        public int getVersion() {
            return mVersion;
        }

        public Runnable getRunnable() {
            return mRunnable;
        }
    }

    void initHandlers() {
        UpgradeTask up53 = new UpgradeTask(
                "Migrate data from external to internal storage", 53,
                new Runnable() {
                    @Override
                    public void run() {
                        File srcTodo = new File(Environment
                                .getExternalStorageDirectory(),
                                "data/com.todotxt.todotxttouch/todo.txt");
                        File srcDone = new File(Environment
                                .getExternalStorageDirectory(),
                                "data/com.todotxt.todotxttouch/done.txt");
                        File destTodo = new File(TodoApplication
                                .getAppContetxt().getFilesDir(), "todo.txt");
                        File destDone = new File(TodoApplication
                                .getAppContetxt().getFilesDir(), "done.txt");

                        if (srcTodo.exists() && !destTodo.exists()) {
                            Log.d(TAG, "Copying " + srcTodo + " to " + destTodo);

                            Util.copyFile(srcTodo, destTodo, false);
                        }

                        if (srcDone.exists() && !destDone.exists()) {
                            Log.d(TAG, "Copying " + srcDone + " to " + destDone);

                            Util.copyFile(srcDone, destDone, false);
                        }
                    }
                });
        mHandlers.add(up53);
    }
}
