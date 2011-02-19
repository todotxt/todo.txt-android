/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/task/DropboxTaskRepository.java
 *
 * Copyright (c) 2011 Tim Barlotta
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
 * @author Tim Barlotta <tim[at]barlotta[dot]net>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2011 Tim Barlotta
 */

package com.todotxt.todotxttouch.task;

import android.os.Environment;
import com.dropbox.client.DropboxAPI;
import com.todotxt.todotxttouch.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Task repository specific to the Dropbox API
 *
 * @author Tim Barlotta
 */
class DropboxTaskRepository implements RemoteTaskRepository {
    private static final File TODO_TXT_TMP_FILE = new File(Environment.getExternalStorageDirectory(), "data/com.todotxt.todotxttouch/tmp/todo.txt");
    private static final String TODO_TXT_REMOTE_FILE_NAME = "todo.txt";
    private final DropboxAPI dropboxApi;
    private final TaskBagImpl.Preferences preferences;

    public DropboxTaskRepository(TaskBagImpl.Preferences preferences, DropboxAPI dropboxApi) {
        this.dropboxApi = dropboxApi;
        this.preferences = preferences;
    }

    @Override
    public void init(File withLocalFile) {
        try {
            dropboxApi.putFile(Constants.DROPBOX_MODUS, preferences.todoFileDirectory, withLocalFile);
        }
        catch(Exception e) {
            throw new RemoteException("error creating dropbox file", e);
        }
    }

    @Override
    public void purge() {
        TODO_TXT_TMP_FILE.delete();
    }

    @Override
    public ArrayList<Task> load() {
        try {
            DropboxAPI.FileDownload file = dropboxApi.getFileStream(Constants.DROPBOX_MODUS, preferences.todoFileDirectory + "/" + TODO_TXT_REMOTE_FILE_NAME, null);
            if(file.isError()) {
                throw new DropboxFileRemoteException("Error loading from dropbox", file);
            }

            return TaskIo.loadTasksFromStream(file.is);
        }
        catch(IOException e) {
            throw new RemoteException("I/O error trying to load from dropbox", e);
        }
    }

    @Override
    public void store(ArrayList<Task> tasks) {
        TaskIo.writeToFile(tasks, TODO_TXT_TMP_FILE, preferences.useWindowsLineBreaks);
        dropboxApi.putFile(Constants.DROPBOX_MODUS, preferences.todoFileDirectory, TODO_TXT_TMP_FILE);
    }
}
