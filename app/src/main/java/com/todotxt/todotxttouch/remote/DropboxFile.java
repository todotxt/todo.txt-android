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

import com.dropbox.client2.DropboxAPI;

import java.io.File;

public class DropboxFile {
    private String remoteFile;
    private File localFile;
    private String originalRev;
    private DropboxAPI.Entry loadedMetadata;
    private DropboxFileStatus status;
    private Exception error;

    /**
     * @param remoteFile
     * @param localFile
     * @param originalRev
     */
    public DropboxFile(String remoteFile, File localFile, String originalRev) {
        this.remoteFile = remoteFile;
        this.localFile = localFile;
        this.originalRev = originalRev;
        this.status = DropboxFileStatus.INITIALIZED;
    }

    /**
     * @return the loadedMetadata
     */
    public DropboxAPI.Entry getLoadedMetadata() {
        return loadedMetadata;
    }

    /**
     * @param loadedMetadata the loadedMetadata to set
     */
    public void setLoadedMetadata(DropboxAPI.Entry loadedMetadata) {
        this.loadedMetadata = loadedMetadata;
    }

    /**
     * @return the status
     */
    public DropboxFileStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(DropboxFileStatus status) {
        this.status = status;
    }

    /**
     * @return the error
     */
    public Exception getError() {
        return error;
    }

    /**
     * @param error the error to set
     */
    public void setError(Exception error) {
        this.error = error;
    }

    /**
     * @return the remoteFile
     */
    public String getRemoteFile() {
        return remoteFile;
    }

    /**
     * @return the localFile
     */
    public File getLocalFile() {
        return localFile;
    }

    /**
     * @return the originalRev
     */
    public String getOriginalRev() {
        return originalRev;
    }
}
