/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/task/DropboxFileRemoteException.java
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


import com.dropbox.client.DropboxAPI;

/**
 * Represents an error that occurred during Dropbox persistence
 *
 * @author Tim Barlotta
 */
public class DropboxFileRemoteException extends RemoteException {
    private DropboxAPI.FileDownload dropboxFileDownload;

    public DropboxFileRemoteException(String message, DropboxAPI.FileDownload dropboxFileDownload) {
        super(message);
        this.dropboxFileDownload = dropboxFileDownload;
    }

    public DropboxFileRemoteException(String message, Throwable cause, DropboxAPI.FileDownload dropboxFileDownload) {
        super(message, cause);
        this.dropboxFileDownload = dropboxFileDownload;
    }

    public DropboxAPI.FileDownload getFileDownload() {
        return dropboxFileDownload;
    }
}
