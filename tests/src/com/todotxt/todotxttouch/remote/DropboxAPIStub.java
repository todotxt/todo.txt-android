/**
 * This file is part of Todo.txtndroid app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2013 Todo.txt contributors (http://todotxt.com)
 *
 * LICENSE:
 *
 * Todo.tTodo.txttware: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt is Todo.txt the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt.  If not,Todo.txt//www.gnu.org/licenses/>.
 *
 * @author Todo.txt contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt contributors (http://todotxt.com)
 */

package com.todotxt.todotxttouch.remote;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.Session;

public class DropboxAPIStub extends DropboxAPI<Session> {

    public DropboxAPIStub() {
        super(new SessionStub());
    }

    /*
     * (non-Javadoc)
     * @see com.dropbox.client2.DropboxAPI#accountInfo()
     */
    @Override
    public com.dropbox.client2.DropboxAPI.Account accountInfo()
            throws DropboxException {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.dropbox.client2.DropboxAPI#assertAuthenticated()
     */
    @Override
    protected void assertAuthenticated() throws DropboxUnlinkedException {
    }

    /*
     * (non-Javadoc)
     * @see com.dropbox.client2.DropboxAPI#copy(java.lang.String,
     * java.lang.String)
     */
    @Override
    public com.dropbox.client2.DropboxAPI.Entry copy(String arg0, String arg1)
            throws DropboxException {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.dropbox.client2.DropboxAPI#createFolder(java.lang.String)
     */
    @Override
    public com.dropbox.client2.DropboxAPI.Entry createFolder(String arg0)
            throws DropboxException {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.dropbox.client2.DropboxAPI#delete(java.lang.String)
     */
    @Override
    public void delete(String arg0) throws DropboxException {
    }

    /*
     * (non-Javadoc)
     * @see com.dropbox.client2.DropboxAPI#getFile(java.lang.String,
     * java.lang.String, java.io.OutputStream,
     * com.dropbox.client2.ProgressListener)
     */
    @Override
    public com.dropbox.client2.DropboxAPI.DropboxFileInfo getFile(String arg0,
            String arg1, OutputStream arg2, ProgressListener arg3)
            throws DropboxException {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.dropbox.client2.DropboxAPI#getFileStream(java.lang.String,
     * java.lang.String)
     */
    @Override
    public com.dropbox.client2.DropboxAPI.DropboxInputStream getFileStream(
            String arg0, String arg1) throws DropboxException {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.dropbox.client2.DropboxAPI#getSession()
     */
    @Override
    public Session getSession() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.dropbox.client2.DropboxAPI#getThumbnail(java.lang.String,
     * java.io.OutputStream, com.dropbox.client2.DropboxAPI.ThumbSize,
     * com.dropbox.client2.DropboxAPI.ThumbFormat,
     * com.dropbox.client2.ProgressListener)
     */
    @Override
    public com.dropbox.client2.DropboxAPI.DropboxFileInfo getThumbnail(
            String arg0, OutputStream arg1,
            com.dropbox.client2.DropboxAPI.ThumbSize arg2,
            com.dropbox.client2.DropboxAPI.ThumbFormat arg3, ProgressListener arg4)
            throws DropboxException {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.dropbox.client2.DropboxAPI#getThumbnailStream(java.lang.String,
     * com.dropbox.client2.DropboxAPI.ThumbSize,
     * com.dropbox.client2.DropboxAPI.ThumbFormat)
     */
    @Override
    public com.dropbox.client2.DropboxAPI.DropboxInputStream getThumbnailStream(
            String arg0, com.dropbox.client2.DropboxAPI.ThumbSize arg1,
            com.dropbox.client2.DropboxAPI.ThumbFormat arg2) throws DropboxException {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.dropbox.client2.DropboxAPI#media(java.lang.String, boolean)
     */
    @Override
    public com.dropbox.client2.DropboxAPI.DropboxLink media(String arg0,
            boolean arg1) throws DropboxException {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.dropbox.client2.DropboxAPI#metadata(java.lang.String, int,
     * java.lang.String, boolean, java.lang.String)
     */
    @Override
    public com.dropbox.client2.DropboxAPI.Entry metadata(String arg0, int arg1,
            String arg2, boolean arg3, String arg4) throws DropboxException {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.dropbox.client2.DropboxAPI#move(java.lang.String,
     * java.lang.String)
     */
    @Override
    public com.dropbox.client2.DropboxAPI.Entry move(String arg0, String arg1)
            throws DropboxException {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.dropbox.client2.DropboxAPI#putFile(java.lang.String,
     * java.io.InputStream, long, java.lang.String,
     * com.dropbox.client2.ProgressListener)
     */
    @Override
    public com.dropbox.client2.DropboxAPI.Entry putFile(String arg0,
            InputStream arg1, long arg2, String arg3, ProgressListener arg4)
            throws DropboxException {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.dropbox.client2.DropboxAPI#putFileOverwrite(java.lang.String,
     * java.io.InputStream, long, com.dropbox.client2.ProgressListener)
     */
    @Override
    public com.dropbox.client2.DropboxAPI.Entry putFileOverwrite(String arg0,
            InputStream arg1, long arg2, ProgressListener arg3)
            throws DropboxException {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.dropbox.client2.DropboxAPI#putFileOverwriteRequest(java.lang.String,
     * java.io.InputStream, long, com.dropbox.client2.ProgressListener)
     */
    @Override
    public com.dropbox.client2.DropboxAPI.UploadRequest putFileOverwriteRequest(
            String arg0, InputStream arg1, long arg2, ProgressListener arg3)
            throws DropboxException {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.dropbox.client2.DropboxAPI#putFileRequest(java.lang.String,
     * java.io.InputStream, long, java.lang.String,
     * com.dropbox.client2.ProgressListener)
     */
    @Override
    public com.dropbox.client2.DropboxAPI.UploadRequest putFileRequest(
            String arg0, InputStream arg1, long arg2, String arg3,
            ProgressListener arg4) throws DropboxException {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.dropbox.client2.DropboxAPI#restore(java.lang.String,
     * java.lang.String)
     */
    @Override
    public com.dropbox.client2.DropboxAPI.Entry restore(String arg0, String arg1)
            throws DropboxException {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.dropbox.client2.DropboxAPI#revisions(java.lang.String, int)
     */
    @Override
    public List<com.dropbox.client2.DropboxAPI.Entry> revisions(String arg0,
            int arg1) throws DropboxException {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.dropbox.client2.DropboxAPI#search(java.lang.String,
     * java.lang.String, int, boolean)
     */
    @Override
    public List<com.dropbox.client2.DropboxAPI.Entry> search(String arg0,
            String arg1, int arg2, boolean arg3) throws DropboxException {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.dropbox.client2.DropboxAPI#share(java.lang.String)
     */
    @Override
    public com.dropbox.client2.DropboxAPI.DropboxLink share(String arg0)
            throws DropboxException {
        return null;
    }

}
