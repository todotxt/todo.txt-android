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

package com.todotxt.todotxttouch.remote;

import com.todotxt.todotxttouch.util.Path;
import com.todotxt.todotxttouch.util.Strings;

class RemoteFolderImpl implements RemoteFolder {
    protected String mPath;
    protected String mName;
    protected String mParentPath;
    protected String mParentName;

    public RemoteFolderImpl(String path) {
        mPath = path;
        mName = Path.fileName(path);
        mParentPath = Path.parentPath(path);
        mParentName = Path.fileName(mParentPath);
    }

    public RemoteFolderImpl(String path, String name, String parentPath, String parentName) {
        mPath = path;
        mName = name;
        mParentPath = parentPath;
        mParentName = parentName;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getParentName() {
        return mParentName;
    }

    @Override
    public String getPath() {
        return mPath;
    }

    @Override
    public String getParentPath() {
        return mParentPath;
    }

    @Override
    public boolean hasParent() {
        return !Strings.isBlank(getParentPath());
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RemoteFolder) {
            return getPath().equalsIgnoreCase(((RemoteFolder) o).getPath());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

}
